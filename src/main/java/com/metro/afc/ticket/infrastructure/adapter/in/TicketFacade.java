package com.metro.afc.ticket.infrastructure.adapter.in;

import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.ticket.application.dto.CreatePassRequest;
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

    public TicketResponse createSingleTrip(CreateSingleTripTicketRequest req) {
        Ticket ticket = ticketUseCase.createSingleTrip(
                req.userId(), req.fromStationId(), req.toStationId(),
                req.mode()
        );
        return TicketResponse.from(ticket,
                getStationCode(req.fromStationId()),
                getStationCode(req.toStationId())
        );
    }

    public TicketResponse createMonthlyPass(CreatePassRequest req) {
        Ticket ticket = ticketUseCase.createPass(
                req.userId(), req.mode(), req.scope(), req.routeId(), req.passengerType(),
                req.validFrom(), req.durationType(), req.durationMonths()
        );
        return TicketResponse.from(ticket, null, null);
    }

    public TicketResponse linkToCard(UUID cardId, LinkTicketRequest req) {
        Ticket ticket = ticketUseCase.linkToCard(req.ticketId(), cardId);
        return TicketResponse.from(ticket, null, null);
    }

    public TicketResponse unlinkFromCard(UUID ticketId) {
        return TicketResponse.from(
                ticketUseCase.unlinkFromCard(ticketId), null, null
        );
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