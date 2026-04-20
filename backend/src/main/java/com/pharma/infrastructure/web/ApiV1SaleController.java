package com.pharma.infrastructure.web;

import com.pharma.application.dto.ReceiptDto;
import com.pharma.application.dto.SaleRequestDto;
import com.pharma.application.dto.SaleResponseDto;
import com.pharma.application.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class ApiV1SaleController {

    private final SaleService saleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    @Operation(summary = "Оформить обычную продажу")
    public ResponseEntity<SaleResponseDto> create(@Valid @RequestBody SaleRequestDto request,
                                                  @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.createSale(request, user.getUsername()));
    }

    @PostMapping("/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    @Operation(summary = "Оформить продажу по медкарте")
    public ResponseEntity<SaleResponseDto> createByCard(@Valid @RequestBody SaleRequestDto request,
                                                        @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.createSaleByCard(request, user.getUsername()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить продажу")
    public ResponseEntity<SaleResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.findSaleResponseById(id));
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить чек продажи")
    public ResponseEntity<ReceiptDto> getReceipt(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getReceipt(id));
    }
}
