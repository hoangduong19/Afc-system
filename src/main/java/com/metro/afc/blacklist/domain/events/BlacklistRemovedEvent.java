package com.metro.afc.blacklist.domain.events;

import java.util.UUID;

public record BlacklistRemovedEvent(
        UUID blacklistId, UUID cardId, UUID removedBy
) {}