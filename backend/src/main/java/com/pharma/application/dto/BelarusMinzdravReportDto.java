package com.pharma.application.dto;

import java.time.Instant;
import java.util.List;

public record BelarusMinzdravReportDto(
        Instant generatedAt,
        int periodDays,
        String regulationReference,
        long salesAnalyzed,
        long criticalStockPositions,
        boolean requiresElectronicPrescriptionControl,
        List<String> mandatoryControlPoints,
        String disclaimer
) {
}
