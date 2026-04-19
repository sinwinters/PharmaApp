package com.pharma.application.dto;

import java.util.List;

public record StockAlertsResponseDto(
        List<StockAlertDto> alerts,
        List<StockAlertDto> expiringSoon,
        List<StockAlertDto> expired
) {
}
