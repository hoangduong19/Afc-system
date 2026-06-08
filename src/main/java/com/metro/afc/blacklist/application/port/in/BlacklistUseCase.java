package com.metro.afc.blacklist.application.port.in;

import com.metro.afc.blacklist.domain.Blacklist;

import java.util.List;
import java.util.UUID;

public interface BlacklistUseCase {
    Blacklist add(UUID cardId, String reason, UUID addedBy);
    Blacklist remove(UUID blacklistId, UUID removedBy);
    List<Blacklist> findAllActive();
    boolean isBlacklisted(UUID cardId);
}