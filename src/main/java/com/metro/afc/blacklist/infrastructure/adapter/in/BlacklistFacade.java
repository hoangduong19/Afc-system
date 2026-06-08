package com.metro.afc.blacklist.infrastructure.adapter.in;

import com.metro.afc.blacklist.application.dto.AddToBlacklistRequest;
import com.metro.afc.blacklist.application.dto.BlacklistResponse;
import com.metro.afc.blacklist.application.port.in.BlacklistUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BlacklistFacade {

    private final BlacklistUseCase blacklistUseCase;

    public BlacklistResponse add(AddToBlacklistRequest request, UUID addedBy) {
        return BlacklistResponse.from(
                blacklistUseCase.add(request.cardId(), request.reason(), addedBy)
        );
    }

    public BlacklistResponse remove(UUID blacklistId, UUID removedBy) {
        return BlacklistResponse.from(
                blacklistUseCase.remove(blacklistId, removedBy)
        );
    }

    public List<BlacklistResponse> findAllActive() {
        return blacklistUseCase.findAllActive().stream()
                .map(BlacklistResponse::from).toList();
    }
}