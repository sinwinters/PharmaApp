package com.pharma.infrastructure.persistence;

import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.domain.entity.Category;
import com.pharma.domain.entity.Drug;
import com.pharma.domain.entity.Supplier;
import com.pharma.domain.repository.DrugRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderBuilderImplTest {

    @Mock
    private DrugRepository drugRepository;

    @Test
    void build_throwsWhenSupplierNotSet() {
        OrderBuilderImpl builder = new OrderBuilderImpl(drugRepository);

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Поставщик");
    }

    @Test
    void build_throwsWhenDrugNotFound() {
        Supplier sup = Supplier.builder().id(1L).name("S").build();
        when(drugRepository.findById(999L)).thenReturn(Optional.empty());

        OrderBuilderImpl builder = new OrderBuilderImpl(drugRepository);
        builder.withSupplier(sup).addItem(999L, 2);

        assertThatThrownBy(builder::build)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Лекарство");
    }

    @Test
    void build_createsOrderWithItems() {
        Supplier sup = Supplier.builder().id(1L).name("Supplier").build();
        Category cat = Category.builder().id(1L).name("C").build();
        Drug drug = Drug.builder().id(10L).name("D").category(cat).supplier(sup).basePrice(BigDecimal.valueOf(50)).build();
        when(drugRepository.findById(10L)).thenReturn(Optional.of(drug));

        OrderBuilderImpl builder = new OrderBuilderImpl(drugRepository);
        var order = builder.withSupplier(sup).addItem(10L, 3).build();

        assertThat(order).isNotNull();
        assertThat(order.getSupplier()).isEqualTo(sup);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getDrug()).isEqualTo(drug);
        assertThat(order.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(order.getItems().get(0).getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }
}
