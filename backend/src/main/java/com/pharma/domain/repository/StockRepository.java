package com.pharma.domain.repository;

import com.pharma.domain.entity.Stock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends org.springframework.data.jpa.repository.JpaRepository<Stock, Long> {

    Optional<Stock> findByDrugId(Long drugId);

    List<Stock> findAllByDrugIdIn(Collection<Long> drugIds);

    @Query("select count(s) from Stock s join s.drug d where s.quantity <= d.minQuantity")
    long countCriticalStock();
}
