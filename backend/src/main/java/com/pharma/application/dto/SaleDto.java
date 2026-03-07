package com.pharma.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleDto(Long id, Long userId, String username, BigDecimal totalAmount, Instant createdAt, List<SaleItemDto> items) {}
