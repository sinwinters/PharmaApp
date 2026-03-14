package com.pharma.application.dto;

import java.math.BigDecimal;

public record BenefitProgramDto(
        String code,
        String title,
        String lawReference,
        BigDecimal discountPercent,
        String description
) {
}
