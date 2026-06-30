package com.metro.afc.passenger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metro.afc.fare.application.port.out.FareDiscountRepository;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.passenger.dto.DiscountResponse;
import com.metro.afc.passenger.dto.FarePriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FareCatalogService {

    private static final String PRICES_KEY = "fare:prices:active";
    private static final String DISCOUNTS_KEY = "fare:discounts:active";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final FareRuleRepository fareRuleRepository;
    private final FareDiscountRepository fareDiscountRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper redisObjectMapper;

    @SneakyThrows
    public List<FarePriceResponse> getFarePrices() {
        String cached = redisTemplate.opsForValue().get(PRICES_KEY);
        if (cached != null) {
            return redisObjectMapper.readValue(cached, new TypeReference<List<FarePriceResponse>>() {});
        }

        List<FarePriceResponse> fresh = new ArrayList<>(
                fareRuleRepository.findAllActive().stream()
                        .map(this::toResponse)
                        .toList()
        );
        redisTemplate.opsForValue().set(PRICES_KEY, redisObjectMapper.writeValueAsString(fresh), TTL);
        return fresh;
    }

    @SneakyThrows
    public List<DiscountResponse> getActiveDiscounts() {
        String cached = redisTemplate.opsForValue().get(DISCOUNTS_KEY);
        if (cached != null) {
            return redisObjectMapper.readValue(cached, new TypeReference<List<DiscountResponse>>() {});
        }

        List<DiscountResponse> fresh = new ArrayList<>(
                fareDiscountRepository.findAllActive().stream()
                        .map(d -> new DiscountResponse(
                                d.getPassengerType(),
                                d.getDiscountValue().getDiscountType().name(),
                                d.getDiscountValue().getValue(),
                                d.getEffectiveFrom(),
                                d.getEffectiveTo()
                        ))
                        .toList()
        );
        redisTemplate.opsForValue().set(DISCOUNTS_KEY, redisObjectMapper.writeValueAsString(fresh), TTL);
        return fresh;
    }

    public void evictPricesCache() {
        redisTemplate.delete(PRICES_KEY);
    }

    public void evictDiscountsCache() {
        redisTemplate.delete(DISCOUNTS_KEY);
    }

    private FarePriceResponse toResponse(FareRule rule) {
        FarePriceResponse.SingleTripPrice singleTrip =
                new FarePriceResponse.SingleTripPrice(
                        rule.getBaseFare().getAmount(),
                        rule.getRatePerKm().getAmount(),
                        rule.getMinPrice().getAmount(),
                        rule.getMaxPrice().getAmount()
                );

        List<FarePriceResponse.PassPriceItem> passPrices = rule.getPassPrices().stream()
                .map(p -> new FarePriceResponse.PassPriceItem(
                        p.getDurationType().name(),
                        p.getDurationMonths(),
                        p.getScope() != null ? p.getScope().name() : null,
                        p.getPrice().getAmount()
                ))
                .toList();

        return new FarePriceResponse(rule.getMode(), singleTrip, passPrices);
    }
}