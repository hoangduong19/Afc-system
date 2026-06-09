package com.metro.afc.ticket.infrastructure.adapter.in;

import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.ticket.application.dto.CreateMonthlyPassRequest;
import com.metro.afc.ticket.application.dto.CreateSingleTripTicketRequest;
import com.metro.afc.ticket.application.dto.LinkTicketRequest;
import com.metro.afc.ticket.application.dto.TicketResponse;
import com.metro.afc.ticket.application.port.in.TicketUseCase;
import com.metro.afc.ticket.domain.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TicketFacade {

    private final TicketUseCase ticketUseCase;
    private final StationRepository stationRepository;

    public TicketResponse createSingleTrip(CreateSingleTripTicketRequest req,
                                           UUID userId) {
        Ticket ticket = ticketUseCase.createSingleTrip(
                userId, req.fromStationId(), req.toStationId(),
                req.mode(), req.passengerType()
        );
        return TicketResponse.from(ticket,
                getStationCode(req.fromStationId()),
                getStationCode(req.toStationId())
        );
    }

    public TicketResponse createMonthlyPass(CreateMonthlyPassRequest req,
                                            UUID userId) {
        Ticket ticket = ticketUseCase.createMonthlyPass(
                userId, req.mode(), req.passengerType(),
                req.validFrom(), req.durationDays()
        );
        return TicketResponse.from(ticket, null, null);
    }

    public TicketResponse linkToCard(UUID cardId, LinkTicketRequest req) {
        Ticket ticket = ticketUseCase.linkToCard(req.ticketId(), cardId);
        return TicketResponse.from(ticket, null, null);
    }

    public TicketResponse findActiveTicketByCardId(UUID cardId) {
        return ticketUseCase.findActiveTicketByCardId(cardId)
                .map(t -> TicketResponse.from(t, null, null))
                .orElseThrow(() -> new NotFoundException(ErrorCode.TICKET_NOT_FOUND));
    }

    private String getStationCode(UUID stationId) {
        if (stationId == null) return null;
        return stationRepository.findById(stationId)
                .map(Station::getCode).orElse("");
    }
}