package com.pharma.application.report;

import java.math.BigDecimal;

public record SalesReportItemDto(
        Long drugId,
        String drugName,
        Long totalQuantity,
        BigDecimal totalRevenue
) {
}
