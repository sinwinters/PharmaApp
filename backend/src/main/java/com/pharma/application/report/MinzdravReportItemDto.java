package com.pharma.application.report;

import java.math.BigDecimal;

public record MinzdravReportItemDto(
        Long categoryId,
        String categoryName,
        String country,
        Long quantity,
        BigDecimal totalAmount
) {
}
