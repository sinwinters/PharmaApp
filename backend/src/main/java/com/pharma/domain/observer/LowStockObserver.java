package com.pharma.domain.observer;

import com.pharma.domain.entity.Drug;

import java.util.Optional;

/**
 * Паттерн Observer: наблюдатель за низким остатком (для автозаказа).
 */
@FunctionalInterface
public interface LowStockObserver {

    void onLowStock(Drug drug, int currentQuantity, int minQuantity);
}
