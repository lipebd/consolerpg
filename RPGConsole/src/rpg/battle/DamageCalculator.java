package rpg.battle;

import java.util.Objects;

import rpg.characters.GameCharacter;
import rpg.skills.DamageType;

// Calculates damage before defending, rounding and HP limits.
public final class DamageCalculator {

	private static final int PHYSICAL_BALANCE_FACTOR = 20;
	private static final int MAGICAL_BALANCE_FACTOR = 20;

	public double calculateDamage(
			GameCharacter attacker,
			GameCharacter target,
			DamageType damageType,
			int skillPower,
			int weaponBaseDamage,
			int powerModifierPercent
	) {
		Objects.requireNonNull(attacker, "Damage attacker cannot be null.");

		Objects.requireNonNull(target, "Damage target cannot be null.");

		Objects.requireNonNull(damageType, "Damage type cannot be null.");

		if (skillPower < 0) {
			throw new IllegalArgumentException("Skill power cannot be negative.");
		}

		if (weaponBaseDamage < 0) {
			throw new IllegalArgumentException("Weapon base damage cannot be negative.");
		}

		if (powerModifierPercent < 0) {
			throw new IllegalArgumentException("Damage power modifier cannot be negative.");
		}

		int offensiveAttribute = getOffensiveAttribute(attacker, damageType);

		int defensiveAttribute = getDefensiveAttribute(target, damageType);

		int balanceFactor = getBalanceFactor(damageType);

		long basePower = (long) skillPower + weaponBaseDamage + offensiveAttribute;

		double modifiedPower = basePower * (1.0 + powerModifierPercent / 100.0);

		double calculatedDamage = modifiedPower * balanceFactor / (defensiveAttribute + balanceFactor);

		if (!Double.isFinite(calculatedDamage)) {
			throw new IllegalStateException("Calculated damage is not finite.");
		}

		return calculatedDamage;
	}

	public int calculateRecoilDamage(int actualDamageDealt, int recoilPercent) {
		if (actualDamageDealt < 0) {
			throw new IllegalArgumentException("Actual damage dealt cannot be negative.");
		}

		if (recoilPercent < 1 || recoilPercent > 100) {
			throw new IllegalArgumentException("Recoil percentage must be between 1 and 100.");
		}

		long scaledDamage = (long) actualDamageDealt * recoilPercent;

		return (int) Math.round(scaledDamage / 100.0);
	}

	private int getOffensiveAttribute(GameCharacter attacker, DamageType damageType) {
		return switch (damageType) {
		case PHYSICAL -> attacker.getAttack();
		case MAGICAL -> attacker.getMagic();
		};
	}

	private int getDefensiveAttribute(GameCharacter target, DamageType damageType) {
		return switch (damageType) {
		case PHYSICAL -> target.getDefense();
		case MAGICAL -> target.getResistance();
		};
	}

	private int getBalanceFactor(DamageType damageType) {
		return switch (damageType) {
		case PHYSICAL -> PHYSICAL_BALANCE_FACTOR;
		case MAGICAL -> MAGICAL_BALANCE_FACTOR;
		};
	}
}