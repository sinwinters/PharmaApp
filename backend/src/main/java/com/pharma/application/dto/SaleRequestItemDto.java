package com.pharma.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SaleRequestItemDto(
        @NotNull Long drugId,
        @NotNull @Min(1) Integer quantity,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal price
) {
}
