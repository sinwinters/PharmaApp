package com.pharma.application.service;

import com.pharma.application.port.OrderBuilder;
import com.pharma.domain.entity.Drug;
import com.pharma.domain.observer.LowStockObserver;
import com.pharma.domain.repository.DrugRepository;
import com.pharma.domain.repository.OrderRepository;
import com.pharma.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Модуль автоматического заказа: наблюдатель низкого остатка + отложенная задача по расписанию.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoOrderService implements LowStockObserver {

    private final DrugRepository drugRepository;
    private final StockRepository stockRepository;
    private final OrderRepository orderRepository;
    private final OrderBuilder orderBuilder;

    @Value("${app.auto-order.enabled:true}")
    private boolean enabled;

    @Override
    @Transactional
    public void onLowStock(Drug drug, int currentQuantity, int minQuantity) {
        log.debug("Низкий остаток: {} (текущий: {}, мин: {})", drug.getName(), currentQuantity, minQuantity);
        // Можно поставить задачу в очередь или только логировать; создание заказа — по крону
    }

    /**
     * Проверяет остатки и создаёт заказы по поставщикам (вызывается Quartz Job по расписанию).
     */
    @Transactional
    public void createOrdersForLowStock() {
        if (!enabled) return;
        List<Drug> lowStock = drugRepository.findAllWithStockBelowMinimum();
        if (lowStock.isEmpty()) return;
        Map<Long, List<Drug>> bySupplier = lowStock.stream().collect(Collectors.groupingBy(d -> d.getSupplier().getId()));
for (Map.Entry<Long, List<Drug>> e : bySupplier.entrySet()) {
                var supplier = e.getValue().get(0).getSupplier();
            OrderBuilder b = orderBuilder.withSupplier(supplier);
            for (Drug d : e.getValue()) {
                int current = stockRepository.findByDrugId(d.getId()).map(s -> s.getQuantity()).orElse(0);
                int toOrder = Math.max(d.getMinQuantity() * 2 - current, d.getMinQuantity());
                b.addItem(d.getId(), toOrder);
            }
            var order = b.build();
            if (!order.getItems().isEmpty()) {
                orderRepository.save(order);
                log.info("Создан автозаказ #{} поставщику {}", order.getId(), supplier.getName());
            }
        }
    }
}
