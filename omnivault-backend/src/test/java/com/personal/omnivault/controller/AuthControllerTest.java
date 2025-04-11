package com.personal.omnivault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.omnivault.config.RateLimiter;
import com.personal.omnivault.domain.dto.request.LoginRequest;
import com.personal.omnivault.domain.dto.request.RegisterRequest;
import com.personal.omnivault.domain.dto.request.TokenRefreshRequest;
import com.personal.omnivault.domain.dto.response.AuthResponse;
import com.personal.omnivault.domain.dto.response.UserDTO;
import com.personal.omnivault.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private RateLimiter rateLimiter;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private TokenRefreshRequest refreshRequest;
    private AuthResponse authResponse;
    private UserDTO userDTO;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new com.personal.omnivault.exception.GlobalExceptionHandler())
                .build();

        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();

        refreshRequest = TokenRefreshRequest.builder()
                .refreshToken("refresh-token")
                .build();

        userDTO = UserDTO.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .createdAt(ZonedDateTime.now())
                .build();

        authResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(userDTO)
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void register_Success() throws Exception {
        // Given
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access-token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(3600)))
                .andExpect(jsonPath("$.user.username", is("testuser")))
                .andExpect(jsonPath("$.user.email", is("test@example.com")));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should login user successfully")
    void login_Success() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access-token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(3600)))
                .andExpect(jsonPath("$.user.username", is("testuser")))
                .andExpect(jsonPath("$.user.email", is("test@example.com")));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void refreshToken_Success() throws Exception {
        // Given
        when(authService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access-token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh-token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(3600)))
                .andExpect(jsonPath("$.user.username", is("testuser")))
                .andExpect(jsonPath("$.user.email", is("test@example.com")));

        verify(authService).refreshToken(any(TokenRefreshRequest.class));
    }

    @Test
    @DisplayName("Should logout successfully")
    void logout_Success() throws Exception {
        // Given
        doNothing().when(authService).logout(anyString());

        // When & Then
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk());

        verify(authService).logout(anyString());
    }

    @Test
    @DisplayName("Should get current user successfully")
    void getCurrentUser_Success() throws Exception {
        // Given
        when(authService.getCurrentUserDTO()).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(get("/auth/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("User")));

        verify(authService).getCurrentUserDTO();
    }

    @Test
    @DisplayName("Should verify email with token successfully")
    void verifyEmail_Success() throws Exception {
        // Given
        when(authService.verifyEmail(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/verify/token")
                        .param("token", "verification-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(true)));

        verify(authService).verifyEmail("verification-token");
    }

    @Test
    @DisplayName("Should verify email with OTP successfully")
    void verifyEmailWithOTP_Success() throws Exception {
        // Given
        when(authService.verifyEmailWithOTP(anyString(), anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/verify/otp")
                        .param("email", "test@example.com")
                        .param("otpCode", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified", is(true)));

        verify(authService).verifyEmailWithOTP("test@example.com", "123456");
    }

    @Test
    @DisplayName("Should resend verification email successfully")
    void resendVerificationEmail_Success() throws Exception {
        // Given
        when(rateLimiter.allowRequest(any(HttpServletRequest.class), eq(5))).thenReturn(true);
        when(authService.resendVerificationEmail(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/resend-verification")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sent", is(true)));

        verify(rateLimiter).allowRequest(any(HttpServletRequest.class), eq(5));
        verify(authService).resendVerificationEmail("test@example.com");
    }

    @Test
    @DisplayName("Should handle rate limit exceeded for resend verification")
    void resendVerificationEmail_RateLimitExceeded() throws Exception {
        // Given
        when(rateLimiter.allowRequest(any(HttpServletRequest.class), eq(5))).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/auth/resend-verification")
                        .param("email", "test@example.com"))
                .andExpect(status().isBadRequest());

        verify(rateLimiter).allowRequest(any(HttpServletRequest.class), eq(5));
        verify(authService, never()).resendVerificationEmail(anyString());
    }

    @Test
    @DisplayName("Should handle validation errors for register request")
    void register_ValidationError() throws Exception {
        // Given
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("t") // Too short username
                .email("invalid-email") // Invalid email format
                .password("short") // Too short password
                .build();

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should handle validation errors for login request")
    void login_ValidationError() throws Exception {
        // Given
        LoginRequest invalidRequest = LoginRequest.builder().build(); // Missing required fields

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.usernameOrEmail").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should handle validation errors for refresh token request")
    void refreshToken_ValidationError() throws Exception {
        // Given
        TokenRefreshRequest invalidRequest = TokenRefreshRequest.builder().build(); // Missing required field

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.refreshToken").exists());

        verify(authService, never()).refreshToken(any(TokenRefreshRequest.class));
    }
}