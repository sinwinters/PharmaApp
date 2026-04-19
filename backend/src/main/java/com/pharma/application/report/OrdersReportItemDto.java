package com.pharma.application.report;

import com.pharma.domain.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrdersReportItemDto(
        Long orderId,
        String supplierName,
        OrderStatus status,
        Integer itemsCount,
        LocalDateTime createdAt
) {
}
