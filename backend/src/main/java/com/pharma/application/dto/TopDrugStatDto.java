package com.pharma.application.dto;

public record TopDrugStatDto(
        Long drugId,
        String drugName,
        Long totalQuantity
) {
}
