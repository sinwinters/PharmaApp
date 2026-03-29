package com.pharma.infrastructure.web;

import com.pharma.application.dto.StockDto;
import com.pharma.application.dto.StockUpdateRequest;
import com.pharma.application.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Список остатков по лекарствам")
    public ResponseEntity<Page<StockDto>> list(Pageable pageable) {
        return ResponseEntity.ok(stockService.findAll(pageable));
    }

    @GetMapping("/{drugId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить остаток по лекарству")
    public ResponseEntity<StockDto> getByDrug(@PathVariable Long drugId) {
        return ResponseEntity.ok(stockService.findByDrugId(drugId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать запись об остатке для лекарства")
    public ResponseEntity<StockDto> create(@Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.create(request));
    }

    @PutMapping("/{drugId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить количество остатка по лекарству")
    public ResponseEntity<StockDto> update(@PathVariable Long drugId,
                                           @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(stockService.update(drugId, request));
    }

    @DeleteMapping("/{drugId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить запись об остатке по лекарству")
    public ResponseEntity<Void> delete(@PathVariable Long drugId) {
        stockService.delete(drugId);
        return ResponseEntity.noContent().build();
    }
}

