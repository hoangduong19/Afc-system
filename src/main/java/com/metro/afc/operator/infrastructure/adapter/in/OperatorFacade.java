package com.metro.afc.operator.infrastructure.adapter.in;

import com.metro.afc.operator.application.dtos.CreateOperatorRequest;
import com.metro.afc.operator.application.dtos.OperatorResponse;
import com.metro.afc.operator.application.dtos.UpdateOperatorRequest;
import com.metro.afc.operator.application.port.in.OperatorUseCase;
import com.metro.afc.operator.domain.model.Operator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OperatorFacade {

    private final OperatorUseCase operatorUseCase;

    public OperatorResponse create(CreateOperatorRequest request) {
        Operator operator = operatorUseCase.create(request.code(), request.name());
        return OperatorResponse.from(operator);
    }

    public OperatorResponse update(UUID id, UpdateOperatorRequest request) {
        Operator operator = operatorUseCase.update(id, request.name());
        return OperatorResponse.from(operator);
    }

    public void activate(UUID id) {
        operatorUseCase.activate(id);
    }

    public void deactivate(UUID id) {
        operatorUseCase.deactivate(id);
    }

    public OperatorResponse findById(UUID id) {
        Operator operator = operatorUseCase.findById(id);
        return OperatorResponse.from(operator);
    }

    public List<OperatorResponse> findAll() {
        return operatorUseCase.findAll()
                .stream()
                .map(OperatorResponse::from)
                .toList();
    }
}
