package com.pharma.application.report;

import java.util.List;

public record MinzdravReportDto(
        List<MinzdravReportItemDto> items,
        String note
) {
}
