package com.pharma.application.report;

import java.math.BigDecimal;
import java.util.List;

public record SalesReportDto(
        BigDecimal totalRevenue,
        Long totalItemsSold,
        List<SalesReportItemDto> items
) {
}
