package com.pharma.application.dto;

public record AuthResponse(
        String token,
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
    public static AuthResponse fromTokenResponse(TokenResponse response) {
        return new AuthResponse(
                response.accessToken(),
                response.accessToken(),
                response.refreshToken(),
                response.expiresIn()
        );
    }
}
