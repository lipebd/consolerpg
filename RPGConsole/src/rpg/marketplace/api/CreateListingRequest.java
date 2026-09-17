package rpg.marketplace.api;

public record CreateListingRequest(
		String sellerId,
        String itemId,
        int quantity,
        int unitPrice
) {
}
