package com.framework.apiserver.controller;

import com.framework.apiserver.dto.JwtRequest;
import com.framework.apiserver.dto.JwtResponse;
import com.framework.apiserver.dto.UserRegistrationDto;
import com.framework.apiserver.service.AuthService;
import com.framework.apiserver.utilities.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller for handling authentication-related operations.
 * Provides endpoints for user login, registration, and token refresh.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthService authService;

    /**
     * Endpoint for user login.
     * Authenticates the user and generates a JWT token upon successful login.
     *
     * @param authenticationRequest The login request containing username and password.
     * @return A ResponseEntity containing the JWT token and username.
     * @throws Exception If authentication fails due to invalid credentials.
     */
    @Operation(
            summary = "User login",
            description = "Authenticates the user and returns a JWT token upon successful login."
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody JwtRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Invalid credentials", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token, userDetails.getUsername()));
    }

    /**
     * Endpoint for user registration.
     * Registers a new user with the provided registration details.
     *
     * @param registrationRequest The registration request containing user details.
     * @return A ResponseEntity indicating the success or failure of the registration.
     */
    @Operation(
            summary = "User registration",
            description = "Registers a new user with the provided registration details."
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto registrationRequest) {
        try {
            authService.registerUser(registrationRequest);
            return ResponseEntity.ok().body("User registered successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint for refreshing a JWT token.
     * Generates a new token based on the provided expired or valid token.
     *
     * @param token The existing JWT token from the Authorization header.
     * @return A ResponseEntity containing the refreshed JWT token and username.
     */
    @Operation(
            summary = "Refresh JWT token",
            description = "Generates a new JWT token based on the provided expired or valid token."
    )
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            String refreshedToken = authService.refreshToken(token);
            return ResponseEntity.ok(new JwtResponse(refreshedToken, jwtTokenUtil.extractUsername(refreshedToken)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error refreshing token: " + e.getMessage());
        }
    }
}