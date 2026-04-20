package com.pharma.application.dto;

import com.pharma.domain.entity.PaymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaleRequestDto(
        @NotEmpty @Valid List<SaleRequestItemDto> items,
        @NotNull PaymentType paymentType,
        @Size(min = 6, message = "medicalCardNumber должен содержать минимум 6 символов") String medicalCardNumber,
        Long prescriptionId
) {
}
