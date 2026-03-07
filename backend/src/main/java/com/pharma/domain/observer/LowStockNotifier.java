package com.pharma.domain.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Субъект для наблюдателей низкого остатка (паттерн Observer).
 */
public class LowStockNotifier {

    private final List<LowStockObserver> observers = new ArrayList<>();

    public void subscribe(LowStockObserver observer) {
        observers.add(observer);
    }

    public void notifyLowStock(com.pharma.domain.entity.Drug drug, int currentQuantity, int minQuantity) {
        observers.forEach(o -> o.onLowStock(drug, currentQuantity, minQuantity));
    }
}
