package com.consolerpg.marketplaceapi.listing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateListingRequest(

        @NotBlank
        String sellerId,

        @NotBlank
        String itemId,

        @Positive
        int quantity,

        @Positive
        int unitPrice

) {
}