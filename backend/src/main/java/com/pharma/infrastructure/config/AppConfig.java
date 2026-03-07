package com.pharma.infrastructure.config;

import com.pharma.application.service.AutoOrderService;
import com.pharma.domain.observer.LowStockNotifier;
import com.pharma.domain.strategy.PricingStrategy;
import com.pharma.domain.strategy.BasePricingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public PricingStrategy basePricingStrategy() {
        return new BasePricingStrategy();
    }

    @Bean
    public LowStockNotifier lowStockNotifier(AutoOrderService autoOrderService) {
        LowStockNotifier notifier = new LowStockNotifier();
        notifier.subscribe(autoOrderService);
        return notifier;
    }
}
