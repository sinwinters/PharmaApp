package com.pharma.application.service;

import com.pharma.application.dto.DrugCreateUpdate;
import com.pharma.application.dto.DrugDto;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.domain.entity.Category;
import com.pharma.domain.entity.Drug;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.repository.CategoryRepository;
import com.pharma.domain.repository.DrugRepository;
import com.pharma.domain.repository.SupplierRepository;
import com.pharma.domain.repository.StockRepository;
import com.pharma.infrastructure.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugRepository drugRepository;
    private final StockRepository stockRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public Page<DrugDto> findAll(Specification<Drug> spec, Pageable pageable) {
        Page<Drug> page = drugRepository.findAll(spec, pageable);
        Map<Long, Integer> stockByDrugId = loadStockMap(page.getContent());
        return page.map(drug -> toDto(drug, stockByDrugId));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CACHE_DRUG, key = "#id")
    public Optional<DrugDto> findById(Long id) {
        return drugRepository.findByIdWithAssociations(id)
                .map(drug -> toDto(drug, Map.of(drug.getId(), stockRepository.findByDrugId(drug.getId()).map(Stock::getQuantity).orElse(0))));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_DRUG, allEntries = true)
    public DrugDto create(DrugCreateUpdate dto) {
        Category cat = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Категория", dto.categoryId()));
        var supplier = supplierRepository.findById(dto.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Поставщик", dto.supplierId()));
        Drug drug = Drug.builder()
                .name(dto.name())
                .category(cat)
                .supplier(supplier)
                .minQuantity(dto.minQuantity())
                .unit(dto.unit())
                .basePrice(dto.basePrice())
                .requiresEdsSignature(dto.requiresEdsSignature())
                .edsControlType(dto.edsControlType())
                .build();
        drug = drugRepository.save(drug);
        Stock stock = Stock.builder().drug(drug).quantity(0).build();
        stockRepository.save(stock);
        Drug saved = drugRepository.findByIdWithAssociations(drug.getId()).orElseThrow();
        return toDto(saved, Map.of(saved.getId(), 0));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_DRUG, key = "#id")
    public DrugDto update(Long id, DrugCreateUpdate dto) {
        Drug drug = drugRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Лекарство", id));
        Category cat = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Категория", dto.categoryId()));
        var supplier = supplierRepository.findById(dto.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Поставщик", dto.supplierId()));
        drug.setName(dto.name());
        drug.setCategory(cat);
        drug.setSupplier(supplier);
        drug.setMinQuantity(dto.minQuantity());
        drug.setUnit(dto.unit());
        drug.setBasePrice(dto.basePrice());
        drug.setRequiresEdsSignature(dto.requiresEdsSignature());
        drug.setEdsControlType(dto.edsControlType());
        drugRepository.save(drug);
        Drug updated = drugRepository.findByIdWithAssociations(id).orElseThrow();
        int stockQty = stockRepository.findByDrugId(updated.getId()).map(Stock::getQuantity).orElse(0);
        return toDto(updated, Map.of(updated.getId(), stockQty));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_DRUG, key = "#id")
    public void delete(Long id) {
        if (!drugRepository.existsById(id)) throw new ResourceNotFoundException("Лекарство", id);
        drugRepository.deleteById(id);
    }

    private Map<Long, Integer> loadStockMap(List<Drug> drugs) {
        if (drugs.isEmpty()) return Map.of();
        List<Long> ids = drugs.stream().map(Drug::getId).toList();
        return stockRepository.findAllByDrugIdIn(ids).stream()
                .collect(Collectors.toMap(s -> s.getDrug().getId(), Stock::getQuantity, (a, b) -> b));
    }

    private DrugDto toDto(Drug d, Map<Long, Integer> stockByDrugId) {
        int stockQty = stockByDrugId.getOrDefault(d.getId(), 0);
        return new DrugDto(
                d.getId(),
                d.getName(),
                d.getCategory().getId(),
                d.getCategory().getName(),
                d.getSupplier().getId(),
                d.getSupplier().getName(),
                d.getMinQuantity(),
                d.getUnit(),
                d.getBasePrice(),
                stockQty,
                d.getRequiresEdsSignature(),
                d.getEdsControlType()
        );
    }
}
