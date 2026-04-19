package com.pharma.infrastructure.web;

import com.pharma.application.dto.StockAlertsResponseDto;
import com.pharma.application.service.StockAlertService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class InventoryAlertController {

    private final StockAlertService stockAlertService;

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    @Operation(summary = "Алерты по остаткам и срокам годности")
    public StockAlertsResponseDto getAlerts() {
        return stockAlertService.getAlerts();
    }
}
