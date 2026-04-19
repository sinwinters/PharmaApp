package com.pharma.application.dto;

import java.math.BigDecimal;

public record OrderDetailsItemDto(
        String drugName,
        Integer quantity,
        BigDecimal unitPrice
) {
}
