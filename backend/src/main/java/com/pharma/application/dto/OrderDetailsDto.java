package com.pharma.application.dto;

import com.pharma.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailsDto(
        Long id,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderDetailsItemDto> items
) {
}
