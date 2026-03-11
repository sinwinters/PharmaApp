package com.pharma.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DrugAnalyticsDto(
        Instant generatedAt,
        int periodDays,
        long salesCount,
        BigDecimal revenue,
        BigDecimal averageCheck,
        long criticalStockCount,
        List<TopDrugStatDto> topDrugs
) {
}
