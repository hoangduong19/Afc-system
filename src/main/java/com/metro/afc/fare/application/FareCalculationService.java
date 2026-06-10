package com.metro.afc.fare.application;

import com.metro.afc.fare.application.dto.fareRule.calculation.MultimodalFareResponse;
import com.metro.afc.fare.application.dto.fareRule.calculation.MultimodalLegRequest;
import com.metro.afc.fare.application.dto.fareRule.calculation.MultimodalLegResponse;
import com.metro.afc.fare.application.dto.fareRule.calculation.SingleTripFareResponse;
import com.metro.afc.fare.application.port.in.FareCalculationUseCase;
import com.metro.afc.fare.application.port.out.FareDiscountRepository;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareDiscount;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FareCalculationService implements FareCalculationUseCase {

    private final StationRepository stationRepository;
    private final FareRuleRepository fareRuleRepository;
    private final FareDiscountRepository fareDiscountRepository;

    @Override
    public SingleTripFareResponse calculate(UUID fromStationId, UUID toStationId,
                                            FareMode mode, PassengerType passengerType) {
        // 1. Lấy stations
        Station from = stationRepository.findById(fromStationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STATION_NOT_FOUND));
        Station to = stationRepository.findById(toStationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STATION_NOT_FOUND));

        // 2. Validate khác ga
        if (fromStationId.equals(toStationId))
            throw new BusinessRuleException(
                    ErrorCode.STATION_SAME,
                    "From and to station must be different"
            );

        // 3. Tính distance
        BigDecimal distanceKm = to.getKmMarker()
                .subtract(from.getKmMarker())
                .abs();

        // 4. Lấy fare rule active theo mode
        FareRule fareRule = fareRuleRepository.findActiveByMode(mode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FARE_RULE_NOT_FOUND));

        // 5. Tính fare (domain logic)
        Money calculatedFare = fareRule.calculateFare(distanceKm);

        // 6. Áp dụng discount nếu có
        boolean discountApplied  = false;
        String discountType      = null;
        BigDecimal discountValue = null;
        Money finalPrice         = calculatedFare;

        if (passengerType != null) {
            Optional<FareDiscount> discount = fareDiscountRepository
                    .findActiveByPassengerType(passengerType);

            if (discount.isPresent()) {
                finalPrice      = discount.get().applyTo(calculatedFare);
                discountApplied = true;
                discountType    = discount.get().getDiscountValue()
                        .getDiscountType().name();
                discountValue   = discount.get().getDiscountValue().getValue();
            }
        }

        // 7. Build response
        return new SingleTripFareResponse(
                from.getCode(),
                to.getCode(),
                distanceKm,
                fareRule.getBaseFare().getAmount(),
                calculatedFare.getAmount(),
                discountApplied,
                discountType,
                discountValue,
                finalPrice.getAmount()
        );
    }

    @Override
    public MultimodalFareResponse calculateMultimodal(List<MultimodalLegRequest> legs,
                                                      PassengerType passengerType) {
        if (legs == null || legs.size() < 2)
            throw new BusinessRuleException(
                    ErrorCode.VALIDATION_ERROR,
                    "At least 2 legs required for multimodal"
            );

        List<MultimodalLegResponse> legResponses = new ArrayList<>();
        Money totalFare = Money.of(BigDecimal.ZERO);

        // 1. Tính từng leg
        for (MultimodalLegRequest leg : legs) {
            Station from = stationRepository.findById(leg.fromStationId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.STATION_NOT_FOUND));
            Station to = stationRepository.findById(leg.toStationId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.STATION_NOT_FOUND));

            BigDecimal distanceKm = to.getKmMarker()
                    .subtract(from.getKmMarker()).abs();

            FareRule fareRule = fareRuleRepository.findActiveByMode(leg.mode())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.FARE_RULE_NOT_FOUND));

            Money legFare = fareRule.calculateFare(distanceKm);
            totalFare     = totalFare.add(legFare);

            legResponses.add(new MultimodalLegResponse(
                    from.getCode(), to.getCode(),
                    leg.mode().name(),
                    distanceKm,
                    legFare.getAmount()
            ));
        }

        // 2. Áp discount 1 lần trên tổng
        boolean discountApplied  = false;
        String discountType      = null;
        BigDecimal discountValue = null;
        Money finalPrice         = totalFare;

        if (passengerType != null) {
            Optional<FareDiscount> discount = fareDiscountRepository
                    .findActiveByPassengerType(passengerType);

            if (discount.isPresent()) {
                finalPrice      = discount.get().applyTo(totalFare);
                discountApplied = true;
                discountType    = discount.get().getDiscountValue().getDiscountType().name();
                discountValue   = discount.get().getDiscountValue().getValue();
            }
        }

        return new MultimodalFareResponse(
                legResponses,
                totalFare.getAmount(),
                discountApplied,
                discountType,
                discountValue,
                finalPrice.getAmount()
        );
    }

    public BigDecimal calculateRaw(Station from, Station to,
                                   FareRule fareRule,
                                   BigDecimal distanceKm) {
        BigDecimal distance = distanceKm != null
                ? distanceKm
                : to.getKmMarker().subtract(from.getKmMarker()).abs();
        return fareRule.calculateFare(distance).getAmount();
    }
}
