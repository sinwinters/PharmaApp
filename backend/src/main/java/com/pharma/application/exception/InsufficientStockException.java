package com.pharma.application.exception;

import java.util.Optional;

public class InsufficientStockException extends PharmaException {

    public InsufficientStockException(String drugName, int requested, Optional<Integer> available) {
        super("Недостаточно товара '%s': запрошено %d, доступно %s"
                .formatted(drugName, requested, available.map(String::valueOf).orElse("0")));
    }
}
