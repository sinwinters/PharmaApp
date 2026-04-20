package com.pharma.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PrescriptionDto(
        Long id,
        String patientName,
        String doctorName,
        LocalDate issuedAt,
        LocalDate validUntil,
        Boolean verified,
        String verificationCode,
        LocalDateTime createdAt
) {
}
