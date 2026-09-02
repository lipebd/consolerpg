package rpg.items;

// Stores the immutable common data of an item.
public final class ItemDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";
	private final String id;
	private final ItemCategory category;
	private final boolean playerTradable;
	private final boolean npcSellable;
	private final String name;
	private final String description;

	public ItemDefinition(String id, ItemCategory category, boolean playerTradable, boolean npcSellable, String name,
			String description) {
		if (id == null || id.isBlank()) {
		    throw new IllegalArgumentException(
		            "Item ID cannot be empty."
		    );
		}

		String trimmedId = id.trim();

		if (!trimmedId.matches(ID_PATTERN)) {
		    throw new IllegalArgumentException(
		            "Item ID must use lowercase letters, numbers "
		                    + "and single underscores: " + trimmedId
		    );
		}

		if (category == null) {
			throw new IllegalArgumentException("Item category cannot be null.");
		}

		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Item name cannot be empty.");
		}

		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("Item description cannot be empty.");
		}

		this.id = trimmedId;
		this.category = category;
		this.playerTradable = playerTradable;
		this.npcSellable = npcSellable;
		this.name = name;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public ItemCategory getCategory() {
		return category;
	}

	public boolean isPlayerTradable() {
		return playerTradable;
	}

	public boolean isNpcSellable() {
		return npcSellable;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}