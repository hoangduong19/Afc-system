package com.metro.afc.card.domain.events.cardLink;

import java.util.UUID;

public record CardLinkedEvent(
        UUID cardId,
        UUID userId,
        UUID performedBy
) {}