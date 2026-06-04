package com.metro.afc.operator.application.port.in;

import com.metro.afc.operator.domain.model.Operator;

import java.util.List;
import java.util.UUID;

public interface OperatorUseCase {
    Operator create(String code, String name);
    Operator update(UUID id, String name);
    void deactivate(UUID id);
    Operator findById(UUID id);
    List<Operator> findAll();
}
