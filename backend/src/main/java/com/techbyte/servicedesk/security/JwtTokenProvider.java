package com.techbyte.servicedesk.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        String normalizedSecret = secret.length() >= 32
                ? secret
                : (secret + "________________________________").substring(0, 32);
        this.secretKey = Keys.hmacShaKeyFor(normalizedSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(CustomUserPrincipal principal) {
        return generateTokenWithCustomTtl(principal, this.expirationMs);
    }

    public String generateTokenWithCustomTtl(CustomUserPrincipal principal, long ttlMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ttlMs);

        return Jwts.builder()
                .subject(principal.getEmail())
                .claim("userId", principal.getId())
                .claim("role", principal.getRole().name())
                .claim("fullName", principal.getFullName())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}

