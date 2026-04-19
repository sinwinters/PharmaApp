package com.pharma.application.service;

import com.pharma.application.exception.PharmaException;
import com.pharma.domain.entity.Category;
import com.pharma.domain.entity.Drug;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.entity.StockStatus;
import com.pharma.domain.entity.Supplier;
import com.pharma.domain.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryPolicyServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private InventoryPolicyService inventoryPolicyService;

    @Test
    void cannotSellExpiredStatus() {
        Stock stock = Stock.builder().status(StockStatus.EXPIRED).quantity(5).reservedQuantity(0).build();

        assertThatThrownBy(() -> inventoryPolicyService.assertSellAllowed(stock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Нельзя продать");
    }

    @Test
    void cannotSellDefectiveStatus() {
        Stock stock = Stock.builder().status(StockStatus.DEFECTIVE).quantity(5).reservedQuantity(0).build();

        assertThatThrownBy(() -> inventoryPolicyService.assertSellAllowed(stock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Нельзя продать");
    }

    @Test
    void fefoDeductsNearestExpirationFirst() {
        Drug drug = Drug.builder().id(1L).name("D").category(Category.builder().id(1L).name("C").build())
                .supplier(Supplier.builder().id(1L).name("S").build()).build();

        Stock earliest = Stock.builder().id(1L).drug(drug).quantity(3).reservedQuantity(0).status(StockStatus.AVAILABLE)
                .expirationDate(LocalDate.now().plusDays(3)).build();
        Stock later = Stock.builder().id(2L).drug(drug).quantity(5).reservedQuantity(0).status(StockStatus.AVAILABLE)
                .expirationDate(LocalDate.now().plusDays(10)).build();

        when(stockRepository.findAllByDrugIdOrderByExpirationDateAsc(1L)).thenReturn(List.of(earliest, later));

        inventoryPolicyService.deductByFefo(1L, 4);

        verify(stockRepository).save(earliest);
        verify(stockRepository).save(later);
    }

    @Test
    void fefoThrowsWhenNotEnoughQuantity() {
        Drug drug = Drug.builder().id(1L).name("D").category(Category.builder().id(1L).name("C").build())
                .supplier(Supplier.builder().id(1L).name("S").build()).build();
        Stock only = Stock.builder().id(1L).drug(drug).quantity(1).reservedQuantity(0).status(StockStatus.AVAILABLE)
                .expirationDate(LocalDate.now().plusDays(5)).build();
        when(stockRepository.findAllByDrugIdOrderByExpirationDateAsc(1L)).thenReturn(List.of(only));

        assertThatThrownBy(() -> inventoryPolicyService.deductByFefo(1L, 3))
                .isInstanceOf(PharmaException.class)
                .hasMessageContaining("Недостаточный остаток");
    }
}
