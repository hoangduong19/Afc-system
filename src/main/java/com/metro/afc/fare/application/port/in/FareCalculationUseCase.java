package com.metro.afc.fare.application.port.in;

import com.metro.afc.fare.application.dto.fareRule.calculation.MultimodalFareResponse;
import com.metro.afc.fare.application.dto.fareRule.calculation.MultimodalLegRequest;
import com.metro.afc.fare.application.dto.fareRule.calculation.SingleTripFareResponse;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;

import java.util.List;
import java.util.UUID;

public interface FareCalculationUseCase {
    SingleTripFareResponse calculate(UUID fromStationId, UUID toStationId,
                                     FareMode mode, PassengerType passengerType);
    MultimodalFareResponse calculateMultimodal(List<MultimodalLegRequest> legs,
                                               PassengerType passengerType);
}