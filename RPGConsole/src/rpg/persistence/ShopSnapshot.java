package rpg.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// Stores one shop and its ordered active offers in a campaign snapshot.
public final class ShopSnapshot {

	private final String shopId;
	private final List<ShopOfferSnapshot> activeOffers;

	public ShopSnapshot(String shopId, List<ShopOfferSnapshot> activeOffers) {
		if (shopId == null || shopId.isBlank()) {
			throw new IllegalArgumentException("Shop ID cannot be null or blank.");
		}

		Objects.requireNonNull(activeOffers, "Active shop offers cannot be null.");

		if (activeOffers.isEmpty()) {
			throw new IllegalArgumentException("Shop snapshot must contain at least one active offer.");
		}

		Set<String> uniqueStockEntryIds = new HashSet<>();

		for (ShopOfferSnapshot offerSnapshot : activeOffers) {
			Objects.requireNonNull(offerSnapshot, "Active shop offer cannot be null.");

			String stockEntryId = offerSnapshot.getStockEntryId();

			if (!uniqueStockEntryIds.add(stockEntryId)) {
				throw new IllegalArgumentException("Shop stock-entry ID is duplicated: " + stockEntryId);
			}
		}

		this.shopId = shopId;
		this.activeOffers = List.copyOf(activeOffers);
	}

	public String getShopId() {
		return shopId;
	}

	public List<ShopOfferSnapshot> getActiveOffers() {
		return activeOffers;
	}
}