package com.pharma.application.service;

import com.pharma.application.exception.PharmaException;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.domain.entity.Stock;
import com.pharma.domain.entity.StockStatus;
import com.pharma.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryPolicyService {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public List<Stock> getFefoStocks(Long drugId) {
        return stockRepository.findAllByDrugIdOrderByExpirationDateAsc(drugId);
    }

    public void assertSellAllowed(Stock stock) {
        if (stock.getStatus() == StockStatus.EXPIRED || stock.getStatus() == StockStatus.DEFECTIVE) {
            throw new IllegalStateException("Нельзя продать просроченный или бракованный товар");
        }

        LocalDate expirationDate = resolveExpirationDate(stock);
        if (expirationDate != null && expirationDate.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Срок годности истёк");
        }
    }

    @Transactional
    public void deductByFefo(Long drugId, int quantityToSell) {
        List<Stock> lots = getFefoStocks(drugId);
        if (lots.isEmpty()) {
            throw new ResourceNotFoundException("Остаток", drugId);
        }

        int remaining = quantityToSell;
        for (Stock stock : lots) {
            assertSellAllowed(stock);
            int available = stock.getQuantity() - (stock.getReservedQuantity() == null ? 0 : stock.getReservedQuantity());
            if (available <= 0) {
                continue;
            }

            int deduct = Math.min(available, remaining);
            stock.setQuantity(stock.getQuantity() - deduct);
            stockRepository.save(stock);
            remaining -= deduct;
            if (remaining == 0) {
                return;
            }
        }

        throw new PharmaException("Недостаточный остаток для продажи по FEFO");
    }

    private LocalDate resolveExpirationDate(Stock stock) {
        if (stock.getExpirationDate() != null) {
            return stock.getExpirationDate();
        }
        if (stock.getExpiresAt() != null) {
            return stock.getExpiresAt().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }
}
