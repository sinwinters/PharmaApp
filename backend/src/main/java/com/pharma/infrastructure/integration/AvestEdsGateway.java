package com.pharma.infrastructure.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Заглушка проверки ЭЦП через Avest.
 */
@Slf4j
@Component
public class AvestEdsGateway {

    public boolean verify(String prescriptionNumber, String signature) {
        boolean valid = prescriptionNumber != null && !prescriptionNumber.isBlank()
                && signature != null && !signature.isBlank()
                && signature.length() >= 16;
        log.info("Avest verify (stub): prescription={}, valid={}", prescriptionNumber, valid);
        return valid;
    }
}
