package com.metro.afc.operator.infrastructure.adapter.out;

import com.metro.afc.operator.domain.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatorJpaRepository extends JpaRepository<Operator, UUID> {
    Optional<Operator> findByCode(String code);
    boolean existsByCode(String code);
}