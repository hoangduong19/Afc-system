package com.metro.afc.issuance;

import java.util.UUID;

public interface CardIssuanceUseCase {
    IssueWithTicketResponse issue(IssueWithTicketRequest request, UUID performedBy);
}