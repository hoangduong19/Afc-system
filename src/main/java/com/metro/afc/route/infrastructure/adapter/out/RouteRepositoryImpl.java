package com.metro.afc.route.infrastructure.adapter.out;

import com.metro.afc.route.application.port.out.RouteRepository;
import com.metro.afc.route.domain.model.Route;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RouteRepositoryImpl implements RouteRepository {

    private final RouteJpaRepository jpa;

    @Override
    public Optional<Route> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Route> findAll() {
        return jpa.findAll();
    }

    @Override
    public List<Route> findByOperatorId(UUID operatorId) {
        return jpa.findByOperatorId(operatorId);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByCode(code);
    }

    @Override
    public Route save(Route route) {
        return jpa.save(route);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}