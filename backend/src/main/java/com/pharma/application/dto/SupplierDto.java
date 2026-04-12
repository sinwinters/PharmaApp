package com.pharma.application.dto;

public record SupplierDto(Long id,
                          String name,
                          String unp,
                          String gln,
                          String address,
                          String contactInfo,
                          String email,
                          String phone) {
}
