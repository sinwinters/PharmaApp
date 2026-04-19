package com.pharma.infrastructure.web;

import com.pharma.application.dto.InventoryReportDto;
import com.pharma.application.service.InventoryReportService;
import com.pharma.application.service.WriteOffService;
import com.pharma.application.report.ChartDto;
import com.pharma.application.report.ExportService;
import com.pharma.application.report.MinzdravReportDto;
import com.pharma.application.report.OrdersReportDto;
import com.pharma.application.report.ReportFilterDto;
import com.pharma.application.report.ReportService;
import com.pharma.application.report.SalesReportDto;
import com.pharma.domain.entity.StockStatus;
import com.pharma.domain.entity.WriteOffReason;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final ReportService reportService;
    private final ExportService exportService;

    @GetMapping("/write-offs")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Отчет по списаниям (legacy GET)")
    public com.pharma.application.dto.WriteOffReportDto writeOffs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) WriteOffReason reason,
            @RequestParam(required = false) Long drugId
    ) {
        return writeOffService.getReport(dateFrom, dateTo, reason, drugId);
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Отчет по складу (legacy GET)")
    public InventoryReportDto inventory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationBefore,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) StockStatus status
    ) {
        return inventoryReportService.getInventoryReport(expirationBefore, categoryId, status);
    }

    @PostMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'MANAGER')")
    public SalesReportDto salesReport(@RequestBody ReportFilterDto filter) {
        return reportService.getSalesReport(filter);
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'MANAGER')")
    public OrdersReportDto ordersReport(@RequestBody ReportFilterDto filter) {
        return reportService.getOrdersReport(filter);
    }

    @PostMapping("/writeoffs")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'MANAGER')")
    public com.pharma.application.report.WriteOffReportDto writeOffReport(@RequestBody ReportFilterDto filter) {
        return reportService.getWriteOffReport(filter);
    }

    @PostMapping("/minzdrav")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public MinzdravReportDto minzdravReport(@RequestBody ReportFilterDto filter) {
        return reportService.getMinzdravReport(filter);
    }

    @PostMapping("/sales/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'MANAGER')")
    public ResponseEntity<byte[]> exportSalesExcel(@RequestBody ReportFilterDto filter) {
        SalesReportDto report = reportService.getSalesReport(filter);
        return fileResponse(exportService.exportSalesToExcel(report),
                "sales-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @PostMapping("/sales/export/word")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'MANAGER')")
    public ResponseEntity<byte[]> exportSalesWord(@RequestBody ReportFilterDto filter) {
        SalesReportDto report = reportService.getSalesReport(filter);
        return fileResponse(exportService.exportSalesToWord(report),
                "sales-report.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @GetMapping("/sales/chart")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'MANAGER')")
    public ChartDto salesChart(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Boolean onlyExpired,
            @RequestParam(required = false) Boolean onlyDefective
    ) {
        ReportFilterDto filter = new ReportFilterDto(dateFrom, dateTo, categoryId, country, onlyExpired, onlyDefective);
        return reportService.getSalesChart(filter);
    }

    private ResponseEntity<byte[]> fileResponse(byte[] body, String fileName, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        return ResponseEntity.ok().headers(headers).body(body);
    }
}
