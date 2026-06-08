package com.metro.afc.blacklist.domain.events;

import java.util.UUID;

public record BlacklistAddedEvent(
        UUID blacklistId, UUID cardId, String reason, UUID addedBy
) {}