package com.pharma.application.service;

import com.pharma.application.dto.OrderCreateRequest;
import com.pharma.application.dto.OrderDto;
import com.pharma.application.dto.OrderItemDto;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.application.port.OrderBuilder;
import com.pharma.domain.entity.Order;
import com.pharma.domain.repository.OrderRepository;
import com.pharma.domain.repository.SupplierRepository;
import com.pharma.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final OrderBuilder orderBuilder;

    @Transactional
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
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<OrderDto> findById(Long id) {
        return orderRepository.findWithItemsById(id).map(this::toDto);
    }

    @Transactional
    public OrderDto updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ", id));
        order.setStatus(status);
        orderRepository.save(order);
        return toDto(order);
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
                items
        );
    }
}
