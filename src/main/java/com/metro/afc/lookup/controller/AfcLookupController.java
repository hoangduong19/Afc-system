package com.metro.afc.lookup.controller;

import com.metro.afc.blacklist.application.port.out.BlacklistRepository;
import com.metro.afc.blacklist.domain.Blacklist;
import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.fare.application.port.out.FareDiscountRepository;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.lookup.dto.AfcBlacklistResponse;
import com.metro.afc.lookup.dto.AfcCardResponse;
import com.metro.afc.lookup.dto.AfcDiscountResponse;
import com.metro.afc.lookup.dto.AfcFareRuleResponse;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/afc")
@RequiredArgsConstructor
public class AfcLookupController {

    private final CardRepository cardRepository;
    private final TicketRepository ticketRepository;
    private final FareRuleRepository fareRuleRepository;
    private final FareDiscountRepository fareDiscountRepository;
    private final BlacklistRepository blacklistRepository;

    // ── GET /api/afc/cards/{cardUid} ────────────────────────────
    @GetMapping("/cards/{cardUid}")
    public ResponseEntity<AfcCardResponse> getCard(
            @PathVariable String cardUid) {
        Card card = cardRepository.findByCardUid(cardUid.toUpperCase())
                .orElseThrow(() -> new NotFoundException(ErrorCode.CARD_NOT_FOUND));

        boolean isBlacklisted = blacklistRepository
                .existsActiveByCardId(card.getId());

        Optional<Ticket> activeTicket = ticketRepository
                .findActiveTicketByCardId(card.getId());

        return ResponseEntity.ok(AfcCardResponse.from(
                card, isBlacklisted, activeTicket.orElse(null)
        ));
    }

    // ── GET /api/afc/fare-rules/active ──────────────────────────
    @GetMapping("/fare-rules/active")
    public ResponseEntity<List<AfcFareRuleResponse>> getActiveFareRules(
            @RequestParam(required = false) FareMode mode) {
        List<FareRule> rules = mode != null
                ? fareRuleRepository.findActiveByMode(mode)
                .map(List::of).orElse(List.of())
                : fareRuleRepository.findAllActive();

        return ResponseEntity.ok(
                rules.stream().map(AfcFareRuleResponse::from).toList()
        );
    }

    // ── GET /api/afc/discounts/active ───────────────────────────
    @GetMapping("/discounts/active")
    public ResponseEntity<List<AfcDiscountResponse>> getActiveDiscounts() {
        return ResponseEntity.ok(
                fareDiscountRepository.findAllActive().stream()
                        .map(AfcDiscountResponse::from).toList()
        );
    }

    // ── GET /api/afc/blacklist/sync ─────────────────────────────
    @GetMapping("/blacklist/sync")
    public ResponseEntity<List<AfcBlacklistResponse>> syncBlacklist(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant since) {
        List<Blacklist> list = since != null
                ? blacklistRepository.findChangedSince(since)
                : blacklistRepository.findAllActive();

        return ResponseEntity.ok(
                list.stream().map(AfcBlacklistResponse::from).toList()
        );
    }
}