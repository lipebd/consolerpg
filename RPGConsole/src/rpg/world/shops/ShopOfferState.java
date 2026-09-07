package rpg.world.shops;

import java.util.Objects;

// Tracks the mutable remaining stock of one active shop offer.
public final class ShopOfferState {

	private final ShopStockEntry stockEntry;
	private int remainingStock;

	public ShopOfferState(ShopStockEntry stockEntry) {
		this.stockEntry = Objects.requireNonNull(stockEntry, "Shop stock entry cannot be null.");

		remainingStock = stockEntry.getInitialStock();
	}

	public void decreaseStock(int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("Shop stock decrease quantity must be greater than 0.");
		}

		if (quantity > remainingStock) {
			throw new IllegalArgumentException("Shop stock decrease quantity cannot exceed remaining stock.");
		}

		remainingStock -= quantity;
	}

	public boolean hasRemainingStock() {
		return remainingStock > 0;
	}

	public ShopStockEntry getStockEntry() {
		return stockEntry;
	}

	public int getRemainingStock() {
		return remainingStock;
	}
}