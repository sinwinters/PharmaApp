package com.pharma.application.report;

import com.pharma.domain.entity.WriteOffReason;

import java.time.LocalDateTime;

public record WriteOffReportItemDto(
        Long writeOffId,
        Long drugId,
        String drugName,
        Integer quantity,
        WriteOffReason reason,
        String comment,
        LocalDateTime createdAt
) {
}
