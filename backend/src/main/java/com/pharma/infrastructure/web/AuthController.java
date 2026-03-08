package com.pharma.infrastructure.web;

import com.pharma.application.dto.AuthResponse;
import com.pharma.application.dto.LoginRequest;
import com.pharma.application.dto.RefreshRequest;
import com.pharma.application.dto.TokenResponse;
import com.pharma.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Вход по логину и паролю")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            TokenResponse tokenResponse = authService.login(request);
            return ResponseEntity.ok(AuthResponse.fromTokenResponse(tokenResponse));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Неверный логин или пароль"));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление access token по refresh token")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }
}
