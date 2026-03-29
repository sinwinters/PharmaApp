package com.pharma.application.service;

import com.pharma.application.dto.StockDto;
import com.pharma.application.dto.StockUpdateRequest;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.domain.entity.Drug;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.repository.DrugRepository;
import com.pharma.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final DrugRepository drugRepository;

    @Transactional(readOnly = true)
    public Page<StockDto> findAll(Pageable pageable) {
        return stockRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public StockDto findByDrugId(Long drugId) {
        Stock stock = stockRepository.findByDrugId(drugId)
                .orElseThrow(() -> new ResourceNotFoundException("Остаток", drugId));
        return toDto(stock);
    }

    @Transactional
    public StockDto create(StockUpdateRequest request) {
        Drug drug = drugRepository.findById(request.drugId())
                .orElseThrow(() -> new ResourceNotFoundException("Лекарство", request.drugId()));
        stockRepository.findByDrugId(drug.getId())
                .ifPresent(s -> {
                    throw new IllegalStateException("Для данного лекарства остаток уже существует");
                });
        Stock stock = Stock.builder()
                .drug(drug)
                .quantity(request.quantity())
                .build();
        stock = stockRepository.save(stock);
        return toDto(stock);
    }

    @Transactional
    public StockDto update(Long drugId, StockUpdateRequest request) {
        Stock stock = stockRepository.findByDrugId(drugId)
                .orElseThrow(() -> new ResourceNotFoundException("Остаток", drugId));
        stock.setQuantity(request.quantity());
        stock = stockRepository.save(stock);
        return toDto(stock);
    }

    @Transactional
    public void delete(Long drugId) {
        Stock stock = stockRepository.findByDrugId(drugId)
                .orElseThrow(() -> new ResourceNotFoundException("Остаток", drugId));
        stockRepository.delete(stock);
    }

    private StockDto toDto(Stock stock) {
        return new StockDto(
                stock.getDrug().getId(),
                stock.getDrug().getName(),
                stock.getQuantity(),
                stock.getDrug().getMinQuantity()
        );
    }
}

