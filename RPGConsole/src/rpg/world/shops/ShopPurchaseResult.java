package rpg.world.shops;

// Describes the outcome of a shop purchase attempt.
public enum ShopPurchaseResult {

	SUCCESS,
	INVALID_SELECTION,
	INVALID_QUANTITY,
	INSUFFICIENT_STOCK,
	INSUFFICIENT_GOLD,
	INVENTORY_CAPACITY_EXCEEDED,
	TOTAL_OVERFLOW
}