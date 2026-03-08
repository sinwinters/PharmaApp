package com.pharma.application.service;

import com.pharma.application.dto.SaleCreateRequest;
import com.pharma.application.dto.SaleItemRequest;
import com.pharma.application.exception.InsufficientStockException;
import com.pharma.domain.entity.Category;
import com.pharma.domain.entity.Drug;
import com.pharma.domain.entity.Role;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.entity.Supplier;
import com.pharma.domain.entity.User;
import com.pharma.domain.observer.LowStockNotifier;
import com.pharma.domain.repository.DrugRepository;
import com.pharma.domain.repository.SaleRepository;
import com.pharma.domain.repository.StockRepository;
import com.pharma.domain.repository.UserRepository;
import com.pharma.domain.strategy.PricingStrategy;
import com.pharma.infrastructure.integration.AvestEdsGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;
    @Mock
    private DrugRepository drugRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PricingStrategy pricingStrategy;
    @Mock
    private LowStockNotifier lowStockNotifier;
    @Mock
    private BenefitPolicyService benefitPolicyService;
    @Mock
    private AvestEdsGateway avestEdsGateway;

    @InjectMocks
    private SaleService saleService;

    @Test
    void createSale_throwsWhenInsufficientStock() {
        Role role = new Role(1L, "CASHIER", null);
        User user = User.builder().id(1L).username("u").passwordHash("h").role(role).enabled(true).createdAt(java.time.Instant.now()).updatedAt(java.time.Instant.now()).build();
        Category cat = Category.builder().id(1L).name("C").build();
        Supplier sup = Supplier.builder().id(1L).name("S").build();
        Drug drug = Drug.builder().id(1L).name("Paracetamol").category(cat).supplier(sup).minQuantity(10).basePrice(BigDecimal.valueOf(100)).build();
        Stock stock = Stock.builder().drug(drug).quantity(2).build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(drugRepository.findByIdWithAssociations(1L)).thenReturn(Optional.of(drug));
        when(stockRepository.findByDrugId(1L)).thenReturn(Optional.of(stock));

        when(benefitPolicyService.resolveProgram(any())).thenReturn(Optional.empty());

        SaleCreateRequest request = new SaleCreateRequest(List.of(new SaleItemRequest(1L, 5)), null, null, null, null);

        assertThatThrownBy(() -> saleService.createSale(request, "u"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Недостаточно");
    }
}
