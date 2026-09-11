package rpg.items.equipment;

import java.util.Objects;

import rpg.items.ItemCategory;

// Stores the immutable specialized data of an armor.
public final class ArmorDefinition extends EquipmentDefinition {

	private final ArmorWeight armorWeight;

	public ArmorDefinition(
			String itemId,
			ArmorWeight armorWeight,
			int levelRequirement,
			int hpBonus,
			int mpBonus,
			int spBonus,
			int attackBonus,
			int defenseBonus,
			int magicBonus,
			int resistanceBonus,
			int speedBonus,
			int luckBonus
	) {
		super(
				itemId,
				levelRequirement,
				hpBonus,
				mpBonus,
				spBonus,
				attackBonus,
				defenseBonus,
				magicBonus,
				resistanceBonus,
				speedBonus,
				luckBonus
		);

		Objects.requireNonNull(armorWeight, "Armor weight cannot be null.");

		if (!hasAnyStatBonus()) {
			throw new IllegalArgumentException("Armor must provide at least one stat bonus: " + itemId);
		}

		this.armorWeight = armorWeight;
	}

	@Override
	public ItemCategory getItemCategory() {
		return ItemCategory.ARMOR;
	}

	public ArmorWeight getArmorWeight() {
		return armorWeight;
	}
}