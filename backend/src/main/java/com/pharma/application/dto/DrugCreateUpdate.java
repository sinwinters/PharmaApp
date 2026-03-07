package com.pharma.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record DrugCreateUpdate(
        @NotBlank @Size(max = 300) String name,
        @NotNull Long categoryId,
        @NotNull Long supplierId,
        @Min(0) Integer minQuantity,
        @Size(max = 50) String unit,
        @NotNull @DecimalMin("0") BigDecimal basePrice
) {
    public DrugCreateUpdate {
        if (minQuantity == null) minQuantity = 10;
        if (unit == null || unit.isBlank()) unit = "шт";
    }
}
