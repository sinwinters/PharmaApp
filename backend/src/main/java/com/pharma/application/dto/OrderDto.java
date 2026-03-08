package com.pharma.application.dto;

import java.time.Instant;
import java.util.List;

public record OrderDto(
        Long id,
        Long supplierId,
        String supplierName,
        String status,
        Long createdBy,
        Instant createdAt,
        String destinationGln,
        String invoiceNumber,
        Instant invoiceGeneratedAt,
        Boolean autoOrder,
        List<OrderItemDto> items
) {}
