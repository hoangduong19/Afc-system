package com.metro.afc.route.domain.model;

import com.metro.afc.route.domain.model.enums.RouteType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "routes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "operator_id", nullable = false, columnDefinition = "uuid")
    private UUID operatorId;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static Route create(UUID operatorId, String code,
                               String name, RouteType type) {
        Route route    = new Route();
        route.id       = UUID.randomUUID();
        route.operatorId = operatorId;
        route.code     = code.trim().toUpperCase();
        route.name     = name.trim();
        route.type     = type;
        return route;
    }

    public void update(String name) {
        this.name = name.trim();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
