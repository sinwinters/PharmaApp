package com.pharma.application.dto;

import java.math.BigDecimal;

public record DrugDto(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        Long supplierId,
        String supplierName,
        Integer minQuantity,
        String unit,
        BigDecimal basePrice,
        Integer stockQuantity,
        Boolean requiresEdsSignature,
        String edsControlType
) {}
