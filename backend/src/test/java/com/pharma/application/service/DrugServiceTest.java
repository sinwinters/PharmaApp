package com.pharma.application.service;

import com.pharma.application.dto.DrugCreateUpdate;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.domain.entity.Category;
import com.pharma.domain.entity.Drug;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.entity.Supplier;
import com.pharma.domain.repository.CategoryRepository;
import com.pharma.domain.repository.DrugRepository;
import com.pharma.domain.repository.StockRepository;
import com.pharma.domain.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrugServiceTest {

    @Mock
    private DrugRepository drugRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private DrugService drugService;

    @Test
    void create_throwsWhenCategoryNotFound() {
        DrugCreateUpdate dto = new DrugCreateUpdate("Drug", 1L, 1L, 10, "шт", BigDecimal.TEN);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> drugService.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Категория");
    }

    @Test
    void create_savesDrugAndStock() {
        Category cat = Category.builder().id(1L).name("Cat").build();
        Supplier sup = Supplier.builder().id(1L).name("Sup").build();
        Drug savedDrug = Drug.builder().id(10L).name("D").category(cat).supplier(sup).basePrice(BigDecimal.ONE).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sup));
        when(drugRepository.save(any(Drug.class))).thenReturn(savedDrug);
        when(drugRepository.findByIdWithAssociations(10L)).thenReturn(Optional.of(savedDrug));
        when(stockRepository.findByDrugId(10L)).thenReturn(Optional.of(Stock.builder().quantity(0).build()));

        DrugCreateUpdate dto = new DrugCreateUpdate("D", 1L, 1L, 5, "шт", BigDecimal.ONE);
        drugService.create(dto);

        ArgumentCaptor<Drug> drugCaptor = ArgumentCaptor.forClass(Drug.class);
        verify(drugRepository).save(drugCaptor.capture());
        assertThat(drugCaptor.getValue().getName()).isEqualTo("D");
        assertThat(drugCaptor.getValue().getMinQuantity()).isEqualTo(5);

        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        verify(stockRepository).save(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getQuantity()).isZero();
    }

    @Test
    void delete_throwsWhenDrugNotFound() {
        when(drugRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> drugService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
