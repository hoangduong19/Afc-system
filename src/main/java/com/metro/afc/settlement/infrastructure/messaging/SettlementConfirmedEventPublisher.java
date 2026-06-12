package com.metro.afc.settlement.infrastructure.messaging;

import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.operator.domain.model.Operator;
import com.metro.afc.settlement.application.dto.settlement.SettlementConfirmedEvent;
import com.metro.afc.settlement.application.port.out.SettlementRepository;
import com.metro.afc.settlement.domain.CompanyShare;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.domain.events.settlement.SettlementConfirmedDomainEvent;
import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import com.metro.afc.shared.messaging.CompanyShareMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementConfirmedEventPublisher {

    private final AmqpTemplate amqpTemplate;
    private final SettlementRepository settlementRepository;
    private final OperatorRepository   operatorRepository;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SettlementConfirmedDomainEvent event) {
        Settlement settlement = settlementRepository
                .findById(event.settlementId())
                .orElseThrow();

        List<CompanyShare> shares = settlementRepository
                .findSharesBySettlementId(event.settlementId());

        List<CompanyShareMessage> shareMessages = shares.stream()
                .map(share -> new CompanyShareMessage(
                        share.getOperatorId(),
                        operatorRepository.findById(share.getOperatorId())
                                .map(Operator::getCode).orElse("UNKNOWN"),
                        share.getShareAmount().getAmount(),
                        share.getTotalKm(),
                        share.getTotalTrips(),
                        share.getExpectedRevenue().getAmount()
                )).toList();

        SettlementConfirmedEvent message = new SettlementConfirmedEvent(
                settlement.getId(),
                settlement.getPeriod(),
                shareMessages
        );

        amqpTemplate.convertAndSend(
                RabbitMQConfig.AFC_EXCHANGE,
                RabbitMQConfig.SETTLEMENT_CONFIRMED,
                message
        );

        log.info("Published SettlementConfirmedEvent: period={}",
                settlement.getPeriod());
    }
}