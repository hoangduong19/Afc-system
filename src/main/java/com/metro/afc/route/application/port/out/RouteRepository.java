package com.metro.afc.route.application.port.out;

import com.metro.afc.route.domain.model.Route;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteRepository {
    Optional<Route> findById(UUID id);
    List<Route> findAll();
    List<Route> findByOperatorId(UUID operatorId);
    boolean existsByCode(String code);
    boolean existsById(UUID id);
    Route save(Route route);
    void deleteById(UUID id);
}