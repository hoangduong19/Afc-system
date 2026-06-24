package com.metro.afc.trip.domain;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TripSpecification {

    public static Specification<Trip> withFilters(
            UUID cardId, UUID operatorId,
            Instant from, Instant to) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (cardId != null)
                predicates.add(cb.equal(root.get("cardId"), cardId));
            if (operatorId != null)
                predicates.add(cb.equal(root.get("operatorId"), operatorId));
            if (from != null)
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("tapInAt"), from));
            if (to != null)
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("tapInAt"), to));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}