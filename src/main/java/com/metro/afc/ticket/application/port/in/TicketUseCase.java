package com.metro.afc.ticket.application.port.in;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRule.PassDurationType;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.PassScope;
import com.metro.afc.ticket.domain.enums.TicketStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketUseCase {
    Ticket createSingleTrip(UUID userId, UUID fromStationId,
                            UUID toStationId, FareMode mode,
                            PassengerType passengerType);

    Ticket createPass(UUID userId, FareMode mode, PassScope passScope,
                             PassengerType passengerType,
                             LocalDate validFrom, PassDurationType durationType,
                             Integer durationMonths);

    Ticket linkToCard(UUID ticketId, UUID cardId);

    Ticket unlinkFromCard(UUID ticketId);

    Optional<Ticket> findActiveTicketByCardId(UUID cardId);

    List<Ticket> findByUserId(UUID userId, TicketStatus status);
    Ticket findById(UUID id);
}