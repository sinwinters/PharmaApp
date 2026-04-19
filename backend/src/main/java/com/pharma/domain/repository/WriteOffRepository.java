package com.pharma.domain.repository;

import com.pharma.domain.entity.WriteOff;
import com.pharma.domain.entity.WriteOffReason;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WriteOffRepository extends JpaRepository<WriteOff, Long> {

    @EntityGraph(attributePaths = {"drug"})
    List<WriteOff> findAllByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    @EntityGraph(attributePaths = {"drug"})
    List<WriteOff> findAllByCreatedAtBetweenAndReasonOrderByCreatedAtDesc(LocalDateTime from,
                                                                          LocalDateTime to,
                                                                          WriteOffReason reason);
}
