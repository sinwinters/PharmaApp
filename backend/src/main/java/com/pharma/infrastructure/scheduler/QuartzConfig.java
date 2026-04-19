package com.pharma.infrastructure.scheduler;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Value("${app.auto-order.cron:0 0 2 * * ?}")
    private String autoOrderCron;

    @Value("${app.stock-writeoff.cron:0 30 2 * * ?}")
    private String stockWriteOffCron;

    @Bean
    public JobDetail autoOrderJobDetail() {
        return JobBuilder.newJob(AutoOrderJob.class)
                .withIdentity("autoOrderJob", "pharma")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger autoOrderTrigger(JobDetail autoOrderJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(autoOrderJobDetail)
                .withIdentity("autoOrderTrigger", "pharma")
                .withSchedule(CronScheduleBuilder.cronSchedule(autoOrderCron))
                .build();
    }

    @Bean
    public JobDetail stockWriteOffJobDetail() {
        return JobBuilder.newJob(StockWriteOffJob.class)
                .withIdentity("stockWriteOffJob", "pharma")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger stockWriteOffTrigger(JobDetail stockWriteOffJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(stockWriteOffJobDetail)
                .withIdentity("stockWriteOffTrigger", "pharma")
                .withSchedule(CronScheduleBuilder.cronSchedule(stockWriteOffCron))
                .build();
    }
}
