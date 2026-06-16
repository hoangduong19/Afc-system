package com.metro.afc.card;

import com.metro.afc.card.infrastructure.adapter.out.RandomCardUidGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RandomCardUidGeneratorTest {

    private final RandomCardUidGenerator generator = new RandomCardUidGenerator();

    @Test
    void generate_hasCorrectFormat() {
        String uid = generator.generate();
        assertThat(uid).matches("VMS-[0-9A-F]{8}");
    }

    @Test
    void generate_isUnique() {
        Set<String> uids = new HashSet<>();
        for (int i = 0; i < 1000; i++) uids.add(generator.generate());
        assertThat(uids).hasSize(1000);
    }
}