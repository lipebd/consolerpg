package rpg.items;

// Represents the common data shared by all game items.
public abstract class Item {

	private final String id;
	private final ItemCategory category;
	private final boolean playerTradable;
	private final boolean npcSellable;
	private final String name;
	private final String description;

	protected Item(ItemDefinition definition) {
		if (definition == null) {
			throw new IllegalArgumentException("Item definition cannot be null.");
		}

		this.id = definition.getId();
		this.category = definition.getCategory();
		this.playerTradable = definition.isPlayerTradable();
		this.npcSellable = definition.isNpcSellable();
		this.name = definition.getName();
		this.description = definition.getDescription();
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

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Item otherItem)) {
			return false;
		}

		return id.equals(otherItem.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}