package com.pharma.domain.repository;

import com.pharma.domain.entity.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DrugRepository extends JpaRepository<Drug, Long>, JpaSpecificationExecutor<Drug> {

    @Query("SELECT d FROM Drug d LEFT JOIN FETCH d.category LEFT JOIN FETCH d.supplier WHERE d.id = :id")
    Optional<Drug> findByIdWithAssociations(Long id);

    @Query("SELECT d FROM Drug d JOIN Stock s ON s.drug.id = d.id WHERE s.quantity < d.minQuantity")
    List<Drug> findAllWithStockBelowMinimum();
}
