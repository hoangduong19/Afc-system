package com.metro.afc.route.infrastructure.adapter.out;

import com.metro.afc.route.domain.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RouteJpaRepository extends JpaRepository<Route, UUID> {
    boolean existsByCode(String code);
    List<Route> findByOperatorId(UUID operatorId);
    Optional<Route> findByCode(String code);
}