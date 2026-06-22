package com.metro.afc.trip.application;

import com.metro.afc.passenger.dto.PassengerTripResponse;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.trip.application.port.in.TripUseCase;
import com.metro.afc.trip.application.port.out.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripService implements TripUseCase {

    private final TripRepository    tripRepository;
    private final TicketRepository  ticketRepository;
    private final StationRepository stationRepository;

    @Override
    public List<PassengerTripResponse> findByUserId(UUID userId) {
        List<UUID> ticketIds = ticketRepository.findByUserId(userId)
                .stream().map(Ticket::getId).toList();

        if (ticketIds.isEmpty()) return List.of();

        return tripRepository.findByTicketIdIn(ticketIds).stream()
                .map(t -> {
                    String tapInCode = t.getTapInStationId() != null
                            ? stationRepository.findById(t.getTapInStationId())
                            .map(Station::getCode).orElse(null)
                            : null;
                    String tapOutCode = t.getTapOutStationId() != null
                            ? stationRepository.findById(t.getTapOutStationId())
                            .map(Station::getCode).orElse(null)
                            : null;
                    return PassengerTripResponse.from(t, tapInCode, tapOutCode);
                })
                .toList();
    }
}