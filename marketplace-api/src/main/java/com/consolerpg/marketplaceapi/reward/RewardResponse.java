package com.consolerpg.marketplaceapi.reward;

import java.util.UUID;

public record RewardResponse(
        UUID id,
        UUID transactionId,
        int amount,
        RewardStatus status) {

    public static RewardResponse from(MarketplaceRewardEntity reward) {
        return new RewardResponse(
                reward.getId(),
                reward.getTransactionId(),
                reward.getAmount(),
                reward.getStatus());
    }
}
