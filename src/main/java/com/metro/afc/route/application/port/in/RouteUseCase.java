package com.metro.afc.route.application.port.in;

import com.metro.afc.route.domain.model.Route;
import com.metro.afc.route.domain.model.enums.RouteType;

import java.util.List;
import java.util.UUID;

public interface RouteUseCase {
    Route create(UUID operatorId, String code, String name, RouteType type);
    Route update(UUID id, String name);
    void delete(UUID id);
    Route findById(UUID id);
    List<Route> findAll();
    List<Route> findByOperatorId(UUID operatorId);
}