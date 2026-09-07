package rpg.world.shops;

// Describes the outcome of a shop sale attempt.
public enum ShopSaleResult {

	SUCCESS,
	INVALID_SELECTION,
	ITEM_NOT_SELLABLE,
	INVALID_QUANTITY,
	INSUFFICIENT_INVENTORY,
	TOTAL_OVERFLOW,
	GOLD_CAPACITY_EXCEEDED
}