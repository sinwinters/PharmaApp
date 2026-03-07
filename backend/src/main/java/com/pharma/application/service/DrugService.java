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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugRepository drugRepository;
    private final StockRepository stockRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public Page<DrugDto> findAll(Specification<Drug> spec, Pageable pageable) {
        return drugRepository.findAll(spec, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CACHE_DRUG, key = "#id")
    public Optional<DrugDto> findById(Long id) {
        return drugRepository.findByIdWithAssociations(id)
                .map(this::toDto);
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
                .build();
        drug = drugRepository.save(drug);
        Stock stock = Stock.builder().drug(drug).quantity(0).build();
        stockRepository.save(stock);
        return toDto(drugRepository.findByIdWithAssociations(drug.getId()).orElseThrow());
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
        drugRepository.save(drug);
        return toDto(drugRepository.findByIdWithAssociations(id).orElseThrow());
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_DRUG, key = "#id")
    public void delete(Long id) {
        if (!drugRepository.existsById(id)) throw new ResourceNotFoundException("Лекарство", id);
        drugRepository.deleteById(id);
    }

    private DrugDto toDto(Drug d) {
        int stockQty = stockRepository.findByDrugId(d.getId()).map(Stock::getQuantity).orElse(0);
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
                stockQty
        );
    }
}
