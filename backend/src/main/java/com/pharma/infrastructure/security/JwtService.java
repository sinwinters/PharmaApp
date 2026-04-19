package com.pharma.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
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
                    local = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                            props.getSecret().getBytes(StandardCharsets.UTF_8)
                    );
                    cachedKey = local;
                }
            }
        }
        return local;
    }

    // ACCESS TOKEN
    public String createAccessToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + props.getAccessTtl()))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // REFRESH TOKEN
    public String createRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + props.getRefreshTtl()))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ FIX: универсальное извлечение username (ТО, ЧЕГО НЕ ХВАТАЛО)
    public Optional<String> extractUsername(String token) {
        return parseClaims(token).map(Claims::getSubject);
    }

    public Optional<String> extractAccessUsername(String token) {
        return parseClaims(token)
                .filter(c -> "access".equals(c.get("type", String.class)))
                .map(Claims::getSubject);
    }

    public Optional<String> extractAccessRole(String token) {
        return parseClaims(token)
                .filter(c -> "access".equals(c.get("type", String.class)))
                .map(c -> c.get("role", String.class))
                .filter(r -> r != null && !r.isBlank());
    }

    private Optional<Claims> parseClaims(String token) {
        try {
            return Optional.of(
                    Jwts.parserBuilder()
                            .setSigningKey(key())
                            .build()
                            .parseClaimsJws(token)
                            .getBody()
            );
        } catch (JwtException e) {
            return Optional.empty();
        }
    }
}