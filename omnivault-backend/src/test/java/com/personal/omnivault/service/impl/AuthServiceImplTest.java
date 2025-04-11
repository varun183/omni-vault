package com.personal.omnivault.service.impl;

import com.personal.omnivault.config.JwtProperties;
import com.personal.omnivault.domain.dto.request.LoginRequest;
import com.personal.omnivault.domain.dto.request.RegisterRequest;
import com.personal.omnivault.domain.dto.response.AuthResponse;
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
import com.personal.omnivault.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Authentication authentication;
    private UserPrincipal userPrincipal;
    private RefreshToken refreshToken;
    private VerificationToken verificationToken;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .emailVerified(true)
                .enabled(true)
                .build();

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();

        userPrincipal = UserPrincipal.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .build();



        refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("refresh-token")
                .user(testUser)
                .expiryDate(ZonedDateTime.now().plusDays(7))
                .blacklisted(false)
                .build();

        verificationToken = VerificationToken.builder()
                .id(UUID.randomUUID())
                .token("verification-token")
                .otpCode("123456")
                .user(testUser)
                .expiryDate(ZonedDateTime.now().plusDays(1))
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void register_Success() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);
        doNothing().when(emailService).sendVerificationEmail(any(User.class), anyString(), anyString());

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("newuser");
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.isEmailVerified()).isFalse();
        assertThat(savedUser.isEnabled()).isFalse();

        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(verificationTokenRepository).save(tokenCaptor.capture());
        VerificationToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(testUser);
        assertThat(savedToken.getToken()).isNotNull();
        assertThat(savedToken.getOtpCode()).isNotNull();

        verify(emailService).sendVerificationEmail(eq(testUser), anyString(), anyString());

        assertThat(response).isNotNull();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getAccessToken()).isNull(); // No tokens yet since not verified
    }

    @Test
    @DisplayName("Should throw exception when username is taken during registration")
    void register_UsernameAlreadyTaken() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.register(registerRequest));

        assertThat(exception.getMessage()).contains("Username is already taken");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email is taken during registration")
    void register_EmailAlreadyInUse() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.register(registerRequest));

        assertThat(exception.getMessage()).contains("Email is already in use");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login user successfully")
    void login_Success() {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateAccessToken(userPrincipal)).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        when(jwtProperties.getAccessTokenExpirationMs()).thenReturn(3600000L);

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findById(testUser.getId());
        verify(tokenProvider).generateAccessToken(userPrincipal);
        verify(refreshTokenRepository).findAllByUserAndBlacklistedFalse(testUser);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(testUser);
        assertThat(savedToken.isBlacklisted()).isFalse();

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        assertThat(response.getUser()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when user not found during login")
    void login_UserNotFound() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));

        assertThat(exception.getMessage()).contains("User not found");
    }

    @Test
    @DisplayName("Should verify email with token successfully")
    void verifyEmail_Success() {
        // Given
        when(verificationTokenRepository.findByToken("verification-token")).thenReturn(Optional.of(verificationToken));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        boolean result = authService.verifyEmail("verification-token");

        // Then
        assertThat(result).isTrue();

        verify(verificationTokenRepository).findByToken("verification-token");
        verify(userRepository).save(any(User.class));
        verify(verificationTokenRepository).delete(verificationToken);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        assertThat(updatedUser.isEmailVerified()).isTrue();
        assertThat(updatedUser.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when verification token not found")
    void verifyEmail_TokenNotFound() {
        // Given
        when(verificationTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> authService.verifyEmail("invalid-token"));

        assertThat(exception.getMessage()).contains("Verification Token not found");
    }

    @Test
    @DisplayName("Should throw exception when verification token is expired")
    void verifyEmail_TokenExpired() {
        // Given
        VerificationToken expiredToken = VerificationToken.builder()
                .id(UUID.randomUUID())
                .token("expired-token")
                .user(testUser)
                .expiryDate(ZonedDateTime.now().minusDays(1))
                .build();

        when(verificationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.verifyEmail("expired-token"));

        assertThat(exception.getMessage()).contains("Verification token has expired");
        verify(verificationTokenRepository).delete(expiredToken);
    }
}