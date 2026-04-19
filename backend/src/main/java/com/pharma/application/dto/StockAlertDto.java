package com.pharma.application.dto;

import com.pharma.domain.entity.StockStatus;

import java.time.LocalDate;

public record StockAlertDto(
        Long drugId,
        String drugName,
        StockStatus status,
        LocalDate expirationDate,
        String message
) {
}
