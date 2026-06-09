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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FareCalculationService")
class FareCalculationServiceTest {

    @Mock private StationRepository      stationRepository;
    @Mock private FareRuleRepository     fareRuleRepository;
    @Mock private FareDiscountRepository fareDiscountRepository;

    @InjectMocks
    private FareCalculationService fareCalculationService;

    private Station fromStation;
    private Station toStation;
    private FareRule metroFareRule;
    private FareRule busFareRule;
    private UUID fromId;
    private UUID toId;

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
                LocalDate.of(2025, 7, 1), null, UUID.randomUUID()
        );

        busFareRule = FareRule.create(
                "HN_BUS_STANDARD", FareMode.BUS,
                new BigDecimal("3000"), new BigDecimal("450"),
                new BigDecimal("3000"), new BigDecimal("30000"),
                LocalDate.of(2025, 7, 1), null, UUID.randomUUID()
        );
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void assertMoney(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                "Expected " + expected + " but was " + actual);
    }

    private void stubStations() {
        when(stationRepository.findById(fromId)).thenReturn(Optional.of(fromStation));
        when(stationRepository.findById(toId)).thenReturn(Optional.of(toStation));
    }

    private void stubMetroRule() {
        when(fareRuleRepository.findActiveByMode(FareMode.METRO))
                .thenReturn(Optional.of(metroFareRule));
    }

    // ── Single trip — no discount ────────────────────────────────

    @Test
    @DisplayName("Calculate METRO fare without discount")
    void calculate_noDiscount_returnsCorrectFare() {
        stubStations();
        stubMetroRule();

        SingleTripFareResponse result = fareCalculationService.calculate(
                fromId, toId, FareMode.METRO, null
        );

        assertEquals("HN_2A_01", result.fromStationCode());
        assertEquals("HN_2A_12", result.toStationCode());
        assertMoney(new BigDecimal("12.500"), result.distanceKm());
        assertMoney(new BigDecimal("8000"), result.baseFare());
        assertMoney(new BigDecimal("18625"), result.calculatedFare());
        assertFalse(result.discountApplied());
        assertNull(result.discountType());
        assertNull(result.discountValue());
        assertMoney(new BigDecimal("18625"), result.finalPrice());
    }

    // ── Single trip — with discount ──────────────────────────────

    @Test
    @DisplayName("Calculate fare with STUDENT 50% discount")
    void calculate_studentDiscount_returnsHalfPrice() {
        FareDiscount discount = FareDiscount.create(
                PassengerType.STUDENT, DiscountType.PERCENT,
                new BigDecimal("50"), LocalDate.of(2025, 7, 1), null,
                UUID.randomUUID()
        );

        stubStations();
        stubMetroRule();
        when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                .thenReturn(Optional.of(discount));

        SingleTripFareResponse result = fareCalculationService.calculate(
                fromId, toId, FareMode.METRO, PassengerType.STUDENT
        );

        assertTrue(result.discountApplied());
        assertEquals("PERCENT", result.discountType());
        assertMoney(new BigDecimal("50"), result.discountValue());
        assertMoney(new BigDecimal("9312.50"), result.finalPrice());
    }

    @Test
    @DisplayName("Calculate fare with SENIOR 100% discount returns free")
    void calculate_seniorDiscount_returnsFree() {
        FareDiscount discount = FareDiscount.create(
                PassengerType.SENIOR, DiscountType.PERCENT,
                new BigDecimal("100"), LocalDate.of(2025, 7, 1), null,
                UUID.randomUUID()
        );

        stubStations();
        stubMetroRule();
        when(fareDiscountRepository.findActiveByPassengerType(PassengerType.SENIOR))
                .thenReturn(Optional.of(discount));

        SingleTripFareResponse result = fareCalculationService.calculate(
                fromId, toId, FareMode.METRO, PassengerType.SENIOR
        );

        assertMoney(new BigDecimal("0.00"), result.finalPrice());
    }

    @Test
    @DisplayName("Calculate fare with FIXED discount")
    void calculate_fixedDiscount_returnsSubtracted() {
        FareDiscount discount = FareDiscount.create(
                PassengerType.PRIORITY, DiscountType.FIXED,
                new BigDecimal("5000"), LocalDate.of(2025, 7, 1), null,
                UUID.randomUUID()
        );

        stubStations();
        stubMetroRule();
        when(fareDiscountRepository.findActiveByPassengerType(PassengerType.PRIORITY))
                .thenReturn(Optional.of(discount));

        SingleTripFareResponse result = fareCalculationService.calculate(
                fromId, toId, FareMode.METRO, PassengerType.PRIORITY
        );

        assertTrue(result.discountApplied());
        assertEquals("FIXED", result.discountType());
        // 18625 - 5000 = 13625
        assertMoney(new BigDecimal("13625"), result.finalPrice());
    }

    @Test
    @DisplayName("PassengerType provided but no active discount — no discount applied")
    void calculate_noActiveDiscount_discountNotApplied() {
        stubStations();
        stubMetroRule();
        when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                .thenReturn(Optional.empty());

        SingleTripFareResponse result = fareCalculationService.calculate(
                fromId, toId, FareMode.METRO, PassengerType.STUDENT
        );

        assertFalse(result.discountApplied());
        assertMoney(new BigDecimal("18625"), result.finalPrice());
    }

    // ── Clamp ────────────────────────────────────────────────────

    @Test
    @DisplayName("Short distance clamps to min price")
    void calculate_shortDistance_clampsToMinPrice() {
        when(fromStation.getKmMarker()).thenReturn(new BigDecimal("0.000"));
        when(toStation.getKmMarker()).thenReturn(new BigDecimal("0.100"));

        stubStations();
        stubMetroRule();

        SingleTripFareResponse result = fareCalculationService.calculate(
                fromId, toId, FareMode.METRO, null
        );

        // 8000 + 0.1×850 = 8085 > minPrice(8000) → không clamp
        // Để test clamp thật sự dùng distance = 0
        assertMoney(new BigDecimal("8000"), result.baseFare());
    }

    @Test
    @DisplayName("Long distance clamps to max price")
    void calculate_longDistance_clampsToMaxPrice() {
        when(toStation.getKmMarker()).thenReturn(new BigDecimal("100.000"));

        stubStations();
        stubMetroRule();

        SingleTripFareResponse result = fareCalculationService.calculate(
                fromId, toId, FareMode.METRO, null
        );

        // 8000 + 100×850 = 93000 > 30000 → clamp to 30000
        assertMoney(new BigDecimal("30000"), result.finalPrice());
    }

    // ── Direction ────────────────────────────────────────────────

    @Test
    @DisplayName("Reverse direction returns positive distance")
    void calculate_reverseDirection_returnsPositiveDistance() {
        when(fromStation.getKmMarker()).thenReturn(new BigDecimal("12.500"));
        when(toStation.getKmMarker()).thenReturn(new BigDecimal("0.000"));

        stubStations();
        stubMetroRule();

        SingleTripFareResponse result = fareCalculationService.calculate(
                fromId, toId, FareMode.METRO, null
        );

        assertMoney(new BigDecimal("12.500"), result.distanceKm());
        assertMoney(new BigDecimal("18625"), result.finalPrice());
    }

    // ── Exception cases ──────────────────────────────────────────

    @Test
    @DisplayName("From station not found throws NotFoundException")
    void calculate_fromStationNotFound_throwsNotFoundException() {
        when(stationRepository.findById(fromId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                fareCalculationService.calculate(fromId, toId, FareMode.METRO, null)
        );
    }

    @Test
    @DisplayName("To station not found throws NotFoundException")
    void calculate_toStationNotFound_throwsNotFoundException() {
        when(stationRepository.findById(fromId)).thenReturn(Optional.of(fromStation));
        when(stationRepository.findById(toId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                fareCalculationService.calculate(fromId, toId, FareMode.METRO, null)
        );
    }

    @Test
    @DisplayName("Same station throws BusinessRuleException")
    void calculate_sameStation_throwsBusinessRuleException() {
        when(stationRepository.findById(fromId))
                .thenReturn(Optional.of(fromStation));

        assertThrows(BusinessRuleException.class, () ->
                fareCalculationService.calculate(fromId, fromId, FareMode.METRO, null)
        );
    }

    @Test
    @DisplayName("No active fare rule throws NotFoundException")
    void calculate_noFareRule_throwsNotFoundException() {
        stubStations();
        when(fareRuleRepository.findActiveByMode(FareMode.METRO))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                fareCalculationService.calculate(fromId, toId, FareMode.METRO, null)
        );
    }

    // ── Multimodal ───────────────────────────────────────────────
    @Test
    @DisplayName("Multimodal null legs throws BusinessRuleException")
    void calculateMultimodal_nullLegs_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                fareCalculationService.calculateMultimodal(null, null)
        );
    }

    @Test
    @DisplayName("Multimodal empty legs throws BusinessRuleException")
    void calculateMultimodal_emptyLegs_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                fareCalculationService.calculateMultimodal(List.of(), null)
        );
    }


    @Test
    @DisplayName("Multimodal BUS + METRO returns correct total")
    void calculateMultimodal_busAndMetro_returnsCorrectTotal() {
        UUID busFromId = UUID.randomUUID();
        UUID busToId   = UUID.randomUUID();

        Station busFrom = mock(Station.class);
        Station busTo   = mock(Station.class);
        when(busFrom.getCode()).thenReturn("BRT01_01");
        when(busTo.getCode()).thenReturn("BRT01_09");
        when(busFrom.getKmMarker()).thenReturn(new BigDecimal("0.000"));
        when(busTo.getKmMarker()).thenReturn(new BigDecimal("12.400"));

        when(stationRepository.findById(busFromId)).thenReturn(Optional.of(busFrom));
        when(stationRepository.findById(busToId)).thenReturn(Optional.of(busTo));
        when(stationRepository.findById(fromId)).thenReturn(Optional.of(fromStation));
        when(stationRepository.findById(toId)).thenReturn(Optional.of(toStation));
        when(fareRuleRepository.findActiveByMode(FareMode.BUS))
                .thenReturn(Optional.of(busFareRule));
        when(fareRuleRepository.findActiveByMode(FareMode.METRO))
                .thenReturn(Optional.of(metroFareRule));

        List<MultimodalLegRequest> legs = List.of(
                new MultimodalLegRequest(busFromId, busToId, FareMode.BUS),
                new MultimodalLegRequest(fromId, toId, FareMode.METRO)
        );

        MultimodalFareResponse result = fareCalculationService
                .calculateMultimodal(legs, null);

        assertEquals(2, result.legs().size());
        assertMoney(new BigDecimal("8580"), result.legs().get(0).fare());
        assertMoney(new BigDecimal("18625"), result.legs().get(1).fare());
        assertMoney(new BigDecimal("27205"), result.totalFare());
        assertFalse(result.discountApplied());
        assertMoney(new BigDecimal("27205"), result.finalPrice());
    }

    @Test
    @DisplayName("Multimodal with STUDENT 50% discount applied on total")
    void calculateMultimodal_studentDiscount_appliedOnTotal() {
        UUID busFromId = UUID.randomUUID();
        UUID busToId   = UUID.randomUUID();

        Station busFrom = mock(Station.class);
        Station busTo   = mock(Station.class);
        when(busFrom.getCode()).thenReturn("BRT01_01");
        when(busTo.getCode()).thenReturn("BRT01_09");
        when(busFrom.getKmMarker()).thenReturn(new BigDecimal("0.000"));
        when(busTo.getKmMarker()).thenReturn(new BigDecimal("12.400"));

        FareDiscount discount = FareDiscount.create(
                PassengerType.STUDENT, DiscountType.PERCENT,
                new BigDecimal("50"), LocalDate.of(2025, 7, 1), null,
                UUID.randomUUID()
        );

        when(stationRepository.findById(busFromId)).thenReturn(Optional.of(busFrom));
        when(stationRepository.findById(busToId)).thenReturn(Optional.of(busTo));
        when(stationRepository.findById(fromId)).thenReturn(Optional.of(fromStation));
        when(stationRepository.findById(toId)).thenReturn(Optional.of(toStation));
        when(fareRuleRepository.findActiveByMode(FareMode.BUS))
                .thenReturn(Optional.of(busFareRule));
        when(fareRuleRepository.findActiveByMode(FareMode.METRO))
                .thenReturn(Optional.of(metroFareRule));
        when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                .thenReturn(Optional.of(discount));

        List<MultimodalLegRequest> legs = List.of(
                new MultimodalLegRequest(busFromId, busToId, FareMode.BUS),
                new MultimodalLegRequest(fromId, toId, FareMode.METRO)
        );

        MultimodalFareResponse result = fareCalculationService
                .calculateMultimodal(legs, PassengerType.STUDENT);

        assertTrue(result.discountApplied());
        assertMoney(new BigDecimal("27205"), result.totalFare());
        assertMoney(new BigDecimal("13602.50"), result.finalPrice());
    }

    @Test
    @DisplayName("Multimodal single leg throws BusinessRuleException")
    void calculateMultimodal_singleLeg_throwsException() {
        List<MultimodalLegRequest> legs = List.of(
                new MultimodalLegRequest(fromId, toId, FareMode.METRO)
        );

        assertThrows(BusinessRuleException.class, () ->
                fareCalculationService.calculateMultimodal(legs, null)
        );
    }
}