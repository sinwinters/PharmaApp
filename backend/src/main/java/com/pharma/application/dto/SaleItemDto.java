package com.pharma.application.dto;

import java.math.BigDecimal;

public record SaleItemDto(
        Long drugId,
        String drugName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalBeforeDiscount,
        BigDecimal discountPercent,
        BigDecimal total
) {}
