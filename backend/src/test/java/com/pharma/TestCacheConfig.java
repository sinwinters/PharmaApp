package com.pharma;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestCacheConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "cacheManager")
    public CacheManager noOpCacheManager() {
        return new NoOpCacheManager();
    }
}
