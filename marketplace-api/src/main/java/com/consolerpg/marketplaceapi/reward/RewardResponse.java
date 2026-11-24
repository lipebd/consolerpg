package com.consolerpg.marketplaceapi.reward;

import java.util.UUID;

import com.consolerpg.marketplaceapi.transaction.MarketplaceTransactionEntity;

public record RewardResponse(
        UUID id,
        UUID transactionId,
        int amount,
        RewardStatus status,
        String itemId,
        int quantity) {

    public static RewardResponse from(MarketplaceRewardEntity reward) {
        MarketplaceTransactionEntity transaction = reward.getTransaction();

        return new RewardResponse(
                reward.getId(),
                reward.getTransactionId(),
                reward.getAmount(),
                reward.getStatus(),
                transaction.getItemId(),
                transaction.getQuantity());
    }
}