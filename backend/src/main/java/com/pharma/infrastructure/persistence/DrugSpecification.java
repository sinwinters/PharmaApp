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
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (supplierId != null) {
                predicates.add(cb.equal(root.get("supplier").get("id"), supplierId));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
