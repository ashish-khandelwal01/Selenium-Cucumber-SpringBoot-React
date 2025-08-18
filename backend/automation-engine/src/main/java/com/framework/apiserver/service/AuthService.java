package com.framework.apiserver.service;

import com.framework.apiserver.dto.UserRegistrationDto;
import com.framework.apiserver.entity.Role;
import com.framework.apiserver.entity.User;
import com.framework.apiserver.repository.UserRepository;
import com.framework.apiserver.utilities.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Service class for handling authentication-related operations.
 */
@Service
@RequiredArgsConstructor // Lombok annotation for constructor injection
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Registers a new user in the system.
     *
     * @param registrationDto The data transfer object containing user registration details.
     * @throws RuntimeException if the username or email is already in use.
     */
    public void registerUser(UserRegistrationDto registrationDto) {
        // Check if the username is already taken
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        // Check if the email is already in use
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create a new User entity using the Builder pattern
        User user = User.builder()
                .username(registrationDto.getUsername())
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .build();

        // Assign the default role to the user
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        user.setRoles(roles);

        // Save the user to the repository
        userRepository.save(user);
    }

    /**
     * Refreshes a JWT token by validating the existing token and generating a new one.
     *
     * @param token The existing JWT token to be refreshed.
     * @return A new JWT token.
     * @throws RuntimeException if the token is invalid.
     */
    public String refreshToken(String token) {
        // Remove the "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate the token and generate a new one if valid
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            return jwtUtil.generateToken(username, new HashMap<>());
        } else {
            throw new RuntimeException("Invalid token for refresh");
        }
    }
}