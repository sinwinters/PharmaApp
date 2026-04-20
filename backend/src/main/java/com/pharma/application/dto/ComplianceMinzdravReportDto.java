package com.pharma.application.dto;

import java.time.Instant;

public record ComplianceMinzdravReportDto(
        String drugName,
        String categoryName,
        Integer quantity,
        Instant saleDate,
        Boolean prescriptionUsed,
        Boolean verified,
        String country
) {
}
