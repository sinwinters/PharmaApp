package com.pharma.application.dto;

public record CategoryDto(
        Long id,
        String name,
        String description,
        Boolean requiresPrescription,
        Boolean requiresStrictControl,
        Boolean requiresVerification
) {}
