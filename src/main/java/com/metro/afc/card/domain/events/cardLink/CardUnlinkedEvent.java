package com.metro.afc.card.domain.events.cardLink;

import java.util.UUID;

public record CardUnlinkedEvent(
        UUID cardId,
        UUID previousUserId,
        UUID performedBy
) {}
