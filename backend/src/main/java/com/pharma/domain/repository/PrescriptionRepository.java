package com.pharma.domain.repository;

import com.pharma.domain.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByVerificationCode(String verificationCode);
}
