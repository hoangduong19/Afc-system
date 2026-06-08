package com.metro.afc.fare.infrastructure.adapter.out.fareDiscountAuditLog;

import com.metro.afc.fare.domain.model.FareDiscountAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FareDiscountAuditLogJpaRepository
        extends JpaRepository<FareDiscountAuditLog, UUID> {}