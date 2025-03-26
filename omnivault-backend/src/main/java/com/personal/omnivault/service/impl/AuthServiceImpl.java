package com.personal.omnivault.service.impl;

import com.personal.omnivault.config.JwtProperties;
import com.personal.omnivault.domain.dto.request.LoginRequest;
import com.personal.omnivault.domain.dto.request.RegisterRequest;
import com.personal.omnivault.domain.dto.request.TokenRefreshRequest;
import com.personal.omnivault.domain.dto.response.AuthResponse;
import com.personal.omnivault.domain.dto.response.UserDTO;
import com.personal.omnivault.domain.model.RefreshToken;
import com.personal.omnivault.domain.model.User;
import com.personal.omnivault.domain.model.VerificationToken;
import com.personal.omnivault.exception.AuthenticationException;
import com.personal.omnivault.exception.BadRequestException;
import com.personal.omnivault.exception.ResourceNotFoundException;
import com.personal.omnivault.repository.RefreshTokenRepository;
import com.personal.omnivault.repository.UserRepository;
import com.personal.omnivault.repository.VerificationTokenRepository;
import com.personal.omnivault.security.TokenProvider;
import com.personal.omnivault.security.UserPrincipal;
import com.personal.omnivault.service.AuthService;
import com.personal.omnivault.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.UUID;

@Service("authService")
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username is already taken
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        // Check if email is already in use
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        // Create new user - note: we set enabled to false until email verification
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .emailVerified(false)
                .enabled(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Created new user with username: {}", savedUser.getUsername());

        // Generate verification token and OTP
        String token = UUID.randomUUID().toString();
        String otpCode = generateOTP(6); // 6-digit OTP

        // Create verification token with expiry
        int tokenExpiryMinutes = 1440; // 24 hours
        ZonedDateTime expiryDate = ZonedDateTime.now().plusMinutes(tokenExpiryMinutes);

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .otpCode(otpCode)
                .user(savedUser)
                .expiryDate(expiryDate)
                .build();

        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(savedUser, token, otpCode);

        // Return limited auth response (no tokens yet since not verified)
        return AuthResponse.builder()
                .user(convertToUserDto(savedUser))
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Verification Token", "token", token));

        if (verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken);
            throw new BadRequestException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);

        // Delete the used token
        verificationTokenRepository.delete(verificationToken);

        return true;
    }

    @Override
    @Transactional
    public boolean verifyEmailWithOTP(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        VerificationToken verificationToken = verificationTokenRepository.findByUserAndOtpCodeIsNotNull(user)
                .orElseThrow(() -> new ResourceNotFoundException("Verification Token", "user", user.getId()));

        if (!verificationToken.getOtpCode().equals(otpCode)) {
            throw new BadRequestException("Invalid OTP code");
        }

        if (verificationToken.isExpired()) {
            verificationTokenRepository.delete(verificationToken);
            throw new BadRequestException("OTP code has expired");
        }

        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);

        // Delete the used token
        verificationTokenRepository.delete(verificationToken);

        return true;
    }

    @Override
    @Transactional
    public boolean resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        // Create new token and send email
        emailService.resendVerificationEmail(user);

        return true;
    }

    private String generateOTP(int length) {
        StringBuilder otp = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        RefreshToken refreshToken = createRefreshToken(user);

        log.info("User logged in: {}", user.getUsername());
        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest refreshRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshRequest.getRefreshToken())
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        // Check if token is expired or blacklisted
        if (refreshToken.isExpired() || refreshToken.isBlacklisted()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthenticationException("Refresh token expired or revoked");
        }

        User user = refreshToken.getUser();
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        // Generate new tokens
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);

        // Optionally, we can rotate the refresh token for better security
        // refreshTokenRepository.delete(refreshToken);
        // RefreshToken newRefreshToken = createRefreshToken(user);
        // return buildAuthResponse(accessToken, newRefreshToken.getToken(), user);

        // Or just reuse the existing refresh token
        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null) {
            return;
        }

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setBlacklisted(true);
                    refreshTokenRepository.save(token);
                    log.info("User logged out and refresh token blacklisted");
                });
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new AuthenticationException("User not authenticated");
        }

        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }

    private RefreshToken createRefreshToken(User user) {
        // Delete any existing non-blacklisted refresh tokens
        refreshTokenRepository.findAllByUserAndBlacklistedFalse(user)
                .forEach(token -> {
                    token.setBlacklisted(true);
                    refreshTokenRepository.save(token);
                });

        // Create new refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(ZonedDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs())))
                .blacklisted(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .user(convertToUserDto(user))
                .build();
    }





    private UserDTO convertToUserDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}