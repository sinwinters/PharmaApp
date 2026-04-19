package com.pharma.application.service;

import com.pharma.application.dto.PrescriptionCreateRequest;
import com.pharma.application.dto.PrescriptionDto;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.domain.entity.Prescription;
import com.pharma.domain.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public PrescriptionDto createPrescription(PrescriptionCreateRequest request, String username) {
        Prescription prescription = Prescription.builder()
                .patientName(request.patientName())
                .doctorName(request.doctorName())
                .issuedAt(request.issuedAt())
                .validUntil(request.validUntil())
                .verificationCode(request.verificationCode())
                .verified(false)
                .build();
        prescription = prescriptionRepository.save(prescription);
        auditLogService.log("CREATE_PRESCRIPTION", username, "Prescription", prescription.getId());
        return toDto(prescription);
    }

    @Transactional
    public PrescriptionDto verifyPrescription(String code, String username) {
        Prescription prescription = prescriptionRepository.findByVerificationCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Рецепт по коду", code));

        prescription.setVerified(code != null && !code.isBlank());
        prescription = prescriptionRepository.save(prescription);
        auditLogService.log("VERIFY_PRESCRIPTION", username, "Prescription", prescription.getId());
        return toDto(prescription);
    }

    @Transactional(readOnly = true)
    public Prescription findByIdRequired(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Рецепт", id));
    }

    @Transactional(readOnly = true)
    public Prescription findByCodeOrNull(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return prescriptionRepository.findByVerificationCode(code).orElse(null);
    }

    private PrescriptionDto toDto(Prescription p) {
        return new PrescriptionDto(
                p.getId(),
                p.getPatientName(),
                p.getDoctorName(),
                p.getIssuedAt(),
                p.getValidUntil(),
                p.getVerified(),
                p.getVerificationCode(),
                p.getCreatedAt()
        );
    }
}
