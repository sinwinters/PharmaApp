package com.pharma.application.service;

import com.pharma.application.dto.CategoryDto;
import com.pharma.application.exception.ResourceNotFoundException;
import com.pharma.domain.entity.Category;
import com.pharma.domain.repository.CategoryRepository;
import com.pharma.infrastructure.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 👇 Кэшируем НЕ Page, а свою обёртку
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.CACHE_CATEGORIES,
            key = "#pageable.pageNumber + '-' + #pageable.pageSize"
    )
    public CachedPage<CategoryDto> findAllCached(Pageable pageable) {
        Page<CategoryDto> page = categoryRepository.findAll(pageable).map(this::toDto);

        return new CachedPage<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    // 👇 Публичный метод возвращает обычный Page
    @Transactional(readOnly = true)
    public Page<CategoryDto> findAll(Pageable pageable) {
        CachedPage<CategoryDto> cached = findAllCached(pageable);

        return new PageImpl<>(
                cached.getContent(),
                PageRequest.of(cached.getPage(), cached.getSize()),
                cached.getTotalElements()
        );
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
        Category c = Category.builder()
                .name(name)
                .description(description)
                .build();

        return toDto(categoryRepository.save(c));
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
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Категория", id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDto toDto(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getDescription());
    }

    // 👇 Простая сериализуемая обёртка
    public static class CachedPage<T> implements Serializable {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;

        public CachedPage() {}

        public CachedPage(List<T> content, int page, int size, long totalElements) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
        }

        public List<T> getContent() { return content; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public long getTotalElements() { return totalElements; }

        public void setContent(List<T> content) { this.content = content; }
        public void setPage(int page) { this.page = page; }
        public void setSize(int size) { this.size = size; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    }
}