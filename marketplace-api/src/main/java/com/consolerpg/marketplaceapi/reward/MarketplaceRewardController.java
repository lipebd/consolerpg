package com.consolerpg.marketplaceapi.reward;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rewards")
public class MarketplaceRewardController {

    private final MarketplaceRewardService service;

    public MarketplaceRewardController(MarketplaceRewardService service) {
        this.service = service;
    }

    @GetMapping
    public List<RewardResponse> getPendingRewards(@RequestHeader("X-Campaign-Id") UUID campaignId) {
        return service.getPendingRewards(campaignId);
    }

    @PostMapping("/{rewardId}/claim")
    public RewardResponse claimReward(
            @PathVariable("rewardId") UUID rewardId,
            @RequestHeader("X-Campaign-Id") UUID campaignId) {

        return service.claimReward(rewardId, campaignId);
    }
}
