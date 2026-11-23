package com.consolerpg.marketplaceapi.reward;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MarketplaceRewardService {

    private final MarketplaceRewardJpaRepository repository;

    public MarketplaceRewardService(MarketplaceRewardJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RewardResponse> getPendingRewards(String sellerId) {
        if (sellerId == null || sellerId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seller ID cannot be empty.");
        }

        return repository.findAllBySellerIdAndStatus(sellerId, RewardStatus.PENDING)
                .stream()
                .map(RewardResponse::from)
                .toList();
    }

    @Transactional
    public RewardResponse claimReward(UUID rewardId, ClaimRewardRequest request) {
        MarketplaceRewardEntity reward = repository.findByIdForUpdate(rewardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reward was not found."));

        if (!reward.getSellerId().equals(request.sellerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the seller can claim this reward.");
        }
        if (reward.getStatus() != RewardStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reward is not pending.");
        }

        reward.claim();

        return RewardResponse.from(reward);
    }
}
