package com.pharma.infrastructure.web;

import com.pharma.application.dto.DrugCreateUpdate;
import com.pharma.application.dto.DrugDto;
import com.pharma.application.service.DrugService;
import com.pharma.infrastructure.persistence.DrugSpecification;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drugs")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;

    @GetMapping
    @Operation(summary = "Список лекарств с пагинацией и фильтрами")
    public ResponseEntity<Page<DrugDto>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Specification<?> spec = DrugSpecification.filter(name, categoryId, supplierId);
        return ResponseEntity.ok(drugService.findAll((Specification<com.pharma.domain.entity.Drug>) spec, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить лекарство по ID")
    public ResponseEntity<DrugDto> getById(@PathVariable Long id) {
        return drugService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать лекарство")
    public ResponseEntity<DrugDto> create(@Valid @RequestBody DrugCreateUpdate dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(drugService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить лекарство")
    public ResponseEntity<DrugDto> update(@PathVariable Long id, @Valid @RequestBody DrugCreateUpdate dto) {
        return drugService.findById(id).isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(drugService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить лекарство")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        drugService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
