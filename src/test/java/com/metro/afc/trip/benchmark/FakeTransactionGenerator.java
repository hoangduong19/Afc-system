package com.metro.afc.trip.benchmark;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.trip.application.dto.TransactionItemRequest;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class FakeTransactionGenerator {

    public static List<TransactionItemRequest> generate(
            int count,
            List<String> stationCodes,
            List<String> operatorCodes,
            List<String> cardUids) {

        List<TransactionItemRequest> items = new ArrayList<>(count);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Instant now = Instant.now();

        for (int i = 0; i < count; i++) {
            String tapIn = stationCodes.get(rnd.nextInt(stationCodes.size()));
            String tapOut = rnd.nextBoolean()
                    ? stationCodes.get(rnd.nextInt(stationCodes.size()))
                    : null; // 1 số trip chưa tap-out để test nhánh null

            Instant tapInAt = now.minus(rnd.nextInt(1, 24), ChronoUnit.HOURS);
            Instant tapOutAt = tapOut != null
                    ? tapInAt.plus(rnd.nextInt(5, 60), ChronoUnit.MINUTES)
                    : null;

            items.add(new TransactionItemRequest(
                    UUID.randomUUID(),
                    rnd.nextInt(100) < 90 ? cardUids.get(rnd.nextInt(cardUids.size())) : null,
                    null, // ticketId - để null cho đơn giản
                    operatorCodes.get(rnd.nextInt(operatorCodes.size())),
                    "L" + rnd.nextInt(1, 4),
                    tapIn,
                    tapInAt,
                    tapOut,
                    tapOutAt,
                    tapOut != null ? BigDecimal.valueOf(rnd.nextDouble(1, 30)).setScale(2, RoundingMode.HALF_UP) : null,
                    tapOut != null ? BigDecimal.valueOf(rnd.nextInt(7000, 20000)) : null,
                    FareMode.values()[rnd.nextInt(FareMode.values().length)],
                    tapOut != null ? TicketTypeUsed.SINGLE_TRIP : null
            ));
        }
        return items;
    }
}