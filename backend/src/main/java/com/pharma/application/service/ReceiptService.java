package com.pharma.application.service;

import com.pharma.application.dto.ReceiptDto;
import com.pharma.application.dto.ReceiptItemDto;
import com.pharma.domain.entity.Sale;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReceiptService {

    public ReceiptDto generateReceipt(Sale sale) {
        List<ReceiptItemDto> items = sale.getItems().stream()
                .map(item -> new ReceiptItemDto(
                        item.getDrugName() != null ? item.getDrugName() : item.getDrug().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotal()
                ))
                .toList();

        return new ReceiptDto(
                sale.getId(),
                sale.getCreatedAt(),
                items,
                sale.getTotalAmount(),
                sale.getPaymentType(),
                sale.getIsPrescriptionSale()
        );
    }
}
