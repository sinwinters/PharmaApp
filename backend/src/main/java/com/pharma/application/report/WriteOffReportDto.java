package com.pharma.application.report;

import java.util.List;

public record WriteOffReportDto(
        Integer totalWriteOffQuantity,
        Integer expiredQuantity,
        Integer defectiveQuantity,
        List<WriteOffReportItemDto> items
) {
}
