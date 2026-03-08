package com.pharma.application.dto;

import java.time.Instant;

public record RbMedicalCardVerifyResponse(
        Instant checkedAt,
        String cardNumber,
        String patientIdentifier,
        String status,
        String message
) {
}
