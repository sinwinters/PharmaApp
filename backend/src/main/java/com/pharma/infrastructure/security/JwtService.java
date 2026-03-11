package com.pharma.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
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
    private volatile SecretKey cachedKey;

    private SecretKey key() {
        SecretKey local = cachedKey;
        if (local == null) {
            synchronized (this) {
                local = cachedKey;
                if (local == null) {
                    local = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
                    cachedKey = local;
                }
            }
        }
        return local;
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
        return parseClaims(token).map(Claims::getSubject);
    }

    public Optional<String> extractAccessUsername(String token) {
        return parseClaims(token)
                .filter(claims -> "access".equals(claims.get("type", String.class)))
                .map(Claims::getSubject);
    }

    private Optional<Claims> parseClaims(String token) {
        try {
            return Optional.of(
                    Jwts.parser().verifyWith(key()).build()
                            .parseSignedClaims(token)
                            .getPayload()
            );
        } catch (JwtException e) {
            return Optional.empty();
        }
    }
}
