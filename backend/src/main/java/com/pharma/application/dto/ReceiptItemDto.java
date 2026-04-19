package com.pharma.application.dto;

import java.math.BigDecimal;

public record ReceiptItemDto(
        String drugName,
        Integer quantity,
        BigDecimal price,
        BigDecimal total
) {
}
