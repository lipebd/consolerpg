package com.consolerpg.marketplaceapi.reward;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rewards")
public class MarketplaceRewardController {

    private final MarketplaceRewardService service;

    public MarketplaceRewardController(MarketplaceRewardService service) {
        this.service = service;
    }

    @GetMapping
    public List<RewardResponse> getPendingRewards(@RequestParam("sellerId") String sellerId) {
        return service.getPendingRewards(sellerId);
    }

    @PostMapping("/{rewardId}/claim")
    public RewardResponse claimReward(
            @PathVariable("rewardId") UUID rewardId,
            @Valid @RequestBody ClaimRewardRequest request) {

        return service.claimReward(rewardId, request);
    }
}
