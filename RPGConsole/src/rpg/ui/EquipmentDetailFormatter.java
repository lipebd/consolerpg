package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

import rpg.characters.PlayableCharacter;
import rpg.items.ItemCategory;
import rpg.items.Item;
import rpg.items.equipment.ArmorDefinition;
import rpg.items.equipment.EquipmentDefinition;
import rpg.items.equipment.TrinketDefinition;
import rpg.items.equipment.WeaponDefinition;

// Formats equipment information for console interfaces.
public final class EquipmentDetailFormatter {

	private EquipmentDetailFormatter() {
	}

	public static String formatDetails(Item item, EquipmentDefinition definition) {
		Objects.requireNonNull(item, "Item cannot be null.");
		Objects.requireNonNull(definition, "Equipment definition cannot be null.");

		if (!item.getId().equals(definition.getItemId())) {
			throw new IllegalArgumentException("Item and equipment definition IDs must match.");
		}

		List<String> lines = new ArrayList<>();

		if (definition instanceof WeaponDefinition weaponDefinition) {
			addWeaponDetails(lines, weaponDefinition);
		} else if (definition instanceof ArmorDefinition armorDefinition) {
			addArmorDetails(lines, armorDefinition);
		} else if (definition instanceof TrinketDefinition trinketDefinition) {
			addTrinketDetails(lines, trinketDefinition);
		} else {
			throw new IllegalArgumentException("Unsupported equipment definition: " + definition.getItemId());
		}

		lines.add(formatDetailLine("Bônus:", formatBonuses(definition)));

		return String.join(System.lineSeparator(), lines);
	}

	public static String formatBonuses(EquipmentDefinition definition) {
		Objects.requireNonNull(definition, "Equipment definition cannot be null.");

		List<String> bonuses = new ArrayList<>();

		addBonus(bonuses, "HP", definition.getHpBonus());
		addBonus(bonuses, "MP", definition.getMpBonus());
		addBonus(bonuses, "SP", definition.getSpBonus());
		addBonus(bonuses, "ATK", definition.getAttackBonus());
		addBonus(bonuses, "DEF", definition.getDefenseBonus());
		addBonus(bonuses, "MAG", definition.getMagicBonus());
		addBonus(bonuses, "RES", definition.getResistanceBonus());
		addBonus(bonuses, "VEL", definition.getSpeedBonus());
		addBonus(bonuses, "LCK", definition.getLuckBonus());

		if (definition instanceof TrinketDefinition trinketDefinition) {
			addPercentageBonus(bonuses, "M.EXP", trinketDefinition.getExperienceModifierPercent());

			addPercentageBonus(bonuses, "M.GOLD", trinketDefinition.getGoldModifierPercent());
		}

		if (bonuses.isEmpty()) {
			return "--";
		}

		return String.join(", ", bonuses);
	}

	public static String formatComparison(
			PlayableCharacter character,
			Item currentItem,
			EquipmentDefinition currentDefinition,
			Item newItem,
			EquipmentDefinition newDefinition
	) {
		Objects.requireNonNull(character, "Playable character cannot be null.");

		validateItemDefinitionPair(currentItem, currentDefinition, "Current");

		validateItemDefinitionPair(newItem, newDefinition, "New");

		if (currentDefinition == null && newDefinition == null) {
			throw new IllegalArgumentException("At least one equipment definition must be provided.");
		}

		if (currentDefinition != null && newDefinition != null
				&& currentDefinition.getItemCategory() != newDefinition.getItemCategory()) {
			throw new IllegalArgumentException("Compared equipment must belong to the same slot.");
		}

		EquipmentDefinition slotDefinition = newDefinition != null ? newDefinition : currentDefinition;

		List<String> lines = new ArrayList<>();
		List<String> changes = new ArrayList<>();

		lines.add(
				getSlotDisplayName(slotDefinition.getItemCategory()) + ": " + getItemName(currentItem) + " → "
						+ getItemName(newItem)
		);

		if (slotDefinition.getItemCategory() == ItemCategory.WEAPON) {
			addWeaponComparison(changes, currentDefinition, newDefinition);
		}

		addStatChange(
				changes,
				"HP máx.",
				character.getMaxHp(),
				currentDefinition,
				newDefinition,
				EquipmentDefinition::getHpBonus
		);

		addStatChange(
				changes,
				"MP máx.",
				character.getMaxMp(),
				currentDefinition,
				newDefinition,
				EquipmentDefinition::getMpBonus
		);

		addStatChange(
				changes,
				"SP máx.",
				character.getMaxSp(),
				currentDefinition,
				newDefinition,
				EquipmentDefinition::getSpBonus
		);

		addStatChange(
				changes,
				"ATK",
				character.getAttack(),
				currentDefinition,
				newDefinition,
				EquipmentDefinition::getAttackBonus
		);

		addStatChange(
				changes,
				"DEF",
				character.getDefense(),
				currentDefinition,
				newDefinition,
				EquipmentDefinition::getDefenseBonus
		);

		addStatChange(
				changes,
				"MAG",
				character.getMagic(),
				currentDefinition,
				newDefinition,
				EquipmentDefinition::getMagicBonus
		);

		addStatChange(
				changes,
				"RES",
				character.getResistance(),
				currentDefinition,
				newDefinition,
				EquipmentDefinition::getResistanceBonus
		);

		addStatChange(
				changes,
				"VEL",
				character.getSpeed(),
				currentDefinition,
				newDefinition,
				EquipmentDefinition::getSpeedBonus
		);

		addStatChange(
				changes,
				"LCK",
				character.getLuck(),
				currentDefinition,
				newDefinition,
				EquipmentDefinition::getLuckBonus
		);

		if (slotDefinition.getItemCategory() == ItemCategory.TRINKET) {
			addTrinketComparison(changes, currentDefinition, newDefinition);
		}

		if (!changes.isEmpty()) {
			lines.add("");
			lines.addAll(changes);
		}

		return String.join(System.lineSeparator(), lines);
	}

	private static void addWeaponDetails(List<String> lines, WeaponDefinition definition) {
		lines.add(formatDetailLine("Ataque:", definition.getNormalAttackName()));
		lines.add(formatDetailLine("Dano:", definition.getDamageType()));
		lines.add(formatDetailLine("Poder:", definition.getBaseWeaponDamage()));
	}

	private static void addArmorDetails(List<String> lines, ArmorDefinition definition) {
		lines.add(formatDetailLine("Peso:", definition.getArmorWeight()));
	}

	private static void addTrinketDetails(List<String> lines, TrinketDefinition definition) {
		lines.add(formatDetailLine("Tipo:", definition.getTrinketType()));
	}

	private static void addBonus(List<String> bonuses, String label, int value) {
		if (value > 0) {
			bonuses.add(label + " +" + value);
		}
	}

	private static String formatDetailLine(String label, Object value) {
		return String.format("%-8s%s", label, value);
	}

	private static void addPercentageBonus(List<String> bonuses, String label, int value) {
		if (value > 0) {
			bonuses.add(label + " +" + value + "%");
		}
	}

	private static void validateItemDefinitionPair(Item item, EquipmentDefinition definition, String pairName) {
		if ((item == null) != (definition == null)) {
			throw new IllegalArgumentException(pairName + " item and definition must both be present or absent.");
		}

		if (item != null && !item.getId().equals(definition.getItemId())) {
			throw new IllegalArgumentException(pairName + " item and definition IDs must match.");
		}
	}

	private static String getItemName(Item item) {
		return item == null ? "Nenhum" : item.getName();
	}

	private static String getSlotDisplayName(ItemCategory category) {
		return switch (category) {
		case WEAPON -> "Arma";
		case ARMOR -> "Armadura";
		case TRINKET -> "Berloque";

		default -> throw new IllegalArgumentException("Item category is not an equipment slot: " + category);
		};
	}

	private static void addWeaponComparison(
			List<String> changes,
			EquipmentDefinition currentDefinition,
			EquipmentDefinition newDefinition
	) {
		WeaponDefinition currentWeapon = currentDefinition instanceof WeaponDefinition weapon ? weapon : null;

		WeaponDefinition newWeapon = newDefinition instanceof WeaponDefinition weapon ? weapon : null;

		addTextChange(
				changes,
				"Ataque",
				currentWeapon == null ? "Nenhum" : currentWeapon.getNormalAttackName(),
				newWeapon == null ? "Nenhum" : newWeapon.getNormalAttackName()
		);

		addTextChange(
				changes,
				"Dano",
				currentWeapon == null ? "Nenhum" : currentWeapon.getDamageType().toString(),
				newWeapon == null ? "Nenhum" : newWeapon.getDamageType().toString()
		);

		addNumericChange(
				changes,
				"Poder",
				currentWeapon == null ? 0 : currentWeapon.getBaseWeaponDamage(),
				newWeapon == null ? 0 : newWeapon.getBaseWeaponDamage()
		);
	}

	private static void addStatChange(
			List<String> changes,
			String label,
			int currentValue,
			EquipmentDefinition currentDefinition,
			EquipmentDefinition newDefinition,
			ToIntFunction<EquipmentDefinition> bonusExtractor
	) {
		int currentEquipmentBonus = currentDefinition == null ? 0 : bonusExtractor.applyAsInt(currentDefinition);

		int newEquipmentBonus = newDefinition == null ? 0 : bonusExtractor.applyAsInt(newDefinition);

		int difference = Math.subtractExact(newEquipmentBonus, currentEquipmentBonus);

		if (difference == 0) {
			return;
		}

		int newValue = Math.addExact(currentValue, difference);

		addNumericChange(changes, label, currentValue, newValue);
	}

	private static void addTrinketComparison(
			List<String> changes,
			EquipmentDefinition currentDefinition,
			EquipmentDefinition newDefinition
	) {
		TrinketDefinition currentTrinket = currentDefinition instanceof TrinketDefinition trinket ? trinket : null;

		TrinketDefinition newTrinket = newDefinition instanceof TrinketDefinition trinket ? trinket : null;

		addMultiplierChange(
				changes,
				"M.EXP",
				currentTrinket == null ? 0 : currentTrinket.getExperienceModifierPercent(),
				newTrinket == null ? 0 : newTrinket.getExperienceModifierPercent()
		);

		addMultiplierChange(
				changes,
				"M.GOLD",
				currentTrinket == null ? 0 : currentTrinket.getGoldModifierPercent(),
				newTrinket == null ? 0 : newTrinket.getGoldModifierPercent()
		);
	}

	private static void addTextChange(List<String> changes, String label, String currentValue, String newValue) {
		if (currentValue.equals(newValue)) {
			return;
		}

		changes.add(String.format("%-8s %s → %s", label + ":", currentValue, newValue));
	}

	private static void addNumericChange(List<String> changes, String label, int currentValue, int newValue) {
		if (currentValue == newValue) {
			return;
		}

		int difference = Math.subtractExact(newValue, currentValue);

		changes.add(String.format("%-8s %-3d → %-3d (%+d)", label + ":", currentValue, newValue, difference));
	}

	private static void addMultiplierChange(
			List<String> changes,
			String label,
			int currentModifierPercent,
			int newModifierPercent
	) {
		if (currentModifierPercent == newModifierPercent) {
			return;
		}

		int difference = Math.subtractExact(newModifierPercent, currentModifierPercent);

		changes.add(
				String.format(
						"%-8s %-5s → %-5s (%+d%%)",
						label + ":",
						RewardModifierFormatter.formatMultiplier(currentModifierPercent),
						RewardModifierFormatter.formatMultiplier(newModifierPercent),
						difference
				)
		);
	}

}
