package com.pharma.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;
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

    // Кешируем ключ для подписи
    private volatile SecretKey cachedKey;

    private SecretKey key() {
        SecretKey local = cachedKey;
        if (local == null) {
            synchronized (this) {
                local = cachedKey;
                if (local == null) {
                    local = io.jsonwebtoken.security.Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
                    cachedKey = local;
                }
            }
        }
        return local;
    }

    // Генерация access токена
    public String createAccessToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + props.getAccessTtl()))
                .signWith(SignatureAlgorithm.HS256, key())
                .compact();
    }

    // Генерация refresh токена
    public String createRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + props.getRefreshTtl()))
                .signWith(SignatureAlgorithm.HS256, key())
                .compact();
    }

    // Извлекаем username любого токена
    public Optional<String> extractUsername(String token) {
        return parseClaims(token).map(Claims::getSubject);
    }

    // Извлекаем username только access токена
    public Optional<String> extractAccessUsername(String token) {
        return parseClaims(token)
                .filter(claims -> "access".equals(claims.get("type", String.class)))
                .map(Claims::getSubject);
    }

    // Разбор токена
    private Optional<Claims> parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key())
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.of(claims);
        } catch (JwtException e) {
            return Optional.empty();
        }
    }
}