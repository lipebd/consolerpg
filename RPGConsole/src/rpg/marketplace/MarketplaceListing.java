package rpg.marketplace;

public class MarketplaceListing {
	
	private final String id;
	private final String sellerId;
	private final String itemId;
	private final int quantity;
	private final int unitPrice;
	

	private ListingStatus status;

	public MarketplaceListing(
			String id,
			String sellerId,
			String itemId,
			int quantity,
			int unitPrice) {

		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Listing ID cannot be null or blank.");
		}

		if (sellerId == null || sellerId.isBlank()) {
			throw new IllegalArgumentException("Seller ID cannot be null or blank.");
		}

		if (itemId == null || itemId.isBlank()) {
			throw new IllegalArgumentException("Item ID cannot be null or blank.");
		}

		if (quantity <= 0) {
			throw new IllegalArgumentException("Listing quantity must be greater than zero.");
		}

		if (unitPrice <= 0) {
			throw new IllegalArgumentException("Listing unit price must be greater than zero.");
		}

		this.id = id;
		this.sellerId = sellerId;
		this.itemId = itemId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.status = ListingStatus.ACTIVE;
	}

	public String getId() {
		return id;
	}

	public String getSellerId() {
		return sellerId;
	}

	public String getItemId() {
		return itemId;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getUnitPrice() {
		return unitPrice;
	}

	public ListingStatus getStatus() {
		return status;
	}

	public void markAsSold() {
		if (status != ListingStatus.ACTIVE) {
			throw new IllegalStateException(
					"Only active listings can be marked as sold.");
		}

		status = ListingStatus.SOLD;
	}

	public void cancel() {
		if (status != ListingStatus.ACTIVE) {
			throw new IllegalStateException(
					"Only active listings can be cancelled.");
		}

		status = ListingStatus.CANCELLED;
	}
}
