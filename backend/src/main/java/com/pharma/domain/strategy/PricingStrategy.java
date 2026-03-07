package com.pharma.domain.strategy;

import com.pharma.domain.entity.Drug;

import java.math.BigDecimal;

/**
 * Паттерн Strategy: стратегия расчёта цены (базовая, со скидкой и т.д.).
 */
public interface PricingStrategy {

    BigDecimal calculatePrice(Drug drug, int quantity);
}
