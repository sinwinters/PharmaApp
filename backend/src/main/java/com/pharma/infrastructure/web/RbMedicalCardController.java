package com.pharma.infrastructure.web;

import com.pharma.application.dto.RbMedicalCardVerifyRequest;
import com.pharma.application.dto.RbMedicalCardVerifyResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/integrations/rb-medical-card")
public class RbMedicalCardController {

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'CASHIER')")
    @Operation(summary = "Заглушка верификации карты медицинского обслуживания РБ")
    public ResponseEntity<RbMedicalCardVerifyResponse> verify(@Valid @RequestBody RbMedicalCardVerifyRequest request) {
        return ResponseEntity.accepted().body(new RbMedicalCardVerifyResponse(
                Instant.now(),
                request.cardNumber(),
                request.patientIdentifier(),
                "STUB_ACCEPTED",
                "Интеграция с государственной системой РБ пока не подключена. Запрос принят как заглушка."
        ));
    }
}
