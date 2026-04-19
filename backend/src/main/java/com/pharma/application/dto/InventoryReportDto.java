package com.pharma.application.dto;

import java.util.List;

public record InventoryReportDto(
        List<InventoryItemReportDto> items,
        Integer totalQuantity,
        Integer totalReserved
) {
}
