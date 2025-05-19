package com.shadsluiter.eventsapp.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.shadsluiter.eventsapp.models.UserModel;
import com.shadsluiter.eventsapp.security.JwtTokenProvider;
import com.shadsluiter.eventsapp.service.UserService;

/**
 * REST API controller for user registration and authentication.
 * 
 * Provides endpoints for registering new users and issuing JWTs on successful login.
 */
@RestController
@RequestMapping("/api/users")
public class UsersApiController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * Constructs the UsersApiController with required dependencies.
     * 
     * @param userService the user service to manage user data
     * @param authManager the authentication manager to validate credentials
     * @param tokenProvider the JWT token provider
     */
    public UsersApiController(UserService userService, AuthenticationManager authManager, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Registers a new user if the username does not already exist.
     * 
     * @param user the new user data
     * @return the created user or an error if the user already exists
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserModel user) {
        if (userService.findByLoginName(user.getUserName()) != null) {
            return ResponseEntity.badRequest().body("User already exists");
        }

        return ResponseEntity.ok(userService.save(user));
    }

    /**
     * Authenticates a user and returns a JWT token upon success.
     * 
     * @param loginRequest the login credentials
     * @return a JWT token in the response body
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody UserModel loginRequest) {
        Authentication authentication = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUserName(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);

        return ResponseEntity.ok(response);
    }
}
