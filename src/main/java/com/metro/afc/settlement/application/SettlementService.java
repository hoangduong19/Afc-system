package com.metro.afc.settlement.application;

import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.settlement.application.dto.settlement.OperatorTripData;
import com.metro.afc.settlement.application.port.in.SettlementUseCase;
import com.metro.afc.settlement.application.port.out.SettlementRepository;
import com.metro.afc.settlement.domain.CompanyShare;
import com.metro.afc.settlement.domain.ReconciliationLog;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
import com.metro.afc.settlement.domain.enums.settlement.SettlementStatus;
import com.metro.afc.settlement.domain.valueObject.SettlementPeriod;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService implements SettlementUseCase {

    private final SettlementRepository settlementRepository;
    private final TripRepository tripRepository;
    private final OperatorRepository operatorRepository;

    private static final BigDecimal DEFAULT_TOLERANCE =
            new BigDecimal("100");

    @Override
    @Transactional
    public Settlement run(int month, int year, UUID ranBy) {

        // 1. Period VO
        SettlementPeriod period = new SettlementPeriod(month, year);

        if (settlementRepository.existsByPeriod(period.format()))
            throw new ConflictException(
                    ErrorCode.SETTLEMENT_ALREADY_EXISTS);

        // 2. Lấy trips
        List<Trip> trips = tripRepository.findCompletedTripsInPeriod(
                period.fromInstant(), period.toInstant());

        if (trips.isEmpty())
            throw new BusinessRuleException(
                    ErrorCode.SETTLEMENT_NO_TRIPS);

        // 3. Tổng hệ thống
        Money totalExpected = Money.of(trips.stream()
                .map(t -> t.getFareAmount() != null
                        ? t.getFareAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal totalSystemKm = trips.stream()
                .map(t -> t.getDistanceKm() != null
                        ? t.getDistanceKm() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Tạo settlement aggregate
        Settlement settlement = Settlement.create(
                period.format(), totalExpected,
                DEFAULT_TOLERANCE, ranBy);
        settlementRepository.save(settlement);

        // 5. Group by operator → OperatorTripData
        Map<UUID, List<Trip>> byOperator = trips.stream()
                .filter(t -> t.getOperatorId() != null)
                .collect(Collectors.groupingBy(Trip::getOperatorId));

        List<OperatorTripData> operatorData = byOperator.entrySet()
                .stream()
                .map(e -> new OperatorTripData(
                        e.getKey(),
                        e.getValue().stream()
                                .map(t -> t.getDistanceKm() != null
                                        ? t.getDistanceKm() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        e.getValue().size()
                )).toList();

        // 6. Domain: tính shares + reconcile
        List<CompanyShare> shares = settlement.allocateShares(
                operatorData, totalSystemKm);
        settlement.reconcile(shares);

        // 7. Save shares
        shares.forEach(settlementRepository::saveShare);

        // 8. Log nếu có sai lệch
        if (settlement.getReconciliationStatus() != ReconcileStatus.MATCH) {
            settlementRepository.saveLog(ReconciliationLog.of(
                    settlement.getId(),
                    settlement.getReconciliationStatus().name(),
                    settlement.getDiffAmount().abs(),
                    trips.size(),
                    "Settlement reconciliation diff detected",
                    Map.of(
                            "totalExpected",
                            totalExpected.getAmount().toString(),
                            "totalActual",
                            settlement.getTotalActual()
                                    .getAmount().toString(),
                            "diffAmount",
                            settlement.getDiffAmount().toString()
                    )
            ));
        }

        log.info("Settlement: period={}, totalExpected={}, status={}",
                period.format(), totalExpected.getAmount(),
                settlement.getReconciliationStatus());

        return settlementRepository.save(settlement);
    }

    @Override
    @Transactional
    public Settlement confirm(UUID settlementId, UUID confirmedBy) {
        Settlement settlement = findOrThrow(settlementId);

        if (settlement.getStatus() != SettlementStatus.DRAFT)
            throw new BusinessRuleException(
                    ErrorCode.SETTLEMENT_NOT_PENDING);

        if (settlement.getReconciliationStatus()
                == ReconcileStatus.MISMATCH)
            throw new BusinessRuleException(
                    ErrorCode.SETTLEMENT_RECONCILE_FAIL);

        settlement.confirm();
        return settlementRepository.save(settlement);
    }

    @Override
    public Settlement findById(UUID id) { return findOrThrow(id); }

    @Override
    public List<Settlement> findAll() {
        return settlementRepository.findAll();
    }

    private Settlement findOrThrow(UUID id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.SETTLEMENT_NOT_FOUND));
    }
}