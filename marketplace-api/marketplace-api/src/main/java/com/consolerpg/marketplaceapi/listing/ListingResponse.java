package com.consolerpg.marketplaceapi.listing;

import java.util.UUID;

public record ListingResponse(

        UUID id,
        String sellerId,
        String itemId,
        int quantity,
        int unitPrice,
        int totalPrice,
        ListingStatus status

) {

    public static ListingResponse from(
            MarketplaceListingEntity listing) {

        return new ListingResponse(
                listing.getId(),
                listing.getSellerId(),
                listing.getItemId(),
                listing.getQuantity(),
                listing.getUnitPrice(),
                listing.getTotalPrice(),
                listing.getStatus()
        );
    }
}
