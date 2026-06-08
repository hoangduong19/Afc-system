package com.metro.afc.blacklist.domain;

import com.metro.afc.blacklist.domain.events.BlacklistAddedEvent;
import com.metro.afc.blacklist.domain.events.BlacklistRemovedEvent;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blacklist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blacklist extends AbstractAggregateRoot<Blacklist> {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "card_id", nullable = false, columnDefinition = "uuid")
    private UUID cardId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "added_by", nullable = false, columnDefinition = "uuid")
    private UUID addedBy;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    @Column(name = "removed_by", columnDefinition = "uuid")
    private UUID removedBy;

    @Column(name = "removed_at")
    private Instant removedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public static Blacklist add(UUID cardId, String reason, UUID addedBy) {
        Blacklist b  = new Blacklist();
        b.id         = UUID.randomUUID();
        b.cardId     = cardId;
        b.reason     = reason;
        b.addedBy    = addedBy;
        b.addedAt    = Instant.now();
        b.isActive   = true;
        b.registerEvent(new BlacklistAddedEvent(b.id, cardId, reason, addedBy));
        return b;
    }

    public void remove(UUID removedBy) {
        if (!this.isActive) {
            throw new BusinessRuleException(
                    ErrorCode.BLACKLIST_ALREADY_REMOVED,
                    "Card is not in active blacklist"
            );
        }
        this.isActive   = false;
        this.removedBy  = removedBy;
        this.removedAt  = Instant.now();
        this.registerEvent(new BlacklistRemovedEvent(this.id, this.cardId, removedBy));
    }
}