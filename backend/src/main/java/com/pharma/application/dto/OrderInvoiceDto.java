package com.pharma.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderInvoiceDto(
        Long orderId,
        String invoiceNumber,
        Instant invoiceGeneratedAt,
        String destinationGln,
        BigDecimal totalAmount,
        List<OrderItemDto> items
) {
}
