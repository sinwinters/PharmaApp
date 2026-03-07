package com.pharma.domain.strategy;

import com.pharma.domain.entity.Drug;

import java.math.BigDecimal;

public class BasePricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(Drug drug, int quantity) {
        return drug.getBasePrice().multiply(BigDecimal.valueOf(quantity));
    }
}
