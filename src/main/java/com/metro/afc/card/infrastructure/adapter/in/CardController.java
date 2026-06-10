package com.metro.afc.card.infrastructure.adapter.in;

import com.metro.afc.card.application.dto.card.*;
import com.metro.afc.card.application.dto.cardLink.LinkCardRequest;
import com.metro.afc.identity.infrastructure.config.SecurityUtils;
import com.metro.afc.ticket.application.dto.LinkTicketRequest;
import com.metro.afc.ticket.application.dto.TicketResponse;
import com.metro.afc.ticket.infrastructure.adapter.in.TicketFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardFacade cardFacade;
    private final TicketFacade ticketFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('CARD_ISSUE')")
    public ResponseEntity<CardResponse> create(
            @Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardFacade.create(request, SecurityUtils.getCurrentUserId()));
    }

    @PatchMapping("/{id}/issue")
    @PreAuthorize("hasAuthority('CARD_ISSUE')")
    public ResponseEntity<CardResponse> issue(
            @PathVariable UUID id,
            @Valid @RequestBody IssueCardRequest request) {
        return ResponseEntity.ok(
                cardFacade.issue(id, request, SecurityUtils.getCurrentUserId())
        );
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('CARD_ACTIVATE')")
    public ResponseEntity<CardResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(
                cardFacade.activate(id, SecurityUtils.getCurrentUserId())
        );
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('CARD_SUSPEND')")
    public ResponseEntity<CardResponse> suspend(
            @PathVariable UUID id,
            @Valid @RequestBody CardActionRequest request) {
        return ResponseEntity.ok(
                cardFacade.suspend(id, request, SecurityUtils.getCurrentUserId())
        );
    }

    @PatchMapping("/{id}/unsuspend")
    @PreAuthorize("hasAuthority('CARD_SUSPEND')")
    public ResponseEntity<CardResponse> unsuspend(
            @PathVariable UUID id,
            @Valid @RequestBody CardActionRequest request) {
        return ResponseEntity.ok(
                cardFacade.unsuspend(id, request, SecurityUtils.getCurrentUserId())
        );
    }

    @PatchMapping("/{id}/revoke")
    @PreAuthorize("hasAuthority('CARD_REVOKE')")
    public ResponseEntity<CardResponse> revoke(
            @PathVariable UUID id,
            @Valid @RequestBody CardActionRequest request) {
        return ResponseEntity.ok(
                cardFacade.revoke(id, request, SecurityUtils.getCurrentUserId())
        );
    }

    @PatchMapping("/{id}/link")
    @PreAuthorize("hasAuthority('CARD_LINK')")
    public ResponseEntity<CardResponse> link(
            @PathVariable UUID id,
            @Valid @RequestBody LinkCardRequest request) {
        return ResponseEntity.ok(
                cardFacade.link(id, request, SecurityUtils.getCurrentUserId())
        );
    }

    @PatchMapping("/{id}/unlink")
    @PreAuthorize("hasAuthority('CARD_LINK')")
    public ResponseEntity<CardResponse> unlink(@PathVariable UUID id) {
        return ResponseEntity.ok(
                cardFacade.unlink(id, SecurityUtils.getCurrentUserId())
        );
    }

    @PostMapping("/{cardId}/link-ticket")
    @PreAuthorize("hasAuthority('CARD_LINK')")
    public ResponseEntity<TicketResponse> linkTicket(
            @PathVariable UUID cardId,
            @Valid @RequestBody LinkTicketRequest request) {
        return ResponseEntity.ok(ticketFacade.linkToCard(cardId, request));
    }

    @DeleteMapping("/{cardId}/unlink-ticket")
    @PreAuthorize("hasAuthority('CARD_LINK')")
    public ResponseEntity<TicketResponse> unlinkTicket(@PathVariable UUID cardId) {
        return ResponseEntity.ok(ticketFacade.unlinkFromCard(cardId));
    }

    @GetMapping("/{cardId}/active-ticket")
    @PreAuthorize("hasAuthority('CARD_READ')")
    public ResponseEntity<TicketResponse> getActiveTicket(
            @PathVariable UUID cardId) {
        return ResponseEntity.ok(ticketFacade.findActiveTicketByCardId(cardId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CARD_READ')")
    public ResponseEntity<CardResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(cardFacade.findById(id));
    }

    @GetMapping("/uid/{cardUid}")
    @PreAuthorize("hasAuthority('CARD_READ')")
    public ResponseEntity<CardResponse> findByCardUid(@PathVariable String cardUid) {
        return ResponseEntity.ok(cardFacade.findByCardUid(cardUid));
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAuthority('CARD_READ')")
    public ResponseEntity<CardDetailResponse> findDetailById(@PathVariable UUID id) {
        return ResponseEntity.ok(cardFacade.findDetailById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CARD_READ')")
    public ResponseEntity<List<CardResponse>> findAll() {
        return ResponseEntity.ok(cardFacade.findAll());
    }
}
