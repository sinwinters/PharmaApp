package com.pharma.infrastructure.integration;

import com.pharma.domain.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Заглушка шлюза передачи автозаказа на фармсклад с GLN.
 */
@Slf4j
@Component
public class PharmaWarehouseGateway {

    public void sendAutoOrder(Order order) {
        log.info("Передача автозаказа на фармсклад: orderId={}, gln={}, invoice={}",
                order.getId(), order.getDestinationGln(), order.getInvoiceNumber());
    }
}
