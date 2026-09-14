package rpg.world.crafting;

// Stores one immutable item requirement of a crafting recipe.
public final class CraftingMaterial {

	private final String itemId;
	private final int requiredQuantity;

	public CraftingMaterial(String itemId, int requiredQuantity) {
		if (itemId == null || itemId.isBlank()) {
			throw new IllegalArgumentException("Crafting material item ID cannot be empty.");
		}

		if (requiredQuantity <= 0) {
			throw new IllegalArgumentException("Crafting material quantity must be greater than 0.");
		}

		this.itemId = itemId.trim();
		this.requiredQuantity = requiredQuantity;
	}

	public String getItemId() {
		return itemId;
	}

	public int getRequiredQuantity() {
		return requiredQuantity;
	}
}