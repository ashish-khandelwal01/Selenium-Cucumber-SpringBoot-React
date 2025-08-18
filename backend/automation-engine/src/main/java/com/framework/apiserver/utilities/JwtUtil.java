package com.framework.apiserver.utilities;

import com.framework.apiserver.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for handling JSON Web Tokens (JWT).
 * Provides methods for generating, validating, and extracting information from JWTs.
 */
@Component
public class JwtUtil {

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * Retrieves the signing key used for JWT creation and validation.
     *
     * @return The secret key for signing JWTs.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    /**
     * Extracts the username (subject) from the given JWT.
     *
     * @param token The JWT from which to extract the username.
     * @return The username contained in the JWT.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the given JWT.
     *
     * @param token The JWT from which to extract the expiration date.
     * @return The expiration date of the JWT.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the given JWT using a claims resolver function.
     *
     * @param token The JWT from which to extract the claim.
     * @param claimsResolver A function to resolve the desired claim from the JWT claims.
     * @param <T> The type of the claim to extract.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the given JWT.
     *
     * @param token The JWT from which to extract all claims.
     * @return The claims contained in the JWT.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the given JWT is expired.
     *
     * @param token The JWT to check.
     * @return True if the token is expired, false otherwise.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a JWT for the given user details.
     *
     * @param userDetails The user details for which to generate the JWT.
     * @return The generated JWT.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generates a JWT for the given username and additional claims.
     *
     * @param username The username for which to generate the JWT.
     * @param extraClaims Additional claims to include in the JWT.
     * @return The generated JWT.
     */
    public String generateToken(String username, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        return createToken(claims, username);
    }

    /**
     * Creates a JWT with the given claims and subject.
     *
     * @param claims The claims to include in the JWT.
     * @param subject The subject (username) of the JWT.
     * @return The created JWT.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the given JWT against the provided user details.
     *
     * @param token The JWT to validate.
     * @param userDetails The user details to validate against.
     * @return True if the token is valid and matches the user details, false otherwise.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Validates the given JWT.
     *
     * @param token The JWT to validate.
     * @return True if the token is valid, false otherwise.
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}