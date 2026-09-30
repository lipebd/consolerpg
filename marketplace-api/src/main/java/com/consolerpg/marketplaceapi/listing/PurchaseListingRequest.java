package com.consolerpg.marketplaceapi.listing;

import jakarta.validation.constraints.NotBlank;

public record PurchaseListingRequest(
        @NotBlank String buyerId
) {
}
