package com.metro.afc.card.infrastructure.adapter.out;

import com.metro.afc.card.application.port.out.CardUidGeneratorPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Primary
public class RandomCardUidGenerator implements CardUidGeneratorPort {
    // Giả lập format MIFARE UID: prefix + 8 hex chars
    // Khi có hệ thống phát hành thật → swap implementation này
    @Override
    public String generate() {
        byte[] bytes = new byte[4];
        ThreadLocalRandom.current().nextBytes(bytes);
        return "VMS-" + HexFormat.of().formatHex(bytes).toUpperCase();
        // VD: VMS-A3F20C91
    }
}