package rpg.marketplace;

public class MarketplaceTransaction {
	
	private final String id;
    private final String listingId;
    private final String buyerId;
    private final String sellerId;
    private final String itemId;
    private final int quantity;
    private final int unitPrice;
    private final int totalPrice;

    private TransactionStatus status;

    public MarketplaceTransaction(
            String id,
            String listingId,
            String buyerId,
            String sellerId,
            String itemId,
            int quantity,
            int unitPrice) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or blank.");
        }

        if (listingId == null || listingId.isBlank()) {
            throw new IllegalArgumentException("Listing ID cannot be null or blank.");
        }

        if (buyerId == null || buyerId.isBlank()) {
            throw new IllegalArgumentException("Buyer ID cannot be null or blank.");
        }

        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Seller ID cannot be null or blank.");
        }

        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("Buyer and seller cannot be the same player.");
        }

        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("Item ID cannot be null or blank.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Transaction quantity must be greater than zero.");
        }

        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero.");
        }

        try {
            this.totalPrice = Math.multiplyExact(unitPrice, quantity);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Transaction total price is too large.",
                    exception);
        }

        this.id = id;
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.status = TransactionStatus.COMPLETED;
    }

    public String getId() {
        return id;
    }

    public String getListingId() {
        return listingId;
    }

    public String getBuyerId() {
        return buyerId;
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

    public int getTotalPrice() {
        return totalPrice;
    }

    public TransactionStatus getStatus() {
        return status;
    }

}
