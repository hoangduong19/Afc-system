package com.metro.afc.settlement.infrastructure.adapter.in;

import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.operator.domain.model.Operator;
import com.metro.afc.settlement.application.dto.settlement.CompanyShareResponse;
import com.metro.afc.settlement.application.dto.settlement.ReconciliationLogResponse;
import com.metro.afc.settlement.application.dto.settlement.RunSettlementRequest;
import com.metro.afc.settlement.application.dto.settlement.SettlementResponse;
import com.metro.afc.settlement.application.port.in.SettlementUseCase;
import com.metro.afc.settlement.application.port.out.SettlementRepository;
import com.metro.afc.settlement.domain.CompanyShare;
import com.metro.afc.settlement.domain.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SettlementFacade {

    private final SettlementUseCase settlementUseCase;
    private final SettlementRepository settlementRepository;
    private final OperatorRepository operatorRepository;

    public SettlementResponse run(RunSettlementRequest req, UUID ranBy) {
        return toResponse(
                settlementUseCase.run(req.month(), req.year(), ranBy));
    }

    public SettlementResponse confirm(UUID id, UUID confirmedBy) {
        return toResponse(settlementUseCase.confirm(id, confirmedBy));
    }

    public SettlementResponse findById(UUID id) {
        return toResponse(settlementUseCase.findById(id));
    }

    public List<SettlementResponse> findAll() {
        return settlementUseCase.findAll().stream()
                .map(this::toResponse).toList();
    }

    public List<ReconciliationLogResponse> findLogs(UUID settlementId) {
        return settlementRepository.findLogsBySettlementId(settlementId)
                .stream()
                .map(ReconciliationLogResponse::from)
                .toList();
    }

    private SettlementResponse toResponse(Settlement s) {
        List<CompanyShare> shares = settlementRepository
                .findSharesBySettlementId(s.getId());

        List<CompanyShareResponse> shareResponses = shares.stream()
                .map(share -> new CompanyShareResponse(
                        share.getOperatorId(),
                        operatorRepository.findById(share.getOperatorId())
                                .map(Operator::getCode).orElse("UNKNOWN"),
                        share.getTotalKm(),
                        share.getTotalTrips(),
                        share.getExpectedRevenue().getAmount(),
                        share.getShareAmount().getAmount(),
                        share.getRoundingAdjustment().getAmount()
                )).toList();

        return new SettlementResponse(
                s.getId(), s.getPeriod(),
                s.getStatus().name(),
                s.getTotalExpected().getAmount(),
                s.getTotalActual() != null
                        ? s.getTotalActual().getAmount() : null,
                s.getDiffAmount(),
                s.getReconciliationStatus() != null
                        ? s.getReconciliationStatus().name() : null,
                s.getToleranceThreshold(),
                shareResponses,
                s.getRanBy(), s.getRanAt(), s.getConfirmedAt()
        );
    }
}