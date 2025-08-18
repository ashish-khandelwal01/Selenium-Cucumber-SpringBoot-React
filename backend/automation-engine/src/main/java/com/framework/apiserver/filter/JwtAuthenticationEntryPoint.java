package com.framework.apiserver.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Component that handles unauthorized access attempts.
 * Implements the AuthenticationEntryPoint interface to provide a custom response
 * when an unauthenticated user tries to access a secured resource.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Handles unauthorized access by sending a JSON response with error details.
     *
     * @param request       The HttpServletRequest object containing client request details.
     * @param response      The HttpServletResponse object used to send the response.
     * @param authException The exception thrown when authentication fails.
     * @throws IOException If an input or output error occurs while writing the response.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Set the response content type to JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // Set the HTTP status to 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Create a map to hold the error details
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        // Write the error details as a JSON response
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}