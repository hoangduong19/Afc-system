package com.metro.afc.route.application;

import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.route.application.port.in.RouteUseCase;
import com.metro.afc.route.application.port.out.RouteRepository;
import com.metro.afc.route.domain.model.Route;
import com.metro.afc.route.domain.model.enums.RouteType;
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
public class RouteService implements RouteUseCase {

    private final RouteRepository routeRepository;
    private final OperatorRepository operatorRepository;

    @Override
    @Transactional
    public Route create(UUID operatorId, String code, String name, RouteType type) {
        if (!operatorRepository.existsById(operatorId)) {
            throw new NotFoundException(ErrorCode.OPERATOR_NOT_FOUND);
        }
        if (routeRepository.existsByCode(code.trim().toUpperCase())) {
            throw new ConflictException(ErrorCode.ROUTE_ALREADY_EXISTS);
        }
        return routeRepository.save(Route.create(operatorId, code, name, type));
    }

    @Override
    @Transactional
    public Route update(UUID id, String name) {
        Route route = findOrThrow(id);
        route.update(name);
        return routeRepository.save(route);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        findOrThrow(id);
        routeRepository.deleteById(id);
    }

    @Override
    public Route findById(UUID id) {
        return findOrThrow(id);
    }

    @Override
    public List<Route> findAll() {
        return routeRepository.findAll();
    }

    @Override
    public List<Route> findByOperatorId(UUID operatorId) {
        return routeRepository.findByOperatorId(operatorId);
    }

    private Route findOrThrow(UUID id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROUTE_NOT_FOUND));
    }
}