package com.pharma.application.service;

import com.pharma.application.dto.StockAlertDto;
import com.pharma.application.dto.StockAlertsResponseDto;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.entity.StockStatus;
import com.pharma.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockAlertService {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public StockAlertsResponseDto getAlerts() {
        List<StockAlertDto> all = new ArrayList<>();
        List<StockAlertDto> expiringSoon = new ArrayList<>();
        List<StockAlertDto> expired = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(7);

        for (Stock stock : stockRepository.findAll()) {
            LocalDate exp = resolveExpirationDate(stock);
            if (stock.getStatus() == StockStatus.DEFECTIVE) {
                StockAlertDto dto = toDto(stock, exp, "Брак — продажа запрещена");
                all.add(dto);
            }
            if (stock.getStatus() == StockStatus.EXPIRED || (exp != null && exp.isBefore(today))) {
                StockAlertDto dto = toDto(stock, exp, "СРОЧНО: товар просрочен");
                all.add(dto);
                expired.add(dto);
                continue;
            }
            if (exp != null && exp.isBefore(soon)) {
                StockAlertDto dto = toDto(stock, exp, "Скоро истекает срок");
                all.add(dto);
                expiringSoon.add(dto);
            }
        }

        return new StockAlertsResponseDto(all, expiringSoon, expired);
    }

    private StockAlertDto toDto(Stock stock, LocalDate expirationDate, String message) {
        return new StockAlertDto(
                stock.getDrug().getId(),
                stock.getDrug().getName(),
                stock.getStatus(),
                expirationDate,
                message
        );
    }

    private LocalDate resolveExpirationDate(Stock stock) {
        if (stock.getExpirationDate() != null) return stock.getExpirationDate();
        if (stock.getExpiresAt() != null) return stock.getExpiresAt().atZone(ZoneId.systemDefault()).toLocalDate();
        return null;
    }
}
