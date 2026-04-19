package com.pharma.infrastructure.web;

import com.pharma.application.dto.BenefitProgramDto;
import com.pharma.application.dto.ReceiptDto;
import com.pharma.application.dto.SaleCreateRequest;
import com.pharma.application.dto.SaleDto;
import com.pharma.application.dto.SaleRequestDto;
import com.pharma.application.dto.SaleResponseDto;
import com.pharma.application.service.BenefitPolicyService;
import com.pharma.application.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final BenefitPolicyService benefitPolicyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'CASHIER')")
    @Operation(summary = "Провести продажу (legacy endpoint, поддерживается benefitCode для льгот РБ)")
    public ResponseEntity<SaleDto> create(@Valid @RequestBody SaleCreateRequest request,
                                          @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.createSale(request, user.getUsername()));
    }

    @PostMapping("/by-card")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    @Operation(summary = "Продажа по штрихкоду медкарты")
    public ResponseEntity<SaleResponseDto> createByCard(@Valid @RequestBody SaleRequestDto request,
                                                        @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.createSaleByCard(request, user.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'CASHIER')")
    @Operation(summary = "Список продаж с пагинацией")
    public ResponseEntity<Page<SaleDto>> list(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(saleService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить продажу по ID")
    public ResponseEntity<SaleResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.findSaleResponseById(id));
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить чек продажи")
    public ResponseEntity<ReceiptDto> getReceipt(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getReceipt(id));
    }

    @GetMapping("/benefits/rb")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'CASHIER')")
    @Operation(summary = "Справочник льгот РБ и соответствующих скидок")
    public ResponseEntity<List<BenefitProgramDto>> listRbBenefits() {
        return ResponseEntity.ok(benefitPolicyService.listPrograms());
    }
}
