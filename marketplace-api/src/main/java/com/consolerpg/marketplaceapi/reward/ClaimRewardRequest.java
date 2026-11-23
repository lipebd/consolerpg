package com.consolerpg.marketplaceapi.reward;

import jakarta.validation.constraints.NotBlank;

public record ClaimRewardRequest(
        @NotBlank String sellerId
) {
}
