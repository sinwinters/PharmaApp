package com.pharma.application.dto;

public record StockDto(
        Long drugId,
        String drugName,
        Integer quantity,
        Integer minQuantity
) {
}

