package com.pharma.application.port;

import com.pharma.domain.entity.Order;
import com.pharma.domain.entity.Supplier;

import java.util.List;

/**
 * Паттерн Builder (интерфейс): построение заказа из набора позиций.
 */
public interface OrderBuilder {

    OrderBuilder withSupplier(Supplier supplier);

    OrderBuilder addItem(Long drugId, int quantity);

    OrderBuilder withCreatedBy(com.pharma.domain.entity.User user);

    Order build();
}
