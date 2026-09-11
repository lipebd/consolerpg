package rpg.items.equipment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to equipment definitions.
public final class EquipmentCatalog {

	private final Map<String, EquipmentDefinition> definitionsByItemId;

	public EquipmentCatalog() {
		definitionsByItemId = new LinkedHashMap<>();
	}

	public void register(EquipmentDefinition definition) {
		if (definition == null) {
			throw new IllegalArgumentException("Equipment definition cannot be null.");
		}

		String itemId = definition.getItemId();

		if (definitionsByItemId.containsKey(itemId)) {
			throw new IllegalArgumentException("Equipment definition is already registered for item: " + itemId);
		}

		definitionsByItemId.put(itemId, definition);
	}

	public EquipmentDefinition getByItemId(String itemId) {
		validateItemId(itemId);

		EquipmentDefinition definition = definitionsByItemId.get(itemId);

		if (definition == null) {
			throw new IllegalArgumentException("Equipment definition not found for item: " + itemId);
		}

		return definition;
	}

	public WeaponDefinition getWeaponByItemId(String itemId) {
		EquipmentDefinition definition = getByItemId(itemId);

		if (!(definition instanceof WeaponDefinition weaponDefinition)) {
			throw new IllegalArgumentException("Weapon definition not found for item: " + itemId);
		}

		return weaponDefinition;
	}

	public ArmorDefinition getArmorByItemId(String itemId) {
		EquipmentDefinition definition = getByItemId(itemId);

		if (!(definition instanceof ArmorDefinition armorDefinition)) {
			throw new IllegalArgumentException("Armor definition not found for item: " + itemId);
		}

		return armorDefinition;
	}

	public TrinketDefinition getTrinketByItemId(String itemId) {
		EquipmentDefinition definition = getByItemId(itemId);

		if (!(definition instanceof TrinketDefinition trinketDefinition)) {
			throw new IllegalArgumentException("Trinket definition not found for item: " + itemId);
		}

		return trinketDefinition;
	}

	public List<EquipmentDefinition> getAll() {
		return List.copyOf(definitionsByItemId.values());
	}

	public List<WeaponDefinition> getAllWeapons() {
		return definitionsByItemId.values().stream().filter(WeaponDefinition.class::isInstance)
				.map(WeaponDefinition.class::cast).toList();
	}

	public List<ArmorDefinition> getAllArmors() {
		return definitionsByItemId.values().stream().filter(ArmorDefinition.class::isInstance)
				.map(ArmorDefinition.class::cast).toList();
	}

	public List<TrinketDefinition> getAllTrinkets() {
		return definitionsByItemId.values().stream().filter(TrinketDefinition.class::isInstance)
				.map(TrinketDefinition.class::cast).toList();
	}

	private void validateItemId(String itemId) {
		if (itemId == null || itemId.isBlank()) {
			throw new IllegalArgumentException("Equipment item ID cannot be empty.");
		}
	}
}