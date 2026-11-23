package rpg.marketplace;

import java.util.Objects;

// Represents one listing whose authoritative state is stored by the marketplace API.
public record MarketplaceListing(
		String id,
		String sellerId,
		String itemId,
		int quantity,
		int unitPrice,
		int totalPrice,
		ListingStatus status) {

	public MarketplaceListing {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Listing ID cannot be empty.");
		}
		if (sellerId == null || sellerId.isBlank()) {
			throw new IllegalArgumentException("Seller ID cannot be empty.");
		}
		if (itemId == null || itemId.isBlank()) {
			throw new IllegalArgumentException("Item ID cannot be empty.");
		}
		if (quantity <= 0 || unitPrice <= 0) {
			throw new IllegalArgumentException("Listing quantity and price must be positive.");
		}
		int expectedTotal = Math.multiplyExact(quantity, unitPrice);
		if (totalPrice != expectedTotal) {
			throw new IllegalArgumentException("Listing total price is inconsistent.");
		}
		Objects.requireNonNull(status, "Listing status cannot be null.");
	}
}
