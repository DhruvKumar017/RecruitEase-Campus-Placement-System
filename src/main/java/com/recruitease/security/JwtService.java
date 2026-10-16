package com.recruitease.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public String generateToken(String username, String role) {
        Date now = new Date();

        Date expiryDate = new Date(
                now.getTime() + jwtExpirationMs
        );

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    // Generate a short-lived token for read-only viewers.
    public String generateViewerToken(String viewerId) {

        Date now = new Date();

        Date expiryDate = new Date(
                now.getTime() + (30 * 60 * 1000L)
        );

        return Jwts.builder()
                .subject(viewerId)
                .claim("role", "DEMO_VIEWER")
.claim("accessType", "DEMO_ONLY")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    // Generate a 30-minute token for demo-only access.
public String generateDemoViewerToken(String verifiedEmail) {

    if (verifiedEmail == null || verifiedEmail.isBlank()) {
        throw new IllegalArgumentException(
                "Verified email is required."
        );
    }

    Date now = new Date();

    Date expiryDate = new Date(
            now.getTime() + (30 * 60 * 1000L)
    );

    return Jwts.builder()
            .subject(verifiedEmail.trim().toLowerCase(
                    java.util.Locale.ROOT
            ))
            .claim("role", "DEMO_VIEWER")
            .claim("accessType", "DEMO_ONLY")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
}

    // Generate JWT for authenticated students
public String generateStudentToken(Long studentId) {

    if (studentId == null) {
        throw new IllegalArgumentException("Student ID is required");
    }

    Date now = new Date();

    Date expiryDate = new Date(
            now.getTime() + jwtExpirationMs
    );

    return Jwts.builder()
            .subject(studentId.toString())
            .claim("role", "STUDENT")
            .claim("studentId", studentId)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
}
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

public String extractRole(String token) {
    return getClaims(token).get("role", String.class);
}

// Extract student ID only from STUDENT tokens
public Long extractStudentId(String token) {

    Claims claims = getClaims(token);

    String role = claims.get("role", String.class);

    if (!"STUDENT".equals(role)) {
        throw new IllegalArgumentException(
                "This token does not belong to a student."
        );
    }

    Number studentId = claims.get("studentId", Number.class);

    if (studentId == null) {
        throw new IllegalArgumentException(
                "Student ID not found in token."
        );
    }

    return studentId.longValue();
}

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);

            return claims.getSubject() != null
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());

        } catch (Exception error) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must contain at least 32 bytes."
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}