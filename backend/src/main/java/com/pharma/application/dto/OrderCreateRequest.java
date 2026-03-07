package com.pharma.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderCreateRequest(
        @NotNull Long supplierId,
        @NotEmpty @Valid List<OrderItemRequest> items
) {}
