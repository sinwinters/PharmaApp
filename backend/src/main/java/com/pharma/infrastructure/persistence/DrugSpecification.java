package com.pharma.infrastructure.persistence;

import com.pharma.domain.entity.Drug;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public final class DrugSpecification {

    private DrugSpecification() {}

    public static Specification<Drug> filter(String name, Long categoryId, Long supplierId) {
    return (root, query, cb) -> {

        List<Predicate> predicates = new ArrayList<>();

        // 🔎 Поиск по имени
        if (name != null && !name.isBlank()) {
            predicates.add(
                cb.like(
                    cb.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
                )
            );
        }

        // 📂 Фильтр по категории (ВАЖНО: JOIN)
        if (categoryId != null) {
            var categoryJoin = root.join("category");
            predicates.add(cb.equal(categoryJoin.get("id"), categoryId));
        }

        // 🚚 Фильтр по поставщику (JOIN)
        if (supplierId != null) {
            var supplierJoin = root.join("supplier");
            predicates.add(cb.equal(supplierJoin.get("id"), supplierId));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    };
}
}
