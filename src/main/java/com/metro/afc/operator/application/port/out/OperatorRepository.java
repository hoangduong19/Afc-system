package com.metro.afc.operator.application.port.out;

import com.metro.afc.operator.domain.model.Operator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperatorRepository {
    Optional<Operator> findById(UUID id);
    Optional<Operator> findByCode(String code);
    List<Operator> findAll();
    boolean existsByCode(String code);
    Operator save(Operator operator);
}
