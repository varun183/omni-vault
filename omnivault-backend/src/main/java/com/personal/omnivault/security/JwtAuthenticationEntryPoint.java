package com.personal.omnivault.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom authentication entry point for handling unauthorized access attempts.
 * Provides a consistent error response for unauthenticated requests,
 * returning a JSON representation of the authentication error.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Handles unauthorized access attempts by sending a structured error response.
     * Creates a JSON response with:
     * - HTTP status code
     * - Error type
     * - Error message
     * - Request path
     *
     * @param request The HTTP request that resulted in an authentication failure
     * @param response The HTTP response to be sent back to the client
     * @param authException The authentication exception that caused the entry point to be called
     * @throws IOException if an input or output exception occurs
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}