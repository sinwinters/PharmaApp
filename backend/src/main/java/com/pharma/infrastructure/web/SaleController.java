package com.pharma.infrastructure.web;

import com.pharma.application.dto.BenefitProgramDto;
import com.pharma.application.dto.SaleCreateRequest;
import com.pharma.application.dto.SaleDto;
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
    @Operation(summary = "Провести продажу (поддерживается benefitCode для льгот РБ)")
    public ResponseEntity<SaleDto> create(@Valid @RequestBody SaleCreateRequest request,
                                          @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.createSale(request, user.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'CASHIER')")
    @Operation(summary = "Список продаж с пагинацией")
    public ResponseEntity<Page<SaleDto>> list(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(saleService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'CASHIER')")
    @Operation(summary = "Получить продажу по ID")
    public ResponseEntity<SaleDto> getById(@PathVariable Long id) {
        return saleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/benefits/rb")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'CASHIER')")
    @Operation(summary = "Справочник льгот РБ и соответствующих скидок")
    public ResponseEntity<List<BenefitProgramDto>> listRbBenefits() {
        return ResponseEntity.ok(benefitPolicyService.listPrograms());
    }
}
