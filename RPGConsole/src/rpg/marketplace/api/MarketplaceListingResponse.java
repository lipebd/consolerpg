package rpg.marketplace.api;

import rpg.marketplace.MarketplaceListing;

public record MarketplaceListingResponse(
		String id,
		String sellerId,
		String itemId,
		int quantity,
		int unitPrice,
		int totalPrice,
		String status)
{
	public MarketplaceListing toMarketplaceListing() {
        return new MarketplaceListing(
                id,
                sellerId,
                itemId,
                quantity,
                unitPrice
        );
    }

}
