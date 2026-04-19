package com.pharma.infrastructure.web;

import com.pharma.application.dto.PrescriptionCreateRequest;
import com.pharma.application.dto.PrescriptionDto;
import com.pharma.application.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    @Operation(summary = "Создать рецепт")
    public ResponseEntity<PrescriptionDto> create(@Valid @RequestBody PrescriptionCreateRequest request,
                                                  @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.createPrescription(request, user != null ? user.getUsername() : "system"));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    @Operation(summary = "Верифицировать рецепт по коду (заглушка ЭЦП)")
    public ResponseEntity<PrescriptionDto> verify(@RequestParam String code,
                                                  @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(prescriptionService.verifyPrescription(code, user != null ? user.getUsername() : "system"));
    }
}
