package com.pharma.application.service;

import com.pharma.application.dto.OrderCreateRequest;
import com.pharma.application.dto.OrderDetailsDto;
import com.pharma.application.dto.OrderDetailsItemDto;
import com.pharma.application.dto.OrderDto;
import com.pharma.application.dto.OrderItemDto;
import com.pharma.application.exception.PharmaException;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.application.port.OrderBuilder;
import com.pharma.domain.entity.Order;
import com.pharma.domain.entity.OrderStatus;
import com.pharma.domain.repository.OrderRepository;
import com.pharma.domain.repository.SupplierRepository;
import com.pharma.domain.repository.UserRepository;
import com.pharma.infrastructure.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final OrderBuilder orderBuilder;

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_ORDERS_LIST, allEntries = true)
    public OrderDto create(OrderCreateRequest request, String username) {
        var supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Поставщик", request.supplierId()));
        var user = userRepository.findByUsername(username).orElse(null);
        OrderBuilder b = orderBuilder.withSupplier(supplier);
        for (var item : request.items()) {
            b.addItem(item.drugId(), item.quantity());
        }
        b.withCreatedBy(user);
        Order order = b.build();
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findAll(Pageable pageable) {
        List<OrderDto> orders = findAllListCached(pageable);
        long total = orderRepository.count();
        return new PageImpl<>(orders, pageable, total);
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_ORDERS_LIST, key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    @Transactional(readOnly = true)
    public List<OrderDto> findAllListCached(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailsDto findDetailsById(Long id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ", id));

        var items = order.getItems().stream()
                .map(item -> new OrderDetailsItemDto(
                        item.getDrug().getName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        return new OrderDetailsDto(order.getId(), order.getStatus(), order.getCreatedAt(), items);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_ORDERS_LIST, allEntries = true)
    public OrderDto updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ", id));

        if (!isTransitionAllowed(order.getStatus(), newStatus)) {
            throw new PharmaException("Недопустимый переход статуса: " + order.getStatus() + " -> " + newStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        return toDto(order);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_ORDERS_LIST, allEntries = true)
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Заказ", id);
        }
        orderRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public String generateInvoice(Long id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ", id));

        var items = order.getItems();
        BigDecimal total = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String lines = items.stream()
                .map(i -> String.format("- %s | кол-во: %d | цена: %s | сумма: %s",
                        i.getDrug().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("- Без позиций");

        return "ТТН\n" +
                "Заказ №" + order.getId() + "\n" +
                "Дата: " + order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                "Статус: " + order.getStatus() + "\n" +
                "Товары:\n" + lines + "\n" +
                "Итого: " + total;
    }

    private boolean isTransitionAllowed(OrderStatus current, OrderStatus next) {
        if (current == null || next == null) {
            return false;
        }
        if (current == next) {
            return true;
        }
        if (current == OrderStatus.COMPLETED || current == OrderStatus.CANCELLED) {
            return false;
        }
        return switch (current) {
            case DRAFT, CREATED -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED || next == OrderStatus.COMPLETED;
            case PROCESSING -> next == OrderStatus.COMPLETED || next == OrderStatus.CANCELLED;
            default -> false;
        };
    }

    private OrderDto toDto(Order o) {
        var items = o.getItems().stream()
                .map(i -> new OrderItemDto(
                        i.getId(),
                        i.getDrug().getId(),
                        i.getDrug().getName(),
                        i.getQuantity(),
                        i.getUnitPrice()
                ))
                .toList();
        return new OrderDto(
                o.getId(),
                o.getSupplier().getId(),
                o.getSupplier().getName(),
                o.getStatus(),
                o.getCreatedBy() != null ? o.getCreatedBy().getId() : null,
                o.getCreatedAt(),
                o.getDestinationGln(),
                o.getInvoiceNumber(),
                o.getInvoiceGeneratedAt(),
                o.getAutoOrder(),
                items
        );
    }
}
