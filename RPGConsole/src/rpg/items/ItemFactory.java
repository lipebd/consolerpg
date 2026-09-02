package rpg.items;

// Creates runtime items from their loaded definitions.
public final class ItemFactory {

	public Item create(ItemDefinition itemDefinition, RecoveryItemDefinitionCatalog recoveryItemDefinitionCatalog) {
		if (itemDefinition == null) {
			throw new IllegalArgumentException("Item definition cannot be null.");
		}

		if (recoveryItemDefinitionCatalog == null) {
			throw new IllegalArgumentException("Recovery item definition catalog cannot be null.");
		}

		String itemId = itemDefinition.getId();

		if (recoveryItemDefinitionCatalog.containsItemId(itemId)) {
			RecoveryItemDefinition recoveryDefinition = recoveryItemDefinitionCatalog.getByItemId(itemId);

			return new RecoveryItem(itemDefinition, recoveryDefinition);
		}

		return new BasicItem(itemDefinition);
	}
}