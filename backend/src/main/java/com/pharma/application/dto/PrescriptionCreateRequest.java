package com.pharma.application.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PrescriptionCreateRequest(
        @NotBlank String patientName,
        @NotBlank String doctorName,
        @NotNull LocalDate issuedAt,
        @NotNull @FutureOrPresent LocalDate validUntil,
        @NotBlank String verificationCode
) {
}
