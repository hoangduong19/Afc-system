package com.metro.afc.fare.domain.model;

import com.metro.afc.fare.domain.model.enums.fareRule.PassDurationType;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.ticket.domain.enums.PassScope;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FarePassPrice {

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_type", nullable = false, length = 10)
    private PassDurationType durationType;

    @Column(name = "duration_months")
    private Integer durationMonths; // null cho DAILY/WEEKLY; 1-12 cho MONTHLY

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", length = 20)
    private PassScope scope; // null cho METRO/ANY

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "amount", nullable = false, precision = 15, scale = 2))
    private Money price;

    public static FarePassPrice of(PassDurationType durationType,
                                   Integer durationMonths,
                                   PassScope scope,
                                   BigDecimal amount) {
        FarePassPrice p  = new FarePassPrice();
        p.durationType   = durationType;
        p.durationMonths = durationMonths;
        p.scope          = scope;
        p.price          = Money.of(amount);
        return p;
    }
}
