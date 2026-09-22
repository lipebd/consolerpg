package rpg.world.shops;

import java.util.List;
import java.util.Objects;

// Stores the active offers and mutable runtime state of one shop.
public final class ShopState {

	private final ShopDefinition definition;
	private final List<ShopOfferState> activeOffers;

	public ShopState(ShopDefinition definition, List<ShopOfferState> activeOffers) {
		this.definition = Objects.requireNonNull(definition, "Shop definition cannot be null.");

		Objects.requireNonNull(activeOffers, "Active shop offers cannot be null.");

		if (activeOffers.size() > definition.getOfferSlots()) {
			throw new IllegalArgumentException("Active shop offers cannot exceed the configured offer slots.");
		}

		for (ShopOfferState offerState : activeOffers) {
			if (offerState == null) {
				throw new IllegalArgumentException("Active shop offer cannot be null.");
			}

			String offerShopId = offerState.getStockEntry().getShopId();

			if (!definition.getId().equals(offerShopId)) {
				throw new IllegalArgumentException(
						"Active shop offer belongs to another shop: " + offerState.getStockEntry().getId()
				);
			}
		}

		this.activeOffers = List.copyOf(activeOffers);
	}

	public ShopDefinition getDefinition() {
		return definition;
	}

	public List<ShopOfferState> getActiveOffers() {
		return activeOffers;
	}

	public List<ShopOfferState> getAvailableOffers() {
		return activeOffers.stream().filter(ShopOfferState::hasRemainingStock).toList();
	}
}