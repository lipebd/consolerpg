package rpg.world.shops;

import rpg.items.Item;

// Defines one possible weighted sale offer for a shop.
public final class ShopStockEntry {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private final String id;
	private final String shopId;
	private final Item item;
	private final int shopSalePricePercent;
	private final int initialStock;
	private final int weight;
	private final int finalShopSalePrice;

	public ShopStockEntry(String id, String shopId, Item item, int shopSalePricePercent, int initialStock, int weight) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Shop stock entry ID cannot be empty.");
		}

		String trimmedId = id.trim();

		if (!trimmedId.matches(ID_PATTERN)) {
			throw new IllegalArgumentException(
					"Shop stock entry ID must use lowercase letters, numbers and single underscores: " + trimmedId
			);
		}

		if (shopId == null || shopId.isBlank()) {
			throw new IllegalArgumentException("Shop stock entry shop ID cannot be empty.");
		}

		if (item == null) {
			throw new IllegalArgumentException("Shop stock entry item cannot be null.");
		}

		if (shopSalePricePercent <= 0) {
			throw new IllegalArgumentException("Shop sale price percent must be greater than 0.");
		}

		if (initialStock <= 0) {
			throw new IllegalArgumentException("Shop stock quantity must be greater than 0.");
		}

		if (weight <= 0) {
			throw new IllegalArgumentException("Shop stock weight must be greater than 0.");
		}

		Integer baseShopSalePrice = item.getBaseShopSalePrice();

		if (baseShopSalePrice == null) {
			throw new IllegalArgumentException("Stocked item must define a base shop sale price: " + item.getId());
		}

		long scaledPrice = (long) baseShopSalePrice * shopSalePricePercent;

		long roundedPrice = (scaledPrice + 50L) / 100L;

		if (roundedPrice < 1) {
			throw new IllegalArgumentException("Calculated shop sale price must be at least 1: " + item.getId());
		}

		if (roundedPrice > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"Calculated shop sale price exceeds the supported maximum: " + item.getId()
			);
		}

		this.id = trimmedId;
		this.shopId = shopId.trim();
		this.item = item;
		this.shopSalePricePercent = shopSalePricePercent;
		this.initialStock = initialStock;
		this.weight = weight;
		this.finalShopSalePrice = (int) roundedPrice;
	}

	public String getId() {
		return id;
	}

	public String getShopId() {
		return shopId;
	}

	public Item getItem() {
		return item;
	}

	public int getShopSalePricePercent() {
		return shopSalePricePercent;
	}

	public int getInitialStock() {
		return initialStock;
	}

	public int getWeight() {
		return weight;
	}

	public int getFinalShopSalePrice() {
		return finalShopSalePrice;
	}
}