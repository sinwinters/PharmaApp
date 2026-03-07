package com.pharma.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO запроса входа (record — требование ООП Java 14+).
 */
public record LoginRequest(
        @NotBlank(message = "Логин обязателен") String username,
        @NotBlank(message = "Пароль обязателен") String password
) {}
