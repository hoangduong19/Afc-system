package com.metro.afc.operator.application;

import com.metro.afc.operator.application.port.in.OperatorUseCase;
import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.operator.domain.model.Operator;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperatorService implements OperatorUseCase {

    private final OperatorRepository operatorRepository;

    @Override
    @Transactional
    public Operator create(String code, String name) {
        if (operatorRepository.existsByCode(code.trim().toUpperCase())) {
            throw new ConflictException(ErrorCode.OPERATOR_ALREADY_EXISTS);
        }

        return operatorRepository.save(Operator.create(code, name));
    }

    @Override
    @Transactional
    public Operator update(UUID id, String name) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.OPERATOR_NOT_FOUND));

        operator.update(name);
        return operatorRepository.save(operator);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.OPERATOR_NOT_FOUND));

        operator.deactivate();
        operatorRepository.save(operator);
    }

    @Override
    public Operator findById(UUID id) {
        return operatorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.OPERATOR_NOT_FOUND));
    }

    @Override
    public List<Operator> findAll() {
        return operatorRepository.findAll();
    }
}