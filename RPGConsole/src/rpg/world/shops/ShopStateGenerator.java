package rpg.world.shops;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

// Generates each shop's active offers using weighted selection without replacement.
public final class ShopStateGenerator {

	private final ShopCatalog shopCatalog;
	private final ShopStockCatalog stockCatalog;
	private final Random random;

	public ShopStateGenerator(ShopCatalog shopCatalog, ShopStockCatalog stockCatalog, Random random) {
		this.shopCatalog = Objects.requireNonNull(shopCatalog, "Shop catalog cannot be null.");

		this.stockCatalog = Objects.requireNonNull(stockCatalog, "Shop stock catalog cannot be null.");

		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");
	}

	public ShopStateCatalog generate() {
		ShopStateCatalog stateCatalog = new ShopStateCatalog();

		for (ShopDefinition shopDefinition : shopCatalog.getAll()) {
			List<ShopStockEntry> possibleEntries = new ArrayList<>(stockCatalog.getByShopId(shopDefinition.getId()));

			int activeOfferCount = Math.min(shopDefinition.getOfferSlots(), possibleEntries.size());

			List<ShopOfferState> activeOffers;

			if (activeOfferCount == possibleEntries.size()) {
				activeOffers = createOfferStates(possibleEntries);
			} else {
				activeOffers = selectWeightedOffers(possibleEntries, activeOfferCount);
			}

			stateCatalog.register(new ShopState(shopDefinition, activeOffers));
		}

		return stateCatalog;
	}

	private List<ShopOfferState> createOfferStates(List<ShopStockEntry> entries) {
		List<ShopOfferState> offerStates = new ArrayList<>();

		for (ShopStockEntry entry : entries) {
			offerStates.add(new ShopOfferState(entry));
		}

		return offerStates;
	}

	private List<ShopOfferState> selectWeightedOffers(List<ShopStockEntry> possibleEntries, int offerCount) {
		List<ShopOfferState> selectedOffers = new ArrayList<>();

		for (int index = 0; index < offerCount; index++) {
			int selectedIndex = selectWeightedIndex(possibleEntries);

			ShopStockEntry selectedEntry = possibleEntries.remove(selectedIndex);

			selectedOffers.add(new ShopOfferState(selectedEntry));
		}

		return selectedOffers;
	}

	private int selectWeightedIndex(List<ShopStockEntry> entries) {
		long totalWeight = 0;

		try {
			for (ShopStockEntry entry : entries) {
				totalWeight = Math.addExact(totalWeight, entry.getWeight());
			}
		} catch (ArithmeticException exception) {
			throw new IllegalStateException("Total shop offer weight exceeds the supported maximum.", exception);
		}

		long roll = random.nextLong(totalWeight);
		long accumulatedWeight = 0;

		for (int index = 0; index < entries.size(); index++) {
			accumulatedWeight = Math.addExact(accumulatedWeight, entries.get(index).getWeight());

			if (roll < accumulatedWeight) {
				return index;
			}
		}

		throw new IllegalStateException("Shop offer selection failed.");
	}
}