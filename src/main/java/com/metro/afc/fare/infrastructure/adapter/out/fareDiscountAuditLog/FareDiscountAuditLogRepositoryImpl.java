package com.metro.afc.fare.infrastructure.adapter.out.fareDiscountAuditLog;

import com.metro.afc.fare.application.port.out.FareDiscountAuditLogRepository;
import com.metro.afc.fare.domain.model.FareDiscountAuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FareDiscountAuditLogRepositoryImpl
        implements FareDiscountAuditLogRepository {

    private final FareDiscountAuditLogJpaRepository jpa;

    @Override
    public FareDiscountAuditLog save(FareDiscountAuditLog log) {
        return jpa.save(log);
    }
}