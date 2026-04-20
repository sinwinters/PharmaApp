package com.pharma.application.dto;

import com.pharma.domain.entity.StockStatus;

import java.time.LocalDate;

public record InventoryItemReportDto(
        Long stockId,
        Long drugId,
        String drugName,
        String categoryName,
        Integer quantity,
        Integer reservedQuantity,
        StockStatus status,
        LocalDate expirationDate
) {
}
