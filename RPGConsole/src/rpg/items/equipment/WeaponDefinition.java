package rpg.items.equipment;

import java.util.Objects;

import rpg.battle.DamageType;
import rpg.items.ItemCategory;

// Stores the immutable specialized data of a weapon.
public final class WeaponDefinition extends EquipmentDefinition {

	private final DamageType damageType;
	private final WeaponType weaponType;
	private final String normalAttackName;
	private final int baseWeaponDamage;

	public WeaponDefinition(
			String itemId,
			DamageType damageType,
			WeaponType weaponType,
			int levelRequirement,
			String normalAttackName,
			int baseWeaponDamage,
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

		Objects.requireNonNull(damageType, "Weapon damage type cannot be null.");
		Objects.requireNonNull(weaponType, "Weapon type cannot be null.");

		if (normalAttackName == null || normalAttackName.isBlank()) {
			throw new IllegalArgumentException("Weapon normal attack name cannot be empty: " + itemId);
		}

		if (baseWeaponDamage <= 0) {
			throw new IllegalArgumentException("Weapon base damage must be positive: " + itemId);
		}

		this.damageType = damageType;
		this.weaponType = weaponType;
		this.normalAttackName = normalAttackName.trim();
		this.baseWeaponDamage = baseWeaponDamage;
	}

	@Override
	public ItemCategory getItemCategory() {
		return ItemCategory.WEAPON;
	}

	public DamageType getDamageType() {
		return damageType;
	}

	public WeaponType getWeaponType() {
		return weaponType;
	}

	public String getNormalAttackName() {
		return normalAttackName;
	}

	public int getBaseWeaponDamage() {
		return baseWeaponDamage;
	}
}