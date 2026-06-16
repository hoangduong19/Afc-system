package com.metro.afc.card;

import com.metro.afc.card.application.CardService;
import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.application.port.out.CardStatusHistoryRepository;
import com.metro.afc.card.application.port.out.CardUidGeneratorPort;
import com.metro.afc.card.domain.model.enums.CardType;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.station.application.port.out.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    CardRepository cardRepository;
    @Mock
    CardUidGeneratorPort cardUidGenerator;
    @Mock
    StationRepository stationRepository;
    @Mock
    CardStatusHistoryRepository cardStatusHistoryRepository;

    @InjectMocks
    CardService cardService;

    @Test
    void create_withProvidedUid_doesNotCallGenerator() {
        when(cardRepository.existsByCardUid("ABC123")).thenReturn(false);
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cardService.create("abc123", CardType.ANON, null, true, false, UUID.randomUUID());

        verify(cardUidGenerator, never()).generate();
        verify(cardRepository).existsByCardUid("ABC123"); // đã uppercase
    }

    @Test
    void create_withNullUid_callsGenerator() {
        when(cardUidGenerator.generate()).thenReturn("VMS-A1B2C3D4");
        when(cardRepository.existsByCardUid("VMS-A1B2C3D4")).thenReturn(false);
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cardService.create(null, CardType.ANON, null, true, false, UUID.randomUUID());

        verify(cardUidGenerator).generate();
    }

    @Test
    void create_duplicateUid_throwsConflict() {
        when(cardRepository.existsByCardUid("ABC123")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                cardService.create("ABC123", CardType.ANON, null, true, false, UUID.randomUUID())
        );
        verify(cardRepository, never()).save(any());
    }
}