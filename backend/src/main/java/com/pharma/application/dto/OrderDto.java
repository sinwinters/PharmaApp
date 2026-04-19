package com.pharma.application.dto;

import com.pharma.domain.entity.OrderStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Long id,
        Long supplierId,
        String supplierName,
        OrderStatus status,
        Long createdBy,
        LocalDateTime createdAt,
        String destinationGln,
        String invoiceNumber,
        Instant invoiceGeneratedAt,
        Boolean autoOrder,
        List<OrderItemDto> items
) {}
