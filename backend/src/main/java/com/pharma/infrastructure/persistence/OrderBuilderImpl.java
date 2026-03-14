package com.pharma.infrastructure.persistence;

import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.domain.entity.Drug;
import com.pharma.domain.entity.Order;
import com.pharma.domain.entity.OrderItem;
import com.pharma.domain.entity.Supplier;
import com.pharma.domain.entity.User;
import com.pharma.application.port.OrderBuilder;
import com.pharma.domain.repository.DrugRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация Builder для заказа (паттерн Builder).
 */
@Component
@RequiredArgsConstructor
public class OrderBuilderImpl implements OrderBuilder {

    private final DrugRepository drugRepository;

    private Supplier supplier;
    private User createdBy;
    private final List<OrderItemEntry> entries = new ArrayList<>();

    @Override
    public OrderBuilder withSupplier(Supplier supplier) {
        this.supplier = supplier;
        return this;
    }

    @Override
    public OrderBuilder addItem(Long drugId, int quantity) {
        entries.add(new OrderItemEntry(drugId, quantity));
        return this;
    }

    @Override
    public OrderBuilder withCreatedBy(User user) {
        this.createdBy = user;
        return this;
    }

    @Override
    public Order build() {
        if (supplier == null) throw new IllegalStateException("Поставщик не указан");
        Order order = Order.builder()
                .supplier(supplier)
                .status("DRAFT")
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        for (OrderItemEntry e : entries) {
            Drug drug = drugRepository.findById(e.drugId())
                    .orElseThrow(() -> new ResourceNotFoundException("Лекарство", e.drugId()));
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .drug(drug)
                    .quantity(e.quantity())
                    .unitPrice(drug.getBasePrice())
                    .build();
            order.getItems().add(item);
        }
        entries.clear();
        supplier = null;
        createdBy = null;
        return order;
    }

    private record OrderItemEntry(Long drugId, int quantity) {}
}
