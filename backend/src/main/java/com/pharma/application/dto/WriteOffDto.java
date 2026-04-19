package com.pharma.application.dto;

import com.pharma.domain.entity.WriteOffReason;

import java.time.LocalDateTime;

public record WriteOffDto(
        Long id,
        Long drugId,
        String drugName,
        Integer quantity,
        WriteOffReason reason,
        String comment,
        LocalDateTime createdAt
) {
}
