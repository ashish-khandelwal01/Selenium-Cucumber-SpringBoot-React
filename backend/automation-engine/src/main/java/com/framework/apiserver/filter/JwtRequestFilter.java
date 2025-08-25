package com.framework.apiserver.filter;

import com.framework.apiserver.config.JwtConfig;
import com.framework.apiserver.utilities.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Filter for processing incoming HTTP requests to validate JWT tokens.
 * Extends OncePerRequestFilter to ensure the filter is executed once per request.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * Filters incoming requests to validate the JWT token and set the authentication context.
     *
     * @param request  The HttpServletRequest object containing client request details.
     * @param response The HttpServletResponse object used to send the response.
     * @param chain    The FilterChain to pass the request and response to the next filter.
     * @throws ServletException If an error occurs during request processing.
     * @throws IOException      If an input or output error occurs during request processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Skip JWT processing for permitted paths
        if (isPermittedPath(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Retrieve the JWT token from the request header
        final String requestTokenHeader = request.getHeader(jwtConfig.getHeaderString());

        String username = null;
        String jwtToken = null;

        // Check if the Authorization header exists and has the correct format
        if (requestTokenHeader != null && requestTokenHeader.startsWith(jwtConfig.getTokenPrefix())) {
            jwtToken = requestTokenHeader.substring(jwtConfig.getTokenPrefix().length());
            try {
                // Extract the username from the token
                username = jwtUtil.extractUsername(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.error("Unable to get JWT Token");
            } catch (Exception e) {
                logger.error("JWT Token has expired");
            }
        } else if (requestTokenHeader != null) {
            logger.warn("JWT Token does not begin with the expected prefix");
        }

        // Validate the token and set the authentication context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user details from the UserDetailsService
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Validate the token and set the authentication in the security context
            if (jwtUtil.validateToken(jwtToken, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        // Continue the filter chain
        chain.doFilter(request, response);
    }

    /**
     * Check if the requested URI is in the list of paths that don't require JWT authentication.
     *
     * @param uri The request URI to check
     * @return true if the path is permitted without authentication, false otherwise
     */
    private boolean isPermittedPath(String uri) {
        return uri.startsWith("/api/auth/") ||
                uri.startsWith("/api/public/") ||
                uri.equals("/api/jobs/updates") ||
                uri.startsWith("/swagger-ui/") ||
                uri.startsWith("/v3/api-docs/");
    }
}