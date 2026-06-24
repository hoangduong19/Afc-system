package com.metro.afc.settlement.application;

import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.settlement.application.port.in.SettlementUseCase;
import com.metro.afc.settlement.application.port.out.SettlementRepository;
import com.metro.afc.settlement.domain.ReconciliationLog;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
import com.metro.afc.settlement.domain.enums.settlement.SettlementStatus;
import com.metro.afc.settlement.domain.settlementAllocation.AllocationStrategy;
import com.metro.afc.settlement.domain.valueObject.SettlementPeriod;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService implements SettlementUseCase {

    private final SettlementRepository  settlementRepository;
    private final TripRepository        tripRepository;
    private final TripAnomalyRepository anomalyRepository;
    private final TicketRepository ticketRepository;
    private final FareRuleRepository fareRuleRepository;

    private final AllocationStrategy allocationStrategy;

    private static final BigDecimal DEFAULT_TOLERANCE = new BigDecimal("100");

    @Override
    @Transactional
    public Settlement run(int month, int year, UUID ranBy) {
        SettlementPeriod period = new SettlementPeriod(month, year);

        if (settlementRepository.existsByPeriod(period.format())) {
            throw new ConflictException(ErrorCode.SETTLEMENT_ALREADY_EXISTS);
        }

        long unresolved = anomalyRepository.countUnresolvedInPeriod(period.fromInstant(), period.toInstant());
        if (unresolved > 0) {
            throw new BusinessRuleException(ErrorCode.SETTLEMENT_HAS_UNRESOLVED_ANOMALIES,
                    unresolved + " unresolved anomalies found.");
        }

        // 2. Fetch all raw domain data required for settlement
        List<Trip> allTrips = tripRepository.findCompletedTripsInPeriod(period.fromInstant(), period.toInstant());
        if (allTrips.isEmpty()) {
            throw new BusinessRuleException(ErrorCode.SETTLEMENT_NO_TRIPS);
        }

        Set<UUID> usedTicketIds = allTrips.stream()
                .map(Trip::getTicketId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, Ticket> usedTickets = ticketRepository.findAllByIds(usedTicketIds).stream()
                .collect(Collectors.toMap(Ticket::getId, t -> t));

        List<FareRule> activeFareRules = fareRuleRepository.findAllActive();

        // Sau
        List<Ticket> soldTickets = ticketRepository
                .findActiveInPeriod(
                        period.fromLocalDate(),
                        period.toLocalDate()
                );

        // 3. Delegate to Domain Aggregate (Rich Domain Model)
        Settlement.SettlementResult result = Settlement.calculateAndSettle(
                period.format(),
                allTrips,
                usedTickets,
                soldTickets,
                activeFareRules,
                allocationStrategy,          // ← pass vào
                DEFAULT_TOLERANCE,
                ranBy
        );

        Settlement settlement = result.settlement();
        settlementRepository.save(settlement);

        // 4. Save side-effects (shares and logs)
        result.shares().forEach(settlementRepository::saveShare);

        if (settlement.getReconciliationStatus() != ReconcileStatus.MATCH) {
            settlementRepository.saveLog(ReconciliationLog.of(
                    settlement.getId(),
                    settlement.getReconciliationStatus().name(),
                    settlement.getDiffAmount().abs(),
                    allTrips.size(),
                    "Settlement reconciliation diff detected",
                    Map.of("diffAmount", settlement.getDiffAmount().toString())
            ));
        }

        log.info("Settlement: period={}, status={}", period.format(), settlement.getReconciliationStatus());
        return settlement;
    }

    @Override
    @Transactional
    public Settlement confirm(UUID settlementId, UUID confirmedBy) {
        Settlement settlement = findOrThrow(settlementId);

        if (settlement.getStatus() != SettlementStatus.DRAFT)
            throw new BusinessRuleException(ErrorCode.SETTLEMENT_NOT_PENDING);

        if (settlement.getReconciliationStatus() == ReconcileStatus.MISMATCH)
            throw new BusinessRuleException(ErrorCode.SETTLEMENT_RECONCILE_FAIL);

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