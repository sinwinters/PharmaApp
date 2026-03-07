package com.pharma.domain.repository;

import com.pharma.domain.entity.Stock;

import java.util.Optional;

public interface StockRepository extends org.springframework.data.jpa.repository.JpaRepository<Stock, Long> {

    Optional<Stock> findByDrugId(Long drugId);
}
