package com.pharma.infrastructure.scheduler;

import com.pharma.application.service.AutoOrderService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * Quartz Job для автоматического создания заказов при низком остатке.
 */
@Component
@RequiredArgsConstructor
public class AutoOrderJob implements Job {

    private final AutoOrderService autoOrderService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        autoOrderService.createOrdersForLowStock();
    }
}
