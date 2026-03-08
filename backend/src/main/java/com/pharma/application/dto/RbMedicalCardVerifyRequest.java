package com.pharma.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RbMedicalCardVerifyRequest(
        @NotBlank @Size(min = 6, max = 32) String cardNumber,
        @NotBlank @Size(min = 3, max = 100) String patientIdentifier
) {
}
