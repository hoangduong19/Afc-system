package com.metro.afc.ticket.application.port.in;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import com.metro.afc.ticket.domain.Ticket;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TicketUseCase {
    Ticket createSingleTrip(UUID userId, UUID fromStationId,
                            UUID toStationId, FareMode mode,
                            PassengerType passengerType);

    Ticket createMonthlyPass(UUID userId, FareMode mode,
                             PassengerType passengerType,
                             LocalDate validFrom, int durationDays);

    Ticket linkToCard(UUID ticketId, UUID cardId);

    Ticket unlinkFromCard(UUID ticketId);

    Optional<Ticket> findActiveTicketByCardId(UUID cardId);
}