package com.pharma.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean("exchangeRateRestClient")
    public RestClient exchangeRateRestClient() {
        return RestClient.builder().build();
    }
}
