package com.pharma.application.service;

import com.pharma.application.dto.BelarusMinzdravReportDto;
import com.pharma.application.dto.DrugAnalyticsDto;
import com.pharma.application.dto.TopDrugStatDto;
import com.pharma.domain.repository.SaleRepository;
import com.pharma.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SaleRepository saleRepository;
    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public DrugAnalyticsDto getDrugAnalytics(int periodDays) {
        int days = normalizeDays(periodDays);
        Instant to = Instant.now();
        Instant from = to.minus(days, ChronoUnit.DAYS);

        long salesCount = saleRepository.countByCreatedAtBetween(from, to);
        BigDecimal revenue = saleRepository.sumTotalAmountByPeriod(from, to);
        BigDecimal averageCheck = salesCount == 0
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(salesCount), 2, RoundingMode.HALF_UP);

        List<TopDrugStatDto> topDrugs = saleRepository.findTopDrugsByPeriod(from, to, PageRequest.of(0, 10))
                .stream()
                .map(v -> new TopDrugStatDto(v.getDrugId(), v.getDrugName(), v.getTotalQuantity()))
                .toList();

        return new DrugAnalyticsDto(
                to,
                days,
                salesCount,
                revenue,
                averageCheck,
                stockRepository.countCriticalStock(),
                topDrugs
        );
    }

    @Transactional(readOnly = true)
    public BelarusMinzdravReportDto generateBelarusMinzdravReport(int periodDays) {
        DrugAnalyticsDto a = getDrugAnalytics(periodDays);
        return new BelarusMinzdravReportDto(
                Instant.now(),
                a.periodDays(),
                "Постановления Минздрава РБ (шаблон контроля, требуется юридическая валидация)",
                a.salesCount(),
                a.criticalStockCount(),
                true,
                List.of(
                        "Контроль сроков годности и серий ЛС",
                        "Проверка корректности рецептурного отпуска",
                        "Учет льготного обеспечения и подтверждающих документов",
                        "Журналирование отпусков препаратов предметно-количественного учета"
                ),
                "Это технический шаблон отчета. Для официальной регуляторной отчетности необходима адаптация под актуальные НПА и формат выгрузки Минздрава РБ."
        );
    }

    private int normalizeDays(int periodDays) {
        if (periodDays <= 0) return 30;
        return Math.min(periodDays, 365);
    }
}
