package com.pharma.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleDto(
        Long id,
        Long userId,
        String username,
        BigDecimal totalAmount,
        BigDecimal totalBeforeDiscount,
        BigDecimal discountAmount,
        String benefitCode,
        String benefitLawReference,
        Boolean edsRequired,
        Boolean edsValidated,
        String edsProvider,
        String prescriptionNumber,
        Instant createdAt,
        List<SaleItemDto> items
) {}
