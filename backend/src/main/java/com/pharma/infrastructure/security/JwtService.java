package com.pharma.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties props;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + props.getAccessTtl()))
                .signWith(key())
                .compact();
    }

    public String createRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + props.getRefreshTtl()))
                .signWith(key())
                .compact();
    }

    public Optional<String> extractUsername(String token) {
        try {
            return Optional.of(
                    Jwts.parser().verifyWith(key()).build()
                            .parseSignedClaims(token)
                            .getPayload()
                            .getSubject()
            );
        } catch (JwtException e) {
            return Optional.empty();
        }
    }
}
