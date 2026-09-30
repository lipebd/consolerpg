package rpg.characters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

import rpg.items.equipment.ArmorDefinition;
import rpg.items.equipment.EquipmentDefinition;
import rpg.items.equipment.TrinketDefinition;
import rpg.items.equipment.WeaponDefinition;

// Stores the equipment currently used by a playable character.
public final class CharacterEquipment {

	private WeaponDefinition weapon;
	private ArmorDefinition armor;
	private TrinketDefinition trinket;

	CharacterEquipment() {
	}

	WeaponDefinition equipWeapon(WeaponDefinition newWeapon) {
		WeaponDefinition previousWeapon = weapon;

		weapon = newWeapon;

		return previousWeapon;
	}

	ArmorDefinition equipArmor(ArmorDefinition newArmor) {
		ArmorDefinition previousArmor = armor;

		armor = newArmor;

		return previousArmor;
	}

	TrinketDefinition equipTrinket(TrinketDefinition newTrinket) {
		TrinketDefinition previousTrinket = trinket;

		trinket = newTrinket;

		return previousTrinket;
	}

	WeaponDefinition unequipWeapon() {
		WeaponDefinition previousWeapon = weapon;

		weapon = null;

		return previousWeapon;
	}

	ArmorDefinition unequipArmor() {
		ArmorDefinition previousArmor = armor;

		armor = null;

		return previousArmor;
	}

	TrinketDefinition unequipTrinket() {
		TrinketDefinition previousTrinket = trinket;

		trinket = null;

		return previousTrinket;
	}

	public Optional<WeaponDefinition> getWeapon() {
		return Optional.ofNullable(weapon);
	}

	public Optional<ArmorDefinition> getArmor() {
		return Optional.ofNullable(armor);
	}

	public Optional<TrinketDefinition> getTrinket() {
		return Optional.ofNullable(trinket);
	}

	public int getExperienceModifierPercent() {
		if (trinket == null) {
			return 0;
		}

		return trinket.getExperienceModifierPercent();
	}

	public int getGoldModifierPercent() {
		if (trinket == null) {
			return 0;
		}

		return trinket.getGoldModifierPercent();
	}

	public List<EquipmentDefinition> getAll() {
		List<EquipmentDefinition> equippedDefinitions = new ArrayList<>();

		if (weapon != null) {
			equippedDefinitions.add(weapon);
		}

		if (armor != null) {
			equippedDefinitions.add(armor);
		}

		if (trinket != null) {
			equippedDefinitions.add(trinket);
		}

		return List.copyOf(equippedDefinitions);
	}

	public int getHpBonus() {
		return sumBonuses(EquipmentDefinition::getHpBonus);
	}

	public int getMpBonus() {
		return sumBonuses(EquipmentDefinition::getMpBonus);
	}

	public int getSpBonus() {
		return sumBonuses(EquipmentDefinition::getSpBonus);
	}

	public int getAttackBonus() {
		return sumBonuses(EquipmentDefinition::getAttackBonus);
	}

	public int getDefenseBonus() {
		return sumBonuses(EquipmentDefinition::getDefenseBonus);
	}

	public int getMagicBonus() {
		return sumBonuses(EquipmentDefinition::getMagicBonus);
	}

	public int getResistanceBonus() {
		return sumBonuses(EquipmentDefinition::getResistanceBonus);
	}

	public int getSpeedBonus() {
		return sumBonuses(EquipmentDefinition::getSpeedBonus);
	}

	public int getLuckBonus() {
		return sumBonuses(EquipmentDefinition::getLuckBonus);
	}

	private int sumBonuses(ToIntFunction<EquipmentDefinition> bonusExtractor) {
		int total = 0;

		for (EquipmentDefinition definition : getAll()) {
			total = Math.addExact(total, bonusExtractor.applyAsInt(definition));
		}

		return total;
	}
}