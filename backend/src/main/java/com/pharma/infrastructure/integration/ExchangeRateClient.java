package com.pharma.infrastructure.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * Клиент внешнего REST API курсов валют (требование: минимум 2 внешних API).
 * Использует open.er-api.com (бесплатный, без ключа).
 */
@Slf4j
@Component
public class ExchangeRateClient {

    private static final String BASE_URL = "https://open.er-api.com/v6/latest/RUB";

    private final RestClient restClient;

    public ExchangeRateClient(@Qualifier("exchangeRateRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<BigDecimal> getRateToRub(String currencyCode) {
        try {
            var response = restClient.get()
                    .uri(BASE_URL)
                    .retrieve()
                    .body(ExchangeResponse.class);
            if (response != null && response.getRates() != null) {
                Double rate = response.getRates().get(currencyCode);
                return rate != null ? Optional.of(BigDecimal.valueOf(rate)) : Optional.empty();
            }
        } catch (Exception e) {
            log.warn("Не удалось получить курс валюты {}: {}", currencyCode, e.getMessage());
        }
        return Optional.empty();
    }

    @SuppressWarnings("unused")
    public record ExchangeResponse(String result, String base, Map<String, Double> rates) {
        public Map<String, Double> getRates() { return rates; }
    }
}
