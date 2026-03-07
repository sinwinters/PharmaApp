package com.pharma.application.dto;

public record TokenResponse(String accessToken, String refreshToken, long expiresIn) {}
