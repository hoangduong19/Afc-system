package com.metro.afc.fareRuleCalculation;

import com.metro.afc.fare.application.FareCalculationService;
import com.metro.afc.fare.application.dto.fareRule.calculation.MultimodalFareResponse;
import com.metro.afc.fare.application.dto.fareRule.calculation.MultimodalLegRequest;
import com.metro.afc.fare.application.dto.fareRule.calculation.SingleTripFareResponse;
import com.metro.afc.fare.application.port.out.FareDiscountRepository;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareDiscount;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountType;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FareCalculationService")
class FareCalculationServiceTest {

    // ── Mocks ─────────────────────────────────────────────────────

    @Mock private StationRepository      stationRepository;
    @Mock private FareRuleRepository     fareRuleRepository;
    @Mock private FareDiscountRepository fareDiscountRepository;

    @InjectMocks
    private FareCalculationService fareCalculationService;

    // ── Shared fixtures ───────────────────────────────────────────

    private static final UUID ACTOR = UUID.randomUUID();
    private static final LocalDate VALID_FROM = LocalDate.of(2025, 7, 1);

    private UUID fromId;
    private UUID toId;
    private Station fromStation;
    private Station toStation;
    private FareRule metroFareRule;
    private FareRule busFareRule;

    @BeforeEach
    void setUp() {
        fromId = UUID.randomUUID();
        toId   = UUID.randomUUID();

        fromStation = mock(Station.class);
        when(fromStation.getId()).thenReturn(fromId);
        when(fromStation.getCode()).thenReturn("HN_2A_01");
        when(fromStation.getKmMarker()).thenReturn(new BigDecimal("0.000"));

        toStation = mock(Station.class);
        when(toStation.getId()).thenReturn(toId);
        when(toStation.getCode()).thenReturn("HN_2A_12");
        when(toStation.getKmMarker()).thenReturn(new BigDecimal("12.500"));

        metroFareRule = FareRule.create(
                "HN_METRO_STANDARD", FareMode.METRO,
                new BigDecimal("8000"), new BigDecimal("850"),
                new BigDecimal("8000"), new BigDecimal("30000"),
                List.of(),
                VALID_FROM, null, ACTOR
        );

        busFareRule = FareRule.create(
                "HN_BUS_STANDARD", FareMode.BUS,
                new BigDecimal("3000"), new BigDecimal("450"),
                new BigDecimal("3000"), new BigDecimal("30000"),
                List.of(),
                VALID_FROM, null, ACTOR
        );
    }

    // ── Stub helpers ──────────────────────────────────────────────

    private void stubStations() {
        when(stationRepository.findById(fromId)).thenReturn(Optional.of(fromStation));
        when(stationRepository.findById(toId)).thenReturn(Optional.of(toStation));
    }

    private void stubMetroRule() {
        when(fareRuleRepository.findActiveByMode(FareMode.METRO))
                .thenReturn(Optional.of(metroFareRule));
    }

    private void stubBusRule() {
        when(fareRuleRepository.findActiveByMode(FareMode.BUS))
                .thenReturn(Optional.of(busFareRule));
    }

    private FareDiscount percentDiscount(PassengerType type, BigDecimal pct) {
        return FareDiscount.create(type, DiscountType.PERCENT, pct, VALID_FROM, null, ACTOR);
    }

    private FareDiscount fixedDiscount(PassengerType type, BigDecimal amount) {
        return FareDiscount.create(type, DiscountType.FIXED, amount, VALID_FROM, null, ACTOR);
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertThat(actual.stripTrailingZeros())
                .isEqualByComparingTo(new BigDecimal(expected).stripTrailingZeros());
    }

    // ── calculate — happy paths ───────────────────────────────────

    @Nested
    @DisplayName("calculate — single trip")
    class SingleTrip {

        @Test
        @DisplayName("METRO 12.5 km, no discount → 18625")
        void noDiscount_returnsCorrectFare() {
            stubStations();
            stubMetroRule();

            SingleTripFareResponse r = fareCalculationService.calculate(
                    fromId, toId, FareMode.METRO, null
            );

            assertThat(r.fromStationCode()).isEqualTo("HN_2A_01");
            assertThat(r.toStationCode()).isEqualTo("HN_2A_12");
            assertMoney("12.5",  r.distanceKm());
            assertMoney("8000",  r.baseFare());
            assertMoney("18625", r.calculatedFare());
            assertThat(r.discountApplied()).isFalse();
            assertThat(r.discountType()).isNull();
            assertThat(r.discountValue()).isNull();
            assertMoney("18625", r.finalPrice());
        }

        @Test
        @DisplayName("STUDENT 50% discount → finalPrice = 9312.50")
        void studentDiscount_50pct_halvesFare() {
            stubStations();
            stubMetroRule();
            when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                    .thenReturn(Optional.of(percentDiscount(PassengerType.STUDENT, new BigDecimal("50"))));

            SingleTripFareResponse r = fareCalculationService.calculate(
                    fromId, toId, FareMode.METRO, PassengerType.STUDENT
            );

            assertThat(r.discountApplied()).isTrue();
            assertThat(r.discountType()).isEqualTo("PERCENT");
            assertMoney("50",      r.discountValue());
            assertMoney("9312.50", r.finalPrice());
        }

        @Test
        @DisplayName("SENIOR 100% discount → finalPrice = 0")
        void seniorDiscount_100pct_returnsFree() {
            stubStations();
            stubMetroRule();
            when(fareDiscountRepository.findActiveByPassengerType(PassengerType.SENIOR))
                    .thenReturn(Optional.of(percentDiscount(PassengerType.SENIOR, new BigDecimal("100"))));

            SingleTripFareResponse r = fareCalculationService.calculate(
                    fromId, toId, FareMode.METRO, PassengerType.SENIOR
            );

            assertMoney("0", r.finalPrice());
        }

        @Test
        @DisplayName("FIXED 5000 discount → 18625 − 5000 = 13625")
        void fixedDiscount_subtractedFromFare() {
            stubStations();
            stubMetroRule();
            when(fareDiscountRepository.findActiveByPassengerType(PassengerType.PRIORITY))
                    .thenReturn(Optional.of(fixedDiscount(PassengerType.PRIORITY, new BigDecimal("5000"))));

            SingleTripFareResponse r = fareCalculationService.calculate(
                    fromId, toId, FareMode.METRO, PassengerType.PRIORITY
            );

            assertThat(r.discountApplied()).isTrue();
            assertThat(r.discountType()).isEqualTo("FIXED");
            assertMoney("13625", r.finalPrice());
        }

        @Test
        @DisplayName("FIXED discount > fare should not return negative (clamped by domain)")
        void fixedDiscount_exceedsFare_notNegative() {
            // fare = 18625; discount = 99999 — expect domain to clamp at 0
            stubStations();
            stubMetroRule();
            when(fareDiscountRepository.findActiveByPassengerType(PassengerType.PRIORITY))
                    .thenReturn(Optional.of(fixedDiscount(PassengerType.PRIORITY, new BigDecimal("99999"))));

            SingleTripFareResponse r = fareCalculationService.calculate(
                    fromId, toId, FareMode.METRO, PassengerType.PRIORITY
            );

            assertThat(r.finalPrice()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("passengerType provided but no active discount → no discount applied")
        void noActiveDiscount_discountFlagFalse() {
            stubStations();
            stubMetroRule();
            when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                    .thenReturn(Optional.empty());

            SingleTripFareResponse r = fareCalculationService.calculate(
                    fromId, toId, FareMode.METRO, PassengerType.STUDENT
            );

            assertThat(r.discountApplied()).isFalse();
            assertMoney("18625", r.finalPrice());
        }
    }

    // ── calculate — fare clamping ─────────────────────────────────

    @Nested
    @DisplayName("calculate — fare boundary clamping")
    class FareClamping {

        @Test
        @DisplayName("100 km distance clamps to maxPrice 30000")
        void longDistance_clampsToMax() {
            when(toStation.getKmMarker()).thenReturn(new BigDecimal("100.000"));
            stubStations();
            stubMetroRule();

            SingleTripFareResponse r = fareCalculationService.calculate(
                    fromId, toId, FareMode.METRO, null
            );

            assertMoney("30000", r.finalPrice());
        }

        @Test
        @DisplayName("Reverse direction (from > to) returns positive distance and same fare")
        void reverseDirection_positiveDistance() {
            when(fromStation.getKmMarker()).thenReturn(new BigDecimal("12.500"));
            when(toStation.getKmMarker()).thenReturn(new BigDecimal("0.000"));
            stubStations();
            stubMetroRule();

            SingleTripFareResponse r = fareCalculationService.calculate(
                    fromId, toId, FareMode.METRO, null
            );

            assertMoney("12.5",  r.distanceKm());
            assertMoney("18625", r.finalPrice());
        }
    }

    // ── calculate — exception cases ───────────────────────────────

    @Nested
    @DisplayName("calculate — exceptions")
    class Exceptions {

        @Test
        @DisplayName("from station not found → NotFoundException")
        void fromStationNotFound_throws() {
            when(stationRepository.findById(fromId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    fareCalculationService.calculate(fromId, toId, FareMode.METRO, null))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("to station not found → NotFoundException")
        void toStationNotFound_throws() {
            when(stationRepository.findById(fromId)).thenReturn(Optional.of(fromStation));
            when(stationRepository.findById(toId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    fareCalculationService.calculate(fromId, toId, FareMode.METRO, null))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("same station (fromId == toId) → BusinessRuleException")
        void sameStation_throws() {
            when(stationRepository.findById(fromId)).thenReturn(Optional.of(fromStation));

            assertThatThrownBy(() ->
                    fareCalculationService.calculate(fromId, fromId, FareMode.METRO, null))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("no active fare rule for mode → NotFoundException")
        void noFareRule_throws() {
            stubStations();
            when(fareRuleRepository.findActiveByMode(FareMode.METRO))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    fareCalculationService.calculate(fromId, toId, FareMode.METRO, null))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ── calculateMultimodal ───────────────────────────────────────

    @Nested
    @DisplayName("calculateMultimodal")
    class Multimodal {

        /** Convenience: bus leg from (0 km) → (12.4 km) */
        private UUID busFromId;
        private UUID busToId;
        private Station busFrom;
        private Station busTo;

        @BeforeEach
        void setUpBusLeg() {
            busFromId = UUID.randomUUID();
            busToId   = UUID.randomUUID();

            busFrom = mock(Station.class);
            busTo   = mock(Station.class);
            when(busFrom.getCode()).thenReturn("BRT01_01");
            when(busTo.getCode()).thenReturn("BRT01_09");
            when(busFrom.getKmMarker()).thenReturn(new BigDecimal("0.000"));
            when(busTo.getKmMarker()).thenReturn(new BigDecimal("12.400"));

            when(stationRepository.findById(busFromId)).thenReturn(Optional.of(busFrom));
            when(stationRepository.findById(busToId)).thenReturn(Optional.of(busTo));
        }

        private void stubFullMultimodal() {
            when(stationRepository.findById(fromId)).thenReturn(Optional.of(fromStation));
            when(stationRepository.findById(toId)).thenReturn(Optional.of(toStation));
            stubBusRule();
            stubMetroRule();
        }

        private List<MultimodalLegRequest> busAndMetroLegs() {
            return List.of(
                    new MultimodalLegRequest(busFromId, busToId, FareMode.BUS),
                    new MultimodalLegRequest(fromId, toId, FareMode.METRO)
            );
        }

        @Test
        @DisplayName("null legs → BusinessRuleException")
        void nullLegs_throws() {
            assertThatThrownBy(() ->
                    fareCalculationService.calculateMultimodal(null, null))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("empty legs → BusinessRuleException")
        void emptyLegs_throws() {
            assertThatThrownBy(() ->
                    fareCalculationService.calculateMultimodal(List.of(), null))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("single leg → BusinessRuleException (minimum 2 required)")
        void singleLeg_throws() {
            assertThatThrownBy(() ->
                    fareCalculationService.calculateMultimodal(
                            List.of(new MultimodalLegRequest(fromId, toId, FareMode.METRO)), null))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("BUS 12.4 km + METRO 12.5 km, no discount → total 27205")
        void busAndMetro_noDiscount_correctTotal() {
            // BUS:   3000 + 12.4×450 = 3000 + 5580 = 8580
            // METRO: 8000 + 12.5×850 = 8000 + 10625 = 18625
            // total: 27205
            stubFullMultimodal();

            MultimodalFareResponse r = fareCalculationService
                    .calculateMultimodal(busAndMetroLegs(), null);

            assertThat(r.legs()).hasSize(2);
            assertMoney("8580",  r.legs().get(0).fare());
            assertMoney("18625", r.legs().get(1).fare());
            assertMoney("27205", r.totalFare());
            assertThat(r.discountApplied()).isFalse();
            assertMoney("27205", r.finalPrice());
        }

        @Test
        @DisplayName("STUDENT 50% discount applied once on total → 13602.50")
        void studentDiscount_appliedOnTotal() {
            stubFullMultimodal();
            when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                    .thenReturn(Optional.of(
                            percentDiscount(PassengerType.STUDENT, new BigDecimal("50"))
                    ));

            MultimodalFareResponse r = fareCalculationService
                    .calculateMultimodal(busAndMetroLegs(), PassengerType.STUDENT);

            assertThat(r.discountApplied()).isTrue();
            assertMoney("27205",   r.totalFare());
            assertMoney("13602.50", r.finalPrice());
        }

        @Test
        @DisplayName("SENIOR 100% discount on multimodal → finalPrice = 0")
        void seniorDiscount_multimodal_free() {
            stubFullMultimodal();
            when(fareDiscountRepository.findActiveByPassengerType(PassengerType.SENIOR))
                    .thenReturn(Optional.of(
                            percentDiscount(PassengerType.SENIOR, new BigDecimal("100"))
                    ));

            MultimodalFareResponse r = fareCalculationService
                    .calculateMultimodal(busAndMetroLegs(), PassengerType.SENIOR);

            assertMoney("0", r.finalPrice());
        }

        @Test
        @DisplayName("No active discount for passengerType → discount not applied")
        void noActiveDiscount_multimodal_notApplied() {
            stubFullMultimodal();
            when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                    .thenReturn(Optional.empty());

            MultimodalFareResponse r = fareCalculationService
                    .calculateMultimodal(busAndMetroLegs(), PassengerType.STUDENT);

            assertThat(r.discountApplied()).isFalse();
            assertMoney("27205", r.finalPrice());
        }

        @Test
        @DisplayName("Station not found in a leg → NotFoundException")
        void legStationNotFound_throws() {
            // busFrom is fine, busTo is not
            when(stationRepository.findById(busFromId)).thenReturn(Optional.of(busFrom));
            when(stationRepository.findById(busToId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    fareCalculationService.calculateMultimodal(busAndMetroLegs(), null))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("No active fare rule for a leg mode → NotFoundException")
        void legNoFareRule_throws() {
            when(stationRepository.findById(busFromId)).thenReturn(Optional.of(busFrom));
            when(stationRepository.findById(busToId)).thenReturn(Optional.of(busTo));
            when(fareRuleRepository.findActiveByMode(FareMode.BUS))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    fareCalculationService.calculateMultimodal(busAndMetroLegs(), null))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Three legs: BUS + BUS + METRO totals correctly")
        void threeLegs_correctTotal() {
            // Second bus leg: from (0) → (5.0)
            UUID bus2FromId = UUID.randomUUID();
            UUID bus2ToId   = UUID.randomUUID();
            Station bus2From = mock(Station.class);
            Station bus2To   = mock(Station.class);
            when(bus2From.getCode()).thenReturn("BRT02_01");
            when(bus2To.getCode()).thenReturn("BRT02_05");
            when(bus2From.getKmMarker()).thenReturn(new BigDecimal("0.000"));
            when(bus2To.getKmMarker()).thenReturn(new BigDecimal("5.000"));
            when(stationRepository.findById(bus2FromId)).thenReturn(Optional.of(bus2From));
            when(stationRepository.findById(bus2ToId)).thenReturn(Optional.of(bus2To));

            stubFullMultimodal();

            // BUS leg 2: 3000 + 5.0×450 = 5250
            List<MultimodalLegRequest> legs = List.of(
                    new MultimodalLegRequest(busFromId, busToId, FareMode.BUS),    // 8580
                    new MultimodalLegRequest(bus2FromId, bus2ToId, FareMode.BUS),  // 5250
                    new MultimodalLegRequest(fromId, toId, FareMode.METRO)         // 18625
            );

            MultimodalFareResponse r = fareCalculationService
                    .calculateMultimodal(legs, null);

            assertThat(r.legs()).hasSize(3);
            assertMoney("8580",  r.legs().get(0).fare());
            assertMoney("5250",  r.legs().get(1).fare());
            assertMoney("18625", r.legs().get(2).fare());
            assertMoney("32455", r.totalFare());
        }
    }
}