package com.consolerpg.marketplaceapi.reward;

import java.util.UUID;

import com.consolerpg.marketplaceapi.transaction.MarketplaceTransactionEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "marketplace_rewards")
public class MarketplaceRewardEntity {

    @Id
    private UUID id;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID transactionId;

    @Column(nullable = false, updatable = false)
    private String sellerId;

    @Column(nullable = false, updatable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardStatus status;

    @Version
    private long version;

    protected MarketplaceRewardEntity() {
    }

    public MarketplaceRewardEntity(MarketplaceTransactionEntity transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Marketplace transaction cannot be null.");
        }

        this.id = UUID.randomUUID();
        this.transactionId = transaction.getId();
        this.sellerId = transaction.getSellerId();
        this.amount = transaction.getTotalPrice();
        this.status = RewardStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public int getAmount() {
        return amount;
    }

    public RewardStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }

    public void claim() {
        if (status != RewardStatus.PENDING) {
            throw new IllegalStateException("Only pending rewards can be claimed.");
        }

        status = RewardStatus.CLAIMED;
    }
}
