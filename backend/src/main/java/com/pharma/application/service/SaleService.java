package com.pharma.application.service;

import com.pharma.application.dto.SaleCreateRequest;
import com.pharma.application.dto.SaleDto;
import com.pharma.application.dto.SaleItemDto;
import com.pharma.application.exception.InsufficientStockException;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.domain.entity.Drug;
import com.pharma.domain.entity.Sale;
import com.pharma.domain.entity.SaleItem;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.entity.User;
import com.pharma.domain.repository.DrugRepository;
import com.pharma.domain.repository.SaleRepository;
import com.pharma.domain.repository.StockRepository;
import com.pharma.domain.repository.UserRepository;
import com.pharma.domain.observer.LowStockNotifier;
import com.pharma.domain.strategy.PricingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Фасад продажи: проведение продажи + списание остатков + уведомление о низком остатке (паттерн Facade).
 */
@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final DrugRepository drugRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PricingStrategy pricingStrategy;
    private final LowStockNotifier lowStockNotifier;

    @Transactional
    public SaleDto createSale(SaleCreateRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", username));
        Sale sale = Sale.builder()
                .user(user)
                .totalAmount(BigDecimal.ZERO)
                .build();
        List<SaleItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var req : request.items()) {
            Drug drug = drugRepository.findByIdWithAssociations(req.drugId())
                    .orElseThrow(() -> new ResourceNotFoundException("Лекарство", req.drugId()));
            Stock stock = stockRepository.findByDrugId(drug.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Остаток", req.drugId()));
            int qty = req.quantity();
            if (stock.getQuantity() < qty) {
                throw new InsufficientStockException(drug.getName(), qty, Optional.of(stock.getQuantity()));
            }
            BigDecimal unitPrice = drug.getBasePrice();
            BigDecimal lineTotal = pricingStrategy.calculatePrice(drug, qty);
            SaleItem item = SaleItem.builder()
                    .sale(sale)
                    .drug(drug)
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .build();
            items.add(item);
            total = total.add(lineTotal);
            stock.setQuantity(stock.getQuantity() - qty);
            stockRepository.save(stock);
            if (stock.getQuantity() <= drug.getMinQuantity()) {
                lowStockNotifier.notifyLowStock(drug, stock.getQuantity(), drug.getMinQuantity());
            }
        }
        sale.setTotalAmount(total);
        sale.setItems(items);
        sale = saleRepository.save(sale);
        return toDto(sale);
    }

    @Transactional(readOnly = true)
    public Page<SaleDto> findAll(Pageable pageable) {
        return saleRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<SaleDto> findById(Long id) {
        return saleRepository.findWithItemsById(id).map(this::toDto);
    }

    private SaleDto toDto(Sale s) {
        List<SaleItemDto> itemDtos = s.getItems().stream()
                .map(i -> new SaleItemDto(
                        i.getDrug().getId(),
                        i.getDrug().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))
                ))
                .toList();
        return new SaleDto(
                s.getId(),
                s.getUser().getId(),
                s.getUser().getUsername(),
                s.getTotalAmount(),
                s.getCreatedAt(),
                itemDtos
        );
    }
}
