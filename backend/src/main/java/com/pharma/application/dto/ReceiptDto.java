package com.pharma.application.dto;

import com.pharma.domain.entity.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReceiptDto(
        Long saleId,
        Instant date,
        List<ReceiptItemDto> items,
        BigDecimal totalAmount,
        PaymentType paymentType,
        Boolean prescriptionSale
) {
}
