package com.pharma.application.report;

import com.pharma.domain.entity.Order;
import com.pharma.domain.entity.Sale;
import com.pharma.domain.entity.WriteOff;
import com.pharma.domain.entity.WriteOffReason;
import com.pharma.domain.repository.OrderRepository;
import com.pharma.domain.repository.SaleRepository;
import com.pharma.domain.repository.WriteOffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final SaleRepository saleRepository;
    private final OrderRepository orderRepository;
    private final WriteOffRepository writeOffRepository;

    @Override
    @Transactional(readOnly = true)
    public SalesReportDto getSalesReport(ReportFilterDto filter) {
        List<Sale> sales = saleRepository.findAll().stream()
                .filter(s -> withinDate(s.getCreatedAt(), filter.dateFrom(), filter.dateTo()))
                .toList();

        Map<Long, SalesAccumulator> acc = new HashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalItems = 0;

        for (Sale sale : sales) {
            for (var item : sale.getItems()) {
                var a = acc.computeIfAbsent(item.getDrug().getId(),
                        k -> new SalesAccumulator(item.getDrug().getId(), item.getDrugName() != null ? item.getDrugName() : item.getDrug().getName()));
                a.quantity += item.getQuantity();
                a.revenue = a.revenue.add(item.getTotal() != null ? item.getTotal() : item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                totalRevenue = totalRevenue.add(item.getTotal() != null ? item.getTotal() : item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                totalItems += item.getQuantity();
            }
        }

        List<SalesReportItemDto> items = acc.values().stream()
                .map(a -> new SalesReportItemDto(a.drugId, a.drugName, a.quantity, a.revenue))
                .sorted(Comparator.comparing(SalesReportItemDto::totalRevenue).reversed())
                .toList();

        return new SalesReportDto(totalRevenue, totalItems, items);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdersReportDto getOrdersReport(ReportFilterDto filter) {
        List<OrdersReportItemDto> orders = orderRepository.findAll().stream()
                .filter(o -> withinDate(toInstant(o.getCreatedAt()), filter.dateFrom(), filter.dateTo()))
                .map(this::toOrderItem)
                .sorted(Comparator.comparing(OrdersReportItemDto::createdAt).reversed())
                .toList();
        return new OrdersReportDto((long) orders.size(), orders);
    }

    @Override
    @Transactional(readOnly = true)
    public WriteOffReportDto getWriteOffReport(ReportFilterDto filter) {
        LocalDateTime from = filter.dateFrom() != null ? filter.dateFrom().atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime to = filter.dateTo() != null ? filter.dateTo().plusDays(1).atStartOfDay() : LocalDate.now().plusDays(1).atStartOfDay();

        List<WriteOff> writeOffs = writeOffRepository.findAllByCreatedAtBetweenOrderByCreatedAtDesc(from, to).stream()
                .filter(w -> filter.onlyExpired() == null || !filter.onlyExpired() || w.getReason() == WriteOffReason.EXPIRED)
                .filter(w -> filter.onlyDefective() == null || !filter.onlyDefective() || w.getReason() == WriteOffReason.DEFECT)
                .toList();

        List<WriteOffReportItemDto> items = writeOffs.stream()
                .map(w -> new WriteOffReportItemDto(
                        w.getId(),
                        w.getDrug().getId(),
                        w.getDrug().getName(),
                        w.getQuantity(),
                        w.getReason(),
                        w.getComment(),
                        w.getCreatedAt()
                ))
                .toList();

        int total = items.stream().mapToInt(WriteOffReportItemDto::quantity).sum();
        int expired = items.stream().filter(i -> i.reason() == WriteOffReason.EXPIRED).mapToInt(WriteOffReportItemDto::quantity).sum();
        int defect = items.stream().filter(i -> i.reason() == WriteOffReason.DEFECT).mapToInt(WriteOffReportItemDto::quantity).sum();

        return new WriteOffReportDto(total, expired, defect, items);
    }

    @Override
    @Transactional(readOnly = true)
    public MinzdravReportDto getMinzdravReport(ReportFilterDto filter) {
        Map<String, MinzdravAccumulator> map = new HashMap<>();

        List<Sale> sales = saleRepository.findAll().stream()
                .filter(s -> withinDate(s.getCreatedAt(), filter.dateFrom(), filter.dateTo()))
                .toList();

        for (Sale sale : sales) {
            for (var item : sale.getItems()) {
                var drug = item.getDrug();
                if (filter.categoryId() != null && !Objects.equals(drug.getCategory().getId(), filter.categoryId())) {
                    continue;
                }
                String country = resolveCountry(drug.getSupplier().getAddress());
                if (filter.country() != null && !filter.country().isBlank() && !filter.country().equalsIgnoreCase(country)) {
                    continue;
                }

                String key = drug.getCategory().getId() + "::" + country;
                var acc = map.computeIfAbsent(key, k -> new MinzdravAccumulator(
                        drug.getCategory().getId(),
                        drug.getCategory().getName(),
                        country
                ));
                acc.quantity += item.getQuantity();
                acc.totalAmount = acc.totalAmount.add(item.getTotal() != null ? item.getTotal() : item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        List<MinzdravReportItemDto> items = map.values().stream()
                .map(a -> new MinzdravReportItemDto(a.categoryId, a.categoryName, a.country, a.quantity, a.totalAmount))
                .sorted(Comparator.comparing(MinzdravReportItemDto::categoryName).thenComparing(MinzdravReportItemDto::country))
                .toList();

        return new MinzdravReportDto(items,
                "Технический отчет для регуляторной отчетности Минздрава РБ. Требуется юридическая валидация формата.");
    }

    @Override
    @Transactional(readOnly = true)
    public ChartDto getSalesChart(ReportFilterDto filter) {
        SalesReportDto report = getSalesReport(filter);
        return new ChartDto(
                report.items().stream().map(SalesReportItemDto::drugName).toList(),
                report.items().stream().map(i -> (Number) i.totalRevenue()).toList()
        );
    }

    private OrdersReportItemDto toOrderItem(Order o) {
        return new OrdersReportItemDto(
                o.getId(),
                o.getSupplier() != null ? o.getSupplier().getName() : "—",
                o.getStatus(),
                o.getItems() != null ? o.getItems().size() : 0,
                o.getCreatedAt()
        );
    }

    private boolean withinDate(Instant instant, LocalDate from, LocalDate to) {
        if (instant == null) {
            return false;
        }
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return checkDate(date, from, to);
    }

    private boolean checkDate(LocalDate date, LocalDate from, LocalDate to) {
        if (from != null && date.isBefore(from)) {
            return false;
        }
        return to == null || !date.isAfter(to);
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
    }

    private String resolveCountry(String supplierAddress) {
        if (supplierAddress == null || supplierAddress.isBlank()) {
            return "Belarus";
        }
        String lower = supplierAddress.toLowerCase();
        if (lower.contains("беларус") || lower.contains("belarus")) {
            return "Belarus";
        }
        if (lower.contains("росс") || lower.contains("russia")) {
            return "Russia";
        }
        return "Other";
    }

    private static class SalesAccumulator {
        private final Long drugId;
        private final String drugName;
        private long quantity = 0;
        private BigDecimal revenue = BigDecimal.ZERO;

        private SalesAccumulator(Long drugId, String drugName) {
            this.drugId = drugId;
            this.drugName = drugName;
        }
    }

    private static class MinzdravAccumulator {
        private final Long categoryId;
        private final String categoryName;
        private final String country;
        private long quantity = 0;
        private BigDecimal totalAmount = BigDecimal.ZERO;

        private MinzdravAccumulator(Long categoryId, String categoryName, String country) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.country = country;
        }
    }
}
