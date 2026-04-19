package com.pharma.application.dto;

import java.util.List;

public record WriteOffReportDto(
        List<WriteOffDto> items,
        Integer totalQuantity,
        Integer totalExpired,
        Integer totalDefect
) {
}
