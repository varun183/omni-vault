package com.omnivault.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT Authentication Filter for processing and validating JWT tokens.
 * Intercepts incoming requests to validate JWT tokens, set authentication context,
 * and support both header-based and query parameter-based token authentication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Filters each request to validate and process JWT authentication.
     * Performs the following steps:
     * 1. Attempts to extract JWT from request header or query parameter
     * 2. Validates the token
     * 3. Loads user details if token is valid
     * 4. Sets authentication context for the request
     *
     * @param request The HTTP servlet request
     * @param response The HTTP servlet response
     * @param filterChain The filter chain for continuing request processing
     * @throws ServletException if a servlet-related error occurs
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Try to get token from Authorization header
            String jwt = getJwtFromRequest(request);

            // If not found in header, try to get from query parameter (for iframes, etc.)
            if (jwt == null) {
                jwt = request.getParameter("token");
            }

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                UUID userId = tokenProvider.getUserIdFromToken(jwt);

                UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from the Authorization header.
     * Expects the token to be in the format: "Bearer {token}"
     *
     * @param request The HTTP servlet request
     * @return The JWT token, or null if not found
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}