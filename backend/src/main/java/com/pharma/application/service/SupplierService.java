package com.pharma.application.service;

import com.pharma.application.dto.SupplierDto;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.domain.entity.Supplier;
import com.pharma.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public Page<SupplierDto> findAll(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public SupplierDto findById(Long id) {
        return supplierRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Поставщик", id));
    }

    @Transactional
    public SupplierDto create(SupplierDto dto) {
        Supplier s = Supplier.builder()
                .name(dto.name())
                .contactInfo(dto.contactInfo())
                .email(dto.email())
                .phone(dto.phone())
                .build();
        s = supplierRepository.save(s);
        return toDto(s);
    }

    @Transactional
    public SupplierDto update(Long id, SupplierDto dto) {
        Supplier s = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Поставщик", id));
        s.setName(dto.name());
        s.setContactInfo(dto.contactInfo());
        s.setEmail(dto.email());
        s.setPhone(dto.phone());
        return toDto(supplierRepository.save(s));
    }

    @Transactional
    public void delete(Long id) {
        if (!supplierRepository.existsById(id)) throw new ResourceNotFoundException("Поставщик", id);
        supplierRepository.deleteById(id);
    }

    private SupplierDto toDto(Supplier s) {
        return new SupplierDto(s.getId(), s.getName(), s.getContactInfo(), s.getEmail(), s.getPhone());
    }
}
