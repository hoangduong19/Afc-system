package com.metro.afc.fare.application.port.out;

import com.metro.afc.fare.domain.model.FareDiscountAuditLog;

public interface FareDiscountAuditLogRepository {
    FareDiscountAuditLog save(FareDiscountAuditLog log);
}