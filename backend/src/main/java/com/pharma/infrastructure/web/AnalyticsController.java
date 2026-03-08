package com.pharma.infrastructure.web;

import com.pharma.application.dto.BelarusMinzdravReportDto;
import com.pharma.application.dto.DrugAnalyticsDto;
import com.pharma.application.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/drugs")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    @Operation(summary = "Аналитика по лекарствам: выручка, продажи, топ позиций, критические остатки")
    public DrugAnalyticsDto drugAnalytics(@RequestParam(defaultValue = "30") int periodDays) {
        return analyticsService.getDrugAnalytics(periodDays);
    }

    @GetMapping("/reports/minzdrav-rb")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Формирование шаблонного отчета по контрольным требованиям Минздрава РБ")
    public BelarusMinzdravReportDto minzdravRbReport(@RequestParam(defaultValue = "30") int periodDays) {
        return analyticsService.generateBelarusMinzdravReport(periodDays);
    }
}
