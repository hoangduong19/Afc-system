package com.metro.afc.issuance;

import com.metro.afc.card.application.dto.card.CreateCardRequest;
import com.metro.afc.card.application.port.in.CardUseCase;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRule.PassDurationType;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.ticket.application.dto.CreatePassRequest;
import com.metro.afc.ticket.application.port.in.TicketUseCase;
import com.metro.afc.ticket.domain.Ticket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardIssuanceServiceTest {

    @Mock private CardUseCase   cardUseCase;
    @Mock private TicketUseCase ticketUseCase;

    @InjectMocks
    private CardIssuanceService cardIssuanceService;

    private final UUID ADMIN_ID = UUID.randomUUID();
    private final UUID USER_ID  = UUID.randomUUID();

    private Card mockCard() {
        return Card.create("VMS-TEST", USER_ID, true, false, ADMIN_ID);
    }

    private Ticket mockTicket(UUID cardId) {
        Ticket t = Ticket.createMonthlyPass(
                USER_ID, FareMode.METRO, null, null,
                Money.of(BigDecimal.valueOf(200000)),
                UUID.randomUUID(), null,
                LocalDate.now().plusDays(1), 30
        );
        t.linkToCard(cardId);
        return t;
    }

    // ── card only ────────────────────────────────────────────────

    @Test
    void issue_cardOnly_returnsCardWithNullTicket() {
        Card card = mockCard();
        when(cardUseCase.create(any(), any(), any(), any(), any()))
                .thenReturn(card);

        IssueWithTicketRequest request = new IssueWithTicketRequest(
                new CreateCardRequest(null, USER_ID, true, false),
                null
        );

        IssueWithTicketResponse result = cardIssuanceService.issue(request, ADMIN_ID);

        assertThat(result.card()).isNotNull();
        assertThat(result.ticket()).isNull();
        verify(ticketUseCase, never()).createPass(any(), any(), any(), any(), any(), any(), any(), any());
        verify(ticketUseCase, never()).linkToCard(any(), any());
    }

    // ── card + ticket ────────────────────────────────────────────

    @Test
    void issue_cardWithTicket_returnsLinkedCardAndTicket() {
        Card card     = mockCard();
        Ticket ticket = mockTicket(card.getId());

        when(cardUseCase.create(any(), any(), any(), any(), any()))
                .thenReturn(card);
        when(ticketUseCase.createPass(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ticket);
        when(ticketUseCase.linkToCard(ticket.getId(), card.getId()))
                .thenReturn(ticket);

        CreatePassRequest passRequest = new CreatePassRequest(
                USER_ID, FareMode.METRO, null, null, null,
                LocalDate.now().plusDays(1), PassDurationType.MONTHLY, 1
        );
        IssueWithTicketRequest request = new IssueWithTicketRequest(
                new CreateCardRequest(null, USER_ID, true, false),
                passRequest
        );

        IssueWithTicketResponse result = cardIssuanceService.issue(request, ADMIN_ID);

        assertThat(result.card()).isNotNull();
        assertThat(result.ticket()).isNotNull();
        verify(ticketUseCase).createPass(
                eq(USER_ID), eq(FareMode.METRO), isNull(), isNull(), isNull(),
                any(), eq(PassDurationType.MONTHLY), eq(1)
        );
        verify(ticketUseCase).linkToCard(ticket.getId(), card.getId());
    }

    // ── ticket creation fails ────────────────────────────────────

    @Test
    void issue_ticketCreationFails_rollsBack() {
        Card card = mockCard();
        when(cardUseCase.create(any(), any(), any(), any(), any()))
                .thenReturn(card);
        when(ticketUseCase.createPass(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new NotFoundException(ErrorCode.FARE_RULE_NOT_FOUND));

        IssueWithTicketRequest request = new IssueWithTicketRequest(
                new CreateCardRequest(null, USER_ID, true, false),
                new CreatePassRequest(USER_ID, FareMode.METRO, null, null, null,
                        LocalDate.now().plusDays(1), PassDurationType.MONTHLY, 1)
        );

        assertThatThrownBy(() -> cardIssuanceService.issue(request, ADMIN_ID))
                .isInstanceOf(NotFoundException.class);
        verify(ticketUseCase, never()).linkToCard(any(), any());
    }
}