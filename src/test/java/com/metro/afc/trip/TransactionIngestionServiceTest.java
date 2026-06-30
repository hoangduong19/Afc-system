package com.metro.afc.trip;

import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.fare.application.FareCalculationService;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.trip.application.TransactionIngestionService;
import com.metro.afc.trip.application.dto.TransactionBatchRequest;
import com.metro.afc.trip.application.dto.TransactionItemRequest;
import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionIngestionServiceTest {

    @Mock private TripRepository tripRepository;
    @Mock private CardRepository cardRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private StationRepository stationRepository;
    @Mock private OperatorRepository operatorRepository;
    @Mock
    private FareRuleRepository fareRuleRepository;
    @Mock private FareCalculationService fareCalculationService;
    @Mock private TripAnomalyRepository anomalyRepository;

    @InjectMocks
    private TransactionIngestionService service;

    @Test
    void shouldCallSaveAllOnceNotPerItem() {
        // given: 100 transaction giả, không trùng, station/operator hợp lệ
        List<TransactionItemRequest> items = buildFakeItems(100);

        when(stationRepository.findAll()).thenReturn(List.of(/* fake stations */));
        when(operatorRepository.findAll()).thenReturn(List.of(/* fake operators */));
        when(fareRuleRepository.findAllActive()).thenReturn(List.of());
        when(tripRepository.findExistingExternalTransactionIds(any())).thenReturn(Set.of());
        when(cardRepository.findByCardUidIn(any())).thenReturn(List.of());
        lenient().when(ticketRepository.findAllByIds(any())).thenReturn(List.of());


        // when
        service.ingest(new TransactionBatchRequest(items));

        // then: saveAll chỉ gọi 1 lần (vì 100 < CHUNK_SIZE = 1000), không phải save() từng item
        verify(tripRepository, times(1)).saveAll(any());
        verify(tripRepository, never()).save(any());

        // và verify KHÔNG có query nào gọi theo từng item:
        verify(stationRepository, times(1)).findAll();      // 1 lần, không phải 100 lần
        verify(cardRepository, times(1)).findByCardUidIn(any());
    }
    private List<TransactionItemRequest> buildFakeItems(int count) {
        List<TransactionItemRequest> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            items.add(new TransactionItemRequest(
                    UUID.randomUUID(),              // transactionId
                    "CARD-" + i,                    // cardUid
                    null,                            // ticketId
                    "OP01",                          // operatorCode
                    "L1",                            // lineCode
                    "ST01",                          // tapInStationCode
                    Instant.now(),                   // tapInAt
                    "ST02",                          // tapOutStationCode
                    Instant.now().plusSeconds(600),  // tapOutAt
                    BigDecimal.valueOf(5.0),         // distanceKm
                    BigDecimal.valueOf(8000),        // fareAmount
                    FareMode.METRO,                  // mode — đổi theo enum thật của m
                    TicketTypeUsed.SINGLE_TRIP        // ticketType
            ));
        }
        return items;
    }
}
