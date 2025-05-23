package com.shadsluiter.eventsapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.shadsluiter.eventsapp.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuration class for Spring Security settings.
 * 
 * This class defines separate security filter chains for session-based
 * authentication (used by the main web app) and stateless JWT-based 
 * authentication (used by the /api/ endpoints).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructs the SecurityConfig with a lazily-initialized JwtAuthenticationFilter.
     * 
     * @param jwtAuthenticationFilter the custom JWT authentication filter
     */
    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Defines the security filter chain for web pages using session-based login.
     * 
     * Applies to all requests that are not handled by the API chain.
     * Allows access to public pages, restricts event creation/editing to ADMIN role,
     * and provides custom login/logout handling.
     * 
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(request -> !request.getRequestURI().startsWith("/api/"))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/js/**", "/events", "/events/search", "/users/loginForm", "/users/register").permitAll()
                .requestMatchers("/events/create", "/events/edit/**", "/events/delete/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/users/loginForm")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/users/loginForm?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/users/logout")
                .logoutSuccessUrl("/")
            );

        return http.build();
    }

    /**
     * Defines the stateless security filter chain for API endpoints using JWT.
     * 
     * Disables CSRF, session state, form login, and HTTP basic auth.
     * Applies to paths under /api/** and uses the JwtAuthenticationFilter
     * to validate requests.
     * 
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized: JWT token required\"}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * Provides the AuthenticationManager bean used for login authentication.
     * 
     * @param authConfig the AuthenticationConfiguration injected by Spring
     * @return an AuthenticationManager instance
     * @throws Exception if unable to retrieve the manager
     */
    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Provides a PasswordEncoder bean using BCrypt hashing algorithm.
     * 
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
