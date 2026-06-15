package com.metro.afc.settlement.application;

import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.settlement.application.dto.settlement.v2.FareParams;
import com.metro.afc.settlement.application.dto.settlement.v2.TicketRevenueData;
import com.metro.afc.settlement.application.dto.settlement.v2.TripContribution;
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
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService implements SettlementUseCase {

    private final SettlementRepository  settlementRepository;
    private final TripRepository        tripRepository;
    private final TripAnomalyRepository anomalyRepository;
    private final TicketRepository ticketRepository;
    private final FareRuleRepository fareRuleRepository;

    private static final BigDecimal DEFAULT_TOLERANCE = new BigDecimal("100");

    @Override
    @Transactional
    public Settlement run(int month, int year, UUID ranBy) {

        // 1. Period VO
        SettlementPeriod period = new SettlementPeriod(month, year);

        if (settlementRepository.existsByPeriod(period.format()))
            throw new ConflictException(ErrorCode.SETTLEMENT_ALREADY_EXISTS);

        // 2. Lấy trips
        List<Trip> allTrips = tripRepository.findCompletedTripsInPeriod(
                period.fromInstant(), period.toInstant());

        if (allTrips.isEmpty())
            throw new BusinessRuleException(ErrorCode.SETTLEMENT_NO_TRIPS);

        long unresolved = anomalyRepository.countUnresolvedInPeriod(
                period.fromInstant(), period.toInstant());

        if (unresolved > 0)
            throw new BusinessRuleException(
                    ErrorCode.SETTLEMENT_HAS_UNRESOLVED_ANOMALIES,
                    unresolved + " unresolved anomalies found in period "
                            + period.format());

        // 3. Pool 1: Vé lượt → direct by operator
        Map<UUID, Money> singleTripShares = allTrips.stream()
                .filter(t -> t.getTicketTypeUsed() == TicketTypeUsed.SINGLE_TRIP
                        && t.getOperatorId() != null
                        && t.getFareAmount() != null)
                .collect(groupingBy(
                        Trip::getOperatorId,
                        Collectors.reducing(
                                Money.of(BigDecimal.ZERO),
                                t -> Money.of(t.getFareAmount()),
                                Money::add
                        )
                ));

        // 4. Pool 2+3: Vé tháng → cần ticket data
        List<Trip> monthlyTrips = allTrips.stream()
                .filter(t -> t.getTicketTypeUsed() == TicketTypeUsed.MONTHLY_PASS
                        && t.getTicketId() != null)
                .toList();

        Set<UUID> ticketIds = monthlyTrips.stream()
                .map(Trip::getTicketId)
                .collect(Collectors.toSet());

        Map<UUID, Ticket> ticketMap = ticketRepository.findAllByIds(ticketIds)
                .stream().collect(toMap(Ticket::getId, t -> t));

        List<TicketRevenueData> monthlyData = monthlyTrips.stream()
                .collect(groupingBy(Trip::getTicketId))
                .entrySet().stream()
                .filter(e -> ticketMap.containsKey(e.getKey()))
                .map(e -> {
                    Ticket ticket = ticketMap.get(e.getKey());
                    List<TripContribution> contribs = e.getValue().stream()
                            .filter(t -> t.getOperatorId() != null)
                            .collect(groupingBy(Trip::getOperatorId))
                            .entrySet().stream()
                            .map(op -> new TripContribution(
                                    op.getKey(),
                                    op.getValue().get(0).getTransportMode(),
                                    op.getValue().size(),
                                    op.getValue().stream()
                                            .map(t -> t.getDistanceKm() != null
                                                    ? t.getDistanceKm()
                                                    : BigDecimal.ZERO)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            ))
                            .toList();
                    return new TicketRevenueData(
                            e.getKey(),
                            ticket.getPrice(),
                            ticket.getMode(),
                            ticket.getScope(),
                            contribs
                    );
                })
                .toList();

        // 5. totalExpected = pool1 + pool2+3
        Money singleTripTotal = singleTripShares.values().stream()
                .reduce(Money.of(BigDecimal.ZERO), Money::add);

        Money monthlyTotal = monthlyData.stream()
                .map(TicketRevenueData::ticketPrice)
                .reduce(Money.of(BigDecimal.ZERO), Money::add);

        Money totalExpected = singleTripTotal.add(monthlyTotal);

        // 6. Fare params từ active FareRule, fallback về default
        Map<FareMode, FareParams> fareParams = new EnumMap<>(FareMode.class);
        fareRuleRepository.findActiveByMode(FareMode.METRO).ifPresent(r ->
                fareParams.put(FareMode.METRO,
                        new FareParams(
                                r.getBaseFare().getAmount(),    // ← .getAmount()
                                r.getRatePerKm().getAmount()    // ← .getAmount()
                        )));
        fareRuleRepository.findActiveByMode(FareMode.BUS).ifPresent(r ->
                fareParams.put(FareMode.BUS,
                        new FareParams(
                                r.getBaseFare().getAmount(),    // ← .getAmount()
                                r.getRatePerKm().getAmount()    // ← .getAmount()
                        )));
        fareParams.putIfAbsent(FareMode.METRO, FareParams.METRO_DEFAULT);
        fareParams.putIfAbsent(FareMode.BUS,   FareParams.BUS_DEFAULT);

        // 7. Operator aggregates cho CompanyShare report
        Map<UUID, BigDecimal> operatorTotalKm = allTrips.stream()
                .filter(t -> t.getOperatorId() != null && t.getDistanceKm() != null)
                .collect(groupingBy(
                        Trip::getOperatorId,
                        Collectors.reducing(BigDecimal.ZERO,
                                Trip::getDistanceKm, BigDecimal::add)
                ));

        Map<UUID, Integer> operatorTripCount = allTrips.stream()
                .filter(t -> t.getOperatorId() != null)
                .collect(groupingBy(
                        Trip::getOperatorId,
                        Collectors.summingInt(t -> 1)
                ));

        // 8. Tạo settlement aggregate
        Settlement settlement = Settlement.create(
                period.format(), totalExpected, DEFAULT_TOLERANCE, ranBy);
        settlementRepository.save(settlement);

        // 9. Domain: allocate + reconcile
        List<CompanyShare> shares = settlement.allocateShares(
                singleTripShares, monthlyData, fareParams,
                operatorTotalKm, operatorTripCount);
        settlement.reconcile(shares);

        // 10. Save shares
        shares.forEach(settlementRepository::saveShare);

        // 11. Log nếu có sai lệch
        if (settlement.getReconciliationStatus() != ReconcileStatus.MATCH) {
            settlementRepository.saveLog(ReconciliationLog.of(
                    settlement.getId(),
                    settlement.getReconciliationStatus().name(),
                    settlement.getDiffAmount().abs(),
                    allTrips.size(),
                    "Settlement reconciliation diff detected",
                    Map.of(
                            "totalExpected", totalExpected.getAmount().toString(),
                            "totalActual",   settlement.getTotalActual().getAmount().toString(),
                            "diffAmount",    settlement.getDiffAmount().toString()
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