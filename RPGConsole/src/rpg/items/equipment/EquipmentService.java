package rpg.items.equipment;

import java.util.Objects;

import rpg.characters.CharacterEquipment;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.Inventory;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.items.ItemCategory;

// Coordinates equipment transfers between the inventory and characters.
public final class EquipmentService {

	private final EquipmentCatalog equipmentCatalog;
	private final ItemCatalog itemCatalog;

	public EquipmentService(EquipmentCatalog equipmentCatalog, ItemCatalog itemCatalog) {
		this.equipmentCatalog = Objects.requireNonNull(equipmentCatalog, "Equipment catalog cannot be null.");

		this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");
	}

	public boolean equip(Party party, PlayableCharacter character, Item item) {
		validatePartyMember(party, character);

		Objects.requireNonNull(item, "Item cannot be null.");

		EquipmentDefinition newDefinition = equipmentCatalog.getByItemId(item.getId());

		if (!character.canEquip(newDefinition)) {
			return false;
		}

		EquipmentDefinition currentDefinition = getCurrentDefinition(character.getEquipment(), newDefinition);

		if (currentDefinition != null && currentDefinition.getItemId().equals(newDefinition.getItemId())) {
			return false;
		}

		Inventory inventory = party.getInventory();

		if (!inventory.removeItem(item, 1)) {
			return false;
		}

		EquipmentDefinition replacedDefinition;

		try {
			replacedDefinition = equipDefinition(character, newDefinition);
		} catch (RuntimeException exception) {
			inventory.addItem(item, 1);

			throw exception;
		}

		if (replacedDefinition != null) {
			Item replacedItem = itemCatalog.getById(replacedDefinition.getItemId());

			if (!inventory.addItem(replacedItem, 1)) {
				throw new IllegalStateException(
						"Replaced equipment could not return to the inventory: " + replacedDefinition.getItemId()
				);
			}
		}

		return true;
	}

	public boolean unequip(Party party, PlayableCharacter character, ItemCategory equipmentCategory) {
		validatePartyMember(party, character);

		Objects.requireNonNull(equipmentCategory, "Equipment category cannot be null.");

		EquipmentDefinition equippedDefinition = getEquippedDefinition(character.getEquipment(), equipmentCategory);

		if (equippedDefinition == null) {
			return false;
		}

		Item item = itemCatalog.getById(equippedDefinition.getItemId());

		if (!party.getInventory().addItem(item, 1)) {
			throw new IllegalStateException(
					"Equipment could not return to the inventory: " + equippedDefinition.getItemId()
			);
		}

		unequipDefinition(character, equipmentCategory);

		return true;
	}

	private EquipmentDefinition equipDefinition(PlayableCharacter character, EquipmentDefinition definition) {
		if (definition instanceof WeaponDefinition weaponDefinition) {
			return character.equipWeapon(weaponDefinition).orElse(null);
		}

		if (definition instanceof ArmorDefinition armorDefinition) {
			return character.equipArmor(armorDefinition).orElse(null);
		}

		if (definition instanceof TrinketDefinition trinketDefinition) {
			return character.equipTrinket(trinketDefinition).orElse(null);
		}

		throw new IllegalArgumentException("Unsupported equipment definition: " + definition.getItemId());
	}

	private void unequipDefinition(PlayableCharacter character, ItemCategory equipmentCategory) {
		switch (equipmentCategory) {
		case WEAPON -> character.unequipWeapon();
		case ARMOR -> character.unequipArmor();
		case TRINKET -> character.unequipTrinket();

		default -> throw new IllegalArgumentException("Item category is not an equipment slot: " + equipmentCategory);
		}
	}

	private EquipmentDefinition getCurrentDefinition(CharacterEquipment equipment, EquipmentDefinition newDefinition) {
		if (newDefinition instanceof WeaponDefinition) {
			return equipment.getWeapon().orElse(null);
		}

		if (newDefinition instanceof ArmorDefinition) {
			return equipment.getArmor().orElse(null);
		}

		if (newDefinition instanceof TrinketDefinition) {
			return equipment.getTrinket().orElse(null);
		}

		return null;
	}

	private EquipmentDefinition getEquippedDefinition(CharacterEquipment equipment, ItemCategory equipmentCategory) {
		return switch (equipmentCategory) {
		case WEAPON -> equipment.getWeapon().orElse(null);
		case ARMOR -> equipment.getArmor().orElse(null);
		case TRINKET -> equipment.getTrinket().orElse(null);

		default -> throw new IllegalArgumentException("Item category is not an equipment slot: " + equipmentCategory);
		};
	}

	private void validatePartyMember(Party party, PlayableCharacter character) {
		Objects.requireNonNull(party, "Party cannot be null.");
		Objects.requireNonNull(character, "Playable character cannot be null.");

		if (!party.getMembers().contains(character)) {
			throw new IllegalArgumentException("Character does not belong to the party: " + character.getName());
		}
	}
}