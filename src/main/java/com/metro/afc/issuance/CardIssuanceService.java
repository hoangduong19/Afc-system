package com.metro.afc.issuance;

import com.metro.afc.card.application.dto.card.CardResponse;
import com.metro.afc.card.application.dto.card.CreateCardRequest;
import com.metro.afc.card.application.port.in.CardUseCase;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.ticket.application.dto.CreatePassRequest;
import com.metro.afc.ticket.application.dto.TicketResponse;
import com.metro.afc.ticket.application.port.in.TicketUseCase;
import com.metro.afc.ticket.domain.Ticket;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardIssuanceService implements CardIssuanceUseCase {

    private final CardUseCase cardUseCase;
    private final TicketUseCase ticketUseCase;

    @Override
    @Transactional
    public IssueWithTicketResponse issue(IssueWithTicketRequest request, UUID performedBy) {
        CreateCardRequest c = request.card();
        Card card = cardUseCase.create(
                c.cardUid(), c.userId(),
                c.supportsMetro(), c.supportsBus(), performedBy
        );

        if (request.ticket() == null) {
            return new IssueWithTicketResponse(CardResponse.from(card), null);
        }

        CreatePassRequest t = request.ticket();
        Ticket ticket = ticketUseCase.createPass(
                t.userId(), t.mode(), t.scope(), t.passengerType(),
                t.validFrom(), t.durationType(), t.durationMonths()
        );
        Ticket linked = ticketUseCase.linkToCard(ticket.getId(), card.getId());

        return new IssueWithTicketResponse(
                CardResponse.from(card),
                TicketResponse.from(linked, null, null)
        );
    }
}