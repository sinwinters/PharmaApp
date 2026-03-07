package com.pharma.application.service;

import com.pharma.application.dto.CategoryDto;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.domain.entity.Category;
import com.pharma.domain.repository.CategoryRepository;
import com.pharma.infrastructure.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CACHE_CATEGORIES, key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<CategoryDto> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Категория", id));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public CategoryDto create(String name, String description) {
        Category c = Category.builder().name(name).description(description).build();
        c = categoryRepository.save(c);
        return toDto(c);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public CategoryDto update(Long id, String name, String description) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категория", id));
        c.setName(name);
        c.setDescription(description);
        return toDto(categoryRepository.save(c));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) throw new ResourceNotFoundException("Категория", id);
        categoryRepository.deleteById(id);
    }

    private CategoryDto toDto(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getDescription());
    }
}
