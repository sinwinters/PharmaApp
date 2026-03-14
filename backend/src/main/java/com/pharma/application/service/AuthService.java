package com.pharma.application.service;

import com.pharma.application.dto.LoginRequest;
import com.pharma.application.dto.TokenResponse;
import com.pharma.domain.repository.UserRepository;
import com.pharma.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        return userRepository.findByUsername(request.username())
                .map(u -> {
                    String access = jwtService.createAccessToken(u.getUsername(), u.getRole().getName());
                    String refresh = jwtService.createRefreshToken(u.getUsername());
                    return new TokenResponse(access, refresh, 3600L);
                })
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Неверный логин или пароль"));
    }

    public Optional<TokenResponse> refresh(String refreshToken) {
        return jwtService.extractUsername(refreshToken)
                .flatMap(userRepository::findByUsername)
                .map(u -> new TokenResponse(
                        jwtService.createAccessToken(u.getUsername(), u.getRole().getName()),
                        jwtService.createRefreshToken(u.getUsername()),
                        3600L
                ));
    }
}
