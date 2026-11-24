package com.consolerpg.marketplaceapi.listing;

import java.util.UUID;

public record ListingResponse(

        UUID id,
        String sellerName,
        String itemId,
        int quantity,
        int unitPrice,
        int totalPrice,
        ListingStatus status,
        boolean ownedByCurrentCampaign

) {

    public static ListingResponse from(
            MarketplaceListingEntity listing, UUID campaignId) {

        return new ListingResponse(
                listing.getId(),
                listing.getSellerName(),
                listing.getItemId(),
                listing.getQuantity(),
                listing.getUnitPrice(),
                listing.getTotalPrice(),
                listing.getStatus(),
                listing.getSellerCampaignId().equals(campaignId)
        );
    }
}
