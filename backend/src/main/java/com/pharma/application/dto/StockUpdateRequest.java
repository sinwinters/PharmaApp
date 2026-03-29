package com.pharma.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(
        @NotNull Long drugId,
        @NotNull @Min(0) Integer quantity
) {
}

