package com.pharma.application.dto;

import java.math.BigDecimal;

public record OrderItemDto(Long id, Long drugId, String drugName, Integer quantity, BigDecimal unitPrice) {}
