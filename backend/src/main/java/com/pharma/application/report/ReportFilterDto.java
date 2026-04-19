package com.pharma.application.report;

import java.time.LocalDate;

public record ReportFilterDto(
        LocalDate dateFrom,
        LocalDate dateTo,
        Long categoryId,
        String country,
        Boolean onlyExpired,
        Boolean onlyDefective
) {
}
