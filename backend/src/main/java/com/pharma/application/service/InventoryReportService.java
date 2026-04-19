package com.pharma.application.service;

import com.pharma.application.dto.InventoryItemReportDto;
import com.pharma.application.dto.InventoryReportDto;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.entity.StockStatus;
import com.pharma.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryReportService {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public InventoryReportDto getInventoryReport(LocalDate expirationBefore,
                                                 Long categoryId,
                                                 StockStatus status) {
        List<InventoryItemReportDto> items = stockRepository.findAll().stream()
                .filter(s -> expirationBefore == null || (s.getExpirationDate() != null && !s.getExpirationDate().isAfter(expirationBefore)))
                .filter(s -> categoryId == null || s.getDrug().getCategory().getId().equals(categoryId))
                .filter(s -> status == null || s.getStatus() == status)
                .map(this::toDto)
                .toList();

        int totalQuantity = items.stream().mapToInt(InventoryItemReportDto::quantity).sum();
        int totalReserved = items.stream().mapToInt(i -> i.reservedQuantity() == null ? 0 : i.reservedQuantity()).sum();

        return new InventoryReportDto(items, totalQuantity, totalReserved);
    }

    private InventoryItemReportDto toDto(Stock s) {
        return new InventoryItemReportDto(
                s.getId(),
                s.getDrug().getId(),
                s.getDrug().getName(),
                s.getDrug().getCategory().getName(),
                s.getQuantity(),
                s.getReservedQuantity(),
                s.getStatus(),
                s.getExpirationDate()
        );
    }
}
