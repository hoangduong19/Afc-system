package com.metro.afc.operator.infrastructure.adapter.out;

import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.operator.domain.model.Operator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OperatorRepositoryImpl implements OperatorRepository {

    private final OperatorJpaRepository jpa;

    @Override
    public Optional<Operator> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Operator> findByCode(String code) {
        return jpa.findByCode(code);
    }

    @Override
    public List<Operator> findAll() {
        return jpa.findAll();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpa.existsById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByCode(code);
    }

    @Override
    public Operator save(Operator operator) {
        return jpa.save(operator);
    }
}