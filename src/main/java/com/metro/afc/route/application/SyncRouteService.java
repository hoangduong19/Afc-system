package com.metro.afc.route.application;

import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.route.application.port.in.SyncRouteUseCase;
import com.metro.afc.route.application.port.out.RouteRepository;
import com.metro.afc.route.domain.model.Route;
import com.metro.afc.route.domain.model.enums.RouteType;
import com.metro.afc.route.infrastructure.messaging.RouteSyncMessage;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SyncRouteService implements SyncRouteUseCase {

    private final RouteRepository routeRepository;
    private final OperatorRepository operatorRepository;

    @Override
    @Transactional
    public void sync(RouteSyncMessage message) {
        routeRepository.findByCode(message.routeCode())
                .ifPresentOrElse(
                        existing -> existing.update(message.routeName()),
                        () -> {
                            UUID operatorId = operatorRepository
                                    .findByCode(message.operatorCode())
                                    .orElseThrow(() -> new NotFoundException(
                                            ErrorCode.OPERATOR_NOT_FOUND))
                                    .getId();

                            routeRepository.save(Route.create(
                                    operatorId,
                                    message.routeCode(),
                                    message.routeName(),
                                    RouteType.valueOf(message.transportType())
                            ));
                        }
                );
    }
}