package com.metro.afc.fare.infrastructure.adapter.out.farerule;

import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRule.FareStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FareRuleJpaRepository extends JpaRepository<FareRule, UUID> {

    Optional<FareRule> findByCodeAndStatus(String code, FareStatus status);

    boolean existsByCodeAndStatus(String code, FareStatus status);

    List<FareRule> findAllByStatus(FareStatus status);

    Optional<FareRule> findByModeAndStatus(FareMode mode, FareStatus status);
}