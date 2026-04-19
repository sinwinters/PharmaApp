package com.pharma.application.report;

import java.util.List;

public record OrdersReportDto(
        Long totalOrders,
        List<OrdersReportItemDto> orders
) {
}
