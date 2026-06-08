package com.metro.afc.route.infrastructure.adapter.in;

import com.metro.afc.route.application.dtos.CreateRouteRequest;
import com.metro.afc.route.application.dtos.RouteResponse;
import com.metro.afc.route.application.dtos.UpdateRouteRequest;
import com.metro.afc.route.application.port.in.RouteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RouteFacade {

    private final RouteUseCase routeUseCase;

    public RouteResponse create(CreateRouteRequest request) {
        return RouteResponse.from(routeUseCase.create(
                request.operatorId(), request.code(),
                request.name(), request.type()
        ));
    }

    public RouteResponse update(UUID id, UpdateRouteRequest request) {
        return RouteResponse.from(routeUseCase.update(id, request.name()));
    }

    public void delete(UUID id) {
        routeUseCase.delete(id);
    }

    public RouteResponse findById(UUID id) {
        return RouteResponse.from(routeUseCase.findById(id));
    }

    public List<RouteResponse> findAll() {
        return routeUseCase.findAll().stream()
                .map(RouteResponse::from).toList();
    }

    public List<RouteResponse> findByOperatorId(UUID operatorId) {
        return routeUseCase.findByOperatorId(operatorId).stream()
                .map(RouteResponse::from).toList();
    }
}