package com.metro.afc.ticket.infrastructure.adapter.in;

import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.ticket.application.dto.CreatePassRequest;
import com.metro.afc.ticket.application.dto.TicketResponse;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.enums.TicketStatus;
import com.metro.afc.ticket.domain.enums.TicketType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {

    private final TicketRepository ticketRepository;
    private final TicketFacade      ticketFacade;
    private final StationRepository stationRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('TICKET_READ')")
    public ResponseEntity<Page<TicketResponse>> findAll(
            @RequestParam(required = false) TicketType type,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("purchasedAt").descending());

        return ResponseEntity.ok(
                ticketRepository.findAllWithFilters(
                                type, status, fromDate, toDate, pageable)
                        .map(t -> TicketResponse.from(t,
                                getStationCode(t.getFromStationId()),
                                getStationCode(t.getToStationId())))
        );
    }

    @PostMapping("/issue")
    @PreAuthorize("hasAuthority('TICKET_PURCHASE')")
    public ResponseEntity<TicketResponse> issue(
            @Valid @RequestBody CreatePassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketFacade.createMonthlyPass(
                        request));
    }

    private String getStationCode(UUID stationId) {
        if (stationId == null) return null;
        return stationRepository.findById(stationId)
                .map(Station::getCode).orElse(null);
    }
}