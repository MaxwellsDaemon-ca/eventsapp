package com.shadsluiter.eventsapp.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Security filter that processes JWT authentication on each request.
 * 
 * Extracts the JWT token from the Authorization header, validates it,
 * and populates the SecurityContext with an authenticated user.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Constructs the filter with the necessary token and user services.
     * 
     * @param tokenProvider the provider to validate and parse JWT tokens
     * @param userDetailsService the service to load user details by username
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filters the incoming request for a valid JWT.
     * 
     * If valid, sets the authentication in the SecurityContext.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getJwtFromRequest(request);

        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromJWT(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetails != null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     * 
     * @param request the incoming HTTP request
     * @return the raw token string, or null if not present or malformed
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
