package com.pharma.infrastructure.scheduler;

import com.pharma.application.service.WriteOffService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockWriteOffJob implements Job {

    private final WriteOffService writeOffService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        writeOffService.autoWriteOffExpiredAndDefective();
    }
}
