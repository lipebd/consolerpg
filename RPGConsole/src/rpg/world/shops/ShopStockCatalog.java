package rpg.world.shops;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores possible shop-stock entries by stable and shop IDs.
public final class ShopStockCatalog {

	private final Map<String, ShopStockEntry> entriesById;
	private final Map<String, List<ShopStockEntry>> entriesByShopId;

	public ShopStockCatalog() {
		entriesById = new LinkedHashMap<>();
		entriesByShopId = new LinkedHashMap<>();
	}

	public void register(ShopStockEntry stockEntry) {
		if (stockEntry == null) {
			throw new IllegalArgumentException("Shop stock entry cannot be null.");
		}

		String entryId = stockEntry.getId();

		if (entriesById.containsKey(entryId)) {
			throw new IllegalArgumentException("Shop stock entry ID is already registered: " + entryId);
		}

		List<ShopStockEntry> shopEntries = entriesByShopId
				.computeIfAbsent(stockEntry.getShopId(), ignored -> new ArrayList<>());

		boolean commerciallyDuplicated = shopEntries.stream().anyMatch(
				existingEntry -> existingEntry.getItem().equals(stockEntry.getItem())
						&& existingEntry.getShopSalePricePercent() == stockEntry.getShopSalePricePercent()
						&& existingEntry.getInitialStock() == stockEntry.getInitialStock()
		);

		if (commerciallyDuplicated) {
			throw new IllegalArgumentException(
					"Commercially duplicate shop offer: " + stockEntry.getShopId() + " -> "
							+ stockEntry.getItem().getId()
			);
		}

		entriesById.put(entryId, stockEntry);
		shopEntries.add(stockEntry);
	}

	public ShopStockEntry getById(String entryId) {
		ShopStockEntry stockEntry = entriesById.get(entryId);

		if (stockEntry == null) {
			throw new IllegalArgumentException("Shop stock entry not found: " + entryId);
		}

		return stockEntry;
	}

	public List<ShopStockEntry> getByShopId(String shopId) {
		List<ShopStockEntry> stockEntries = entriesByShopId.get(shopId);

		if (stockEntries == null) {
			return List.of();
		}

		return List.copyOf(stockEntries);
	}

	public List<ShopStockEntry> getAll() {
		return List.copyOf(entriesById.values());
	}
}