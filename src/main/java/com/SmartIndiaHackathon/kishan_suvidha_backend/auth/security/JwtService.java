package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.security;

import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long jwtExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long jwtExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(User user) {

        Date now = new Date();
        Date expiration = new Date(
                now.getTime() + jwtExpiration
        );

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public boolean isTokenValid(String token) {

        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception exception) {
            return false;
        }
    }

    public Long extractUserId(String token) {

        Claims claims = extractClaims(token);

        return Long.valueOf(claims.getSubject());
    }

    public String extractEmail(String token) {

        Claims claims = extractClaims(token);

        return claims.get("email", String.class);
    }

    public String extractRole(String token) {

        Claims claims = extractClaims(token);

        return claims.get("role", String.class);
    }

    private Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}