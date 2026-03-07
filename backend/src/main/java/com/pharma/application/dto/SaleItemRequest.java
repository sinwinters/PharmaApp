package com.pharma.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaleItemRequest(@NotNull Long drugId, @Min(1) Integer quantity) {}
