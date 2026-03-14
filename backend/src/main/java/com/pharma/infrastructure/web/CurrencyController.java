package com.pharma.infrastructure.web;

import com.pharma.infrastructure.integration.ExchangeRateClient;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Эндпоинт с использованием внешнего REST API (курсы валют).
 */
@RestController
@RequestMapping("/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final ExchangeRateClient exchangeRateClient;

    @GetMapping("/rates")
    @Operation(summary = "Курс валюты к рублю (внешний API open.er-api.com)")
    public ResponseEntity<Map<String, Object>> getRate(@RequestParam(defaultValue = "USD") String currency) {
        return exchangeRateClient.getRateToRub(currency.toUpperCase())
                .map(rate -> ResponseEntity.<Map<String, Object>>ok(Map.of(
                        "currency", currency.toUpperCase(),
                        "rateFromRub", rate
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
