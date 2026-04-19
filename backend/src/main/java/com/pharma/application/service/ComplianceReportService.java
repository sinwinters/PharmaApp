package com.pharma.application.service;

import com.pharma.application.dto.ComplianceMinzdravReportDto;
import com.pharma.domain.entity.Sale;
import com.pharma.domain.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ComplianceReportService {

    private final SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public List<ComplianceMinzdravReportDto> buildMinzdravReport(LocalDate from,
                                                                  LocalDate to,
                                                                  Long categoryId,
                                                                  Boolean requiresStrictControl,
                                                                  String country) {
        return saleRepository.findAll().stream()
                .filter(s -> inRange(s.getCreatedAt(), from, to))
                .flatMap((Sale sale) -> sale.getItems().stream().map(item -> new ReportRow(sale, item)))
                .filter(r -> categoryId == null || Objects.equals(r.item().getDrug().getCategory().getId(), categoryId))
                .filter(r -> requiresStrictControl == null || Objects.equals(r.item().getDrug().getCategory().getRequiresStrictControl(), requiresStrictControl))
                .filter(r -> country == null || country.isBlank() || resolveCountry(r.item().getDrug().getSupplier().getAddress()).equalsIgnoreCase(country))
                .map(r -> new ComplianceMinzdravReportDto(
                        r.item().getDrugName() != null ? r.item().getDrugName() : r.item().getDrug().getName(),
                        r.item().getDrug().getCategory().getName(),
                        r.item().getQuantity(),
                        r.sale().getCreatedAt(),
                        r.sale().getPrescription() != null,
                        r.sale().getPrescription() != null && Boolean.TRUE.equals(r.sale().getPrescription().getVerified()),
                        resolveCountry(r.item().getDrug().getSupplier().getAddress())
                ))
                .toList();
    }

    private boolean inRange(Instant instant, LocalDate from, LocalDate to) {
        if (instant == null) return false;
        LocalDate d = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        if (from != null && d.isBefore(from)) return false;
        return to == null || !d.isAfter(to);
    }

    private String resolveCountry(String supplierAddress) {
        if (supplierAddress == null || supplierAddress.isBlank()) {
            return "Belarus";
        }
        String lower = supplierAddress.toLowerCase();
        if (lower.contains("беларус") || lower.contains("belarus")) return "Belarus";
        if (lower.contains("росс") || lower.contains("russia")) return "Russia";
        return "Other";
    }

    private record ReportRow(Sale sale, com.pharma.domain.entity.SaleItem item) {}
}
