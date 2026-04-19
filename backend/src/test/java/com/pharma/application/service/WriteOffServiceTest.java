package com.pharma.application.service;

import com.pharma.domain.entity.Category;
import com.pharma.domain.entity.Drug;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.entity.StockStatus;
import com.pharma.domain.entity.Supplier;
import com.pharma.domain.repository.StockRepository;
import com.pharma.domain.repository.WriteOffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WriteOffServiceTest {

    @Mock
    private WriteOffRepository writeOffRepository;
    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private WriteOffService writeOffService;

    @Test
    void autoWriteOffCreatesEntriesForExpiredAndDefective() {
        Drug drug = Drug.builder().id(1L).name("Drug").category(Category.builder().id(1L).name("C").build())
                .supplier(Supplier.builder().id(1L).name("S").build()).build();

        Stock expired = Stock.builder().drug(drug).quantity(5).reservedQuantity(0).status(StockStatus.AVAILABLE)
                .expirationDate(LocalDate.now().minusDays(1)).build();
        Stock defect = Stock.builder().drug(drug).quantity(2).reservedQuantity(0).status(StockStatus.DEFECTIVE)
                .expirationDate(LocalDate.now().plusDays(10)).build();

        when(stockRepository.findAll()).thenReturn(List.of(expired, defect));

        writeOffService.autoWriteOffExpiredAndDefective();

        verify(writeOffRepository, atLeastOnce()).save(any());
        verify(stockRepository, atLeastOnce()).save(any());
    }
}
