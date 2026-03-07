package com.pharma.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SaleCreateRequest(@NotEmpty(message = "Список позиций не может быть пустым") @Valid List<SaleItemRequest> items) {}
