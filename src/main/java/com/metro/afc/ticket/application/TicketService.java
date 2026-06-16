package com.metro.afc.ticket.application;

import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.card.domain.model.enums.CardStatus;
import com.metro.afc.fare.application.port.out.FareDiscountRepository;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareDiscount;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRule.PassDurationType;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.ticket.application.port.in.TicketUseCase;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.PassScope;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService implements TicketUseCase {

    private final TicketRepository ticketRepository;
    private final CardRepository cardRepository;
    private final StationRepository stationRepository;
    private final FareRuleRepository fareRuleRepository;
    private final FareDiscountRepository fareDiscountRepository;

    // ── Single trip ──────────────────────────────────────────────

    @Override
    @Transactional
    public Ticket createSingleTrip(UUID userId, UUID fromStationId,
                                   UUID toStationId, FareMode mode,
                                   PassengerType passengerType) {
        Station from = stationRepository.findById(fromStationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STATION_NOT_FOUND));
        Station to = stationRepository.findById(toStationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STATION_NOT_FOUND));

        if (fromStationId.equals(toStationId))
            throw new BusinessRuleException(
                    ErrorCode.STATION_SAME, "From and to station must be different"
            );

        FareRule fareRule = fareRuleRepository.findActiveByMode(mode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FARE_RULE_NOT_FOUND));

        BigDecimal distanceKm = to.getKmMarker()
                .subtract(from.getKmMarker()).abs();
        Money calculatedFare = fareRule.calculateFare(distanceKm);

        UUID discountId  = null;
        Money finalPrice = calculatedFare;

        if (passengerType != null) {
            Optional<FareDiscount> discount = fareDiscountRepository
                    .findActiveByPassengerType(passengerType);
            if (discount.isPresent()) {
                finalPrice = discount.get().applyTo(calculatedFare);
                discountId = discount.get().getId();
            }
        }

        return ticketRepository.save(Ticket.createSingleTrip(
                userId, fromStationId, toStationId,
                mode, finalPrice, fareRule.getId(), discountId
        ));
    }

    // ── Monthly pass ─────────────────────────────────────────────

    @Override
    @Transactional
    public Ticket createPass(UUID userId, FareMode mode, PassScope scope,
                                    PassengerType passengerType,
                                    LocalDate validFrom,
                                    PassDurationType durationType,
                                    Integer durationMonths) {

        FareRule fareRule = fareRuleRepository.findActiveByMode(mode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FARE_RULE_NOT_FOUND));

        Money price = fareRule.lookupPassPrice(durationType, durationMonths, scope);

        UUID discountId = null;
        if (passengerType != null) {
            Optional<FareDiscount> discount = fareDiscountRepository
                    .findActiveByPassengerType(passengerType);
            if (discount.isPresent()) {
                price      = discount.get().applyTo(price);
                discountId = discount.get().getId();
            }
        }

        return ticketRepository.save(Ticket.createMonthlyPass(
                userId, mode, scope, price, fareRule.getId(),
                discountId, validFrom, toDays(durationType, durationMonths)
        ));
    }

    // ── Link ticket to card ──────────────────────────────────────

    @Override
    @Transactional
    public Ticket linkToCard(UUID ticketId, UUID cardId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TICKET_NOT_FOUND));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.CARD_NOT_FOUND));

        if (card.getStatus() != CardStatus.ACTIVE)
            throw new BusinessRuleException(ErrorCode.CARD_NOT_ACTIVE);

        if (ticketRepository.existsActiveTicketByCardId(cardId))
            throw new ConflictException(ErrorCode.TICKET_ALREADY_LINKED);

        ticket.linkToCard(cardId);
        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public Ticket unlinkFromCard(UUID cardId) {
        Ticket ticket = ticketRepository.findActiveTicketByCardId(cardId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TICKET_NOT_FOUND));
        ticket.unlinkFromCard();
        return ticketRepository.save(ticket);
    }

    @Override
    public Optional<Ticket> findActiveTicketByCardId(UUID cardId) {
        return ticketRepository.findActiveTicketByCardId(cardId);
    }

    private int toDays(PassDurationType type, Integer months) {
        return switch (type) {
            case DAILY   -> 1;
            case WEEKLY  -> 7;
            case MONTHLY -> months * 30;
        };
    }
}