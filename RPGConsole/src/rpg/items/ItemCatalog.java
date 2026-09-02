package rpg.items;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to runtime game items.
public final class ItemCatalog {

	private final Map<String, Item> itemsById;

	public ItemCatalog() {
		itemsById = new LinkedHashMap<>();
	}

	public void register(Item item) {
		if (item == null) {
			throw new IllegalArgumentException("Item cannot be null.");
		}

		String itemId = item.getId();

		if (itemsById.containsKey(itemId)) {
			throw new IllegalArgumentException("Item ID is already registered: " + itemId);
		}

		itemsById.put(itemId, item);
	}

	public Item getById(String itemId) {
		if (itemId == null || itemId.isBlank()) {
			throw new IllegalArgumentException("Item ID cannot be empty.");
		}

		Item item = itemsById.get(itemId);

		if (item == null) {
			throw new IllegalArgumentException("Item not found: " + itemId);
		}

		return item;
	}

	public List<Item> getAll() {
		return List.copyOf(itemsById.values());
	}
}