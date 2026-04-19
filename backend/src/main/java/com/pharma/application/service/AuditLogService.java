package com.pharma.application.service;

import com.pharma.domain.entity.AuditLog;
import com.pharma.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String action, String username, String entityType, Long entityId) {
        auditLogRepository.save(AuditLog.builder()
                .action(action)
                .username(username == null ? "system" : username)
                .entityType(entityType)
                .entityId(entityId)
                .build());
    }
}
