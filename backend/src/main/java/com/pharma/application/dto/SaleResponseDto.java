package com.pharma.application.dto;

import com.pharma.domain.entity.PaymentType;
import com.pharma.domain.entity.SaleStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record SaleResponseDto(
        Long id,
        BigDecimal totalAmount,
        Instant createdAt,
        PaymentType paymentType,
        String medicalCardNumber,
        Boolean isPrescriptionSale,
        SaleStatus status
) {
}
