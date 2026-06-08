package com.metro.afc.blacklist.application;

import com.metro.afc.blacklist.application.port.in.BlacklistUseCase;
import com.metro.afc.blacklist.application.port.out.BlacklistRepository;
import com.metro.afc.blacklist.domain.Blacklist;
import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlacklistService implements BlacklistUseCase {

    private final BlacklistRepository blacklistRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional
    public Blacklist add(UUID cardId, String reason, UUID addedBy) {
        if (!cardRepository.existsById(cardId)) {
            throw new NotFoundException(ErrorCode.CARD_NOT_FOUND);
        }
        if (blacklistRepository.existsActiveByCardId(cardId)) {
            throw new ConflictException(ErrorCode.CARD_ALREADY_BLACKLISTED);
        }
        return blacklistRepository.save(Blacklist.add(cardId, reason, addedBy));
    }

    @Override
    @Transactional
    public Blacklist remove(UUID blacklistId, UUID removedBy) {
        Blacklist blacklist = blacklistRepository.findById(blacklistId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BLACKLIST_NOT_FOUND));
        blacklist.remove(removedBy);
        return blacklistRepository.save(blacklist);
    }

    @Override
    public List<Blacklist> findAllActive() {
        return blacklistRepository.findAllActive();
    }

    @Override
    public boolean isBlacklisted(UUID cardId) {
        return blacklistRepository.existsActiveByCardId(cardId);
    }
}
