package rpg.persistence;

import rpg.items.Inventory;

// Stores one ordered inventory entry in a campaign snapshot.
public final class InventoryEntrySnapshot {

	private final String itemId;
	private final int quantity;

	public InventoryEntrySnapshot(String itemId, int quantity) {
		if (itemId == null || itemId.isBlank()) {
			throw new IllegalArgumentException("Inventory item ID cannot be null or blank.");
		}

		if (quantity <= 0 || quantity > Inventory.MAXIMUM_QUANTITY_PER_ITEM) {
			throw new IllegalArgumentException(
					"Inventory quantity must be between 1 and " + Inventory.MAXIMUM_QUANTITY_PER_ITEM + "."
			);
		}

		this.itemId = itemId;
		this.quantity = quantity;
	}

	public String getItemId() {
		return itemId;
	}

	public int getQuantity() {
		return quantity;
	}
}