package com.pharma.infrastructure.web;

import com.pharma.application.dto.InventoryReportDto;
import com.pharma.application.dto.WriteOffReportDto;
import com.pharma.application.service.InventoryReportService;
import com.pharma.application.service.WriteOffService;
import com.pharma.domain.entity.StockStatus;
import com.pharma.domain.entity.WriteOffReason;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final WriteOffService writeOffService;
    private final InventoryReportService inventoryReportService;

    @GetMapping("/write-offs")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Отчет по списаниям")
    public WriteOffReportDto writeOffs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) WriteOffReason reason,
            @RequestParam(required = false) Long drugId
    ) {
        return writeOffService.getReport(dateFrom, dateTo, reason, drugId);
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Отчет по складу")
    public InventoryReportDto inventory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationBefore,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) StockStatus status
    ) {
        return inventoryReportService.getInventoryReport(expirationBefore, categoryId, status);
    }
}
