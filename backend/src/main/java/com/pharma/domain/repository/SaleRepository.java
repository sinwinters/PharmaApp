package com.pharma.domain.repository;

import com.pharma.domain.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @EntityGraph(attributePaths = {"user", "items", "items.drug"})
    Page<Sale> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.drug"})
    Optional<Sale> findWithItemsById(Long id);

    long countByCreatedAtBetween(Instant from, Instant to);

    @Query("""
            select coalesce(sum(s.totalAmount), 0)
            from Sale s
            where s.createdAt between :from and :to
            """)
    BigDecimal sumTotalAmountByPeriod(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            select si.drug.id as drugId,
                   si.drug.name as drugName,
                   coalesce(sum(si.quantity), 0) as totalQuantity
            from SaleItem si
            where si.sale.createdAt between :from and :to
            group by si.drug.id, si.drug.name
            order by coalesce(sum(si.quantity), 0) desc
            """)
    List<TopDrugView> findTopDrugsByPeriod(@Param("from") Instant from,
                                           @Param("to") Instant to,
                                           Pageable pageable);

    interface TopDrugView {
        Long getDrugId();

        String getDrugName();

        Long getTotalQuantity();
    }
}
