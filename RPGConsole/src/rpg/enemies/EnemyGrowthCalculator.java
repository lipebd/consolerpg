package rpg.enemies;

import java.util.Objects;

import rpg.characters.CharacterAttribute;
import rpg.characters.CharacterStats;
import rpg.characters.EnemyRank;

// Calculates an enemy's natural stats from its level-one base stats.
public final class EnemyGrowthCalculator {

	private static final int RESOURCE_GROWTH_PER_LEVEL = 5;

	public CharacterStats calculateNaturalStats(
			CharacterStats baseStats,
			CharacterAttribute primaryAttribute,
			CharacterAttribute secondaryAttribute,
			CharacterAttribute weaknessAttribute,
			EnemyRank rank,
			int level
	) {
		Objects.requireNonNull(baseStats, "Enemy base stats cannot be null.");
		Objects.requireNonNull(rank, "Enemy rank cannot be null.");

		validateGrowthAttributes(primaryAttribute, secondaryAttribute, weaknessAttribute);

		if (level < 1) {
			throw new IllegalArgumentException("Enemy level must be at least one.");
		}

		int gainedLevels = level - 1;

		int hpMultiplierPercent = rank.getHpMultiplierPercent();

		int offensiveMultiplierPercent = rank.getOffensiveMultiplierPercent();

		int maxHp = Math.addExact(baseStats.getMaxHp(), Math.multiplyExact(10, gainedLevels));

		int maxMp = Math.addExact(baseStats.getMaxMp(), Math.multiplyExact(RESOURCE_GROWTH_PER_LEVEL, gainedLevels));

		int maxSp = Math.addExact(baseStats.getMaxSp(), Math.multiplyExact(RESOURCE_GROWTH_PER_LEVEL, gainedLevels));

		int attack = growAttribute(
				baseStats.getAttack(),
				CharacterAttribute.ATK,
				primaryAttribute,
				secondaryAttribute,
				weaknessAttribute,
				level
		);

		int magic = growAttribute(
				baseStats.getMagic(),
				CharacterAttribute.MAG,
				primaryAttribute,
				secondaryAttribute,
				weaknessAttribute,
				level
		);

		return new CharacterStats(
				applyRankMultiplier(maxHp, hpMultiplierPercent),
				maxMp,
				maxSp,
				applyRankMultiplier(attack, offensiveMultiplierPercent),
				growAttribute(
						baseStats.getDefense(),
						CharacterAttribute.DEF,
						primaryAttribute,
						secondaryAttribute,
						weaknessAttribute,
						level
				),
				applyRankMultiplier(magic, offensiveMultiplierPercent),
				growAttribute(
						baseStats.getResistance(),
						CharacterAttribute.RES,
						primaryAttribute,
						secondaryAttribute,
						weaknessAttribute,
						level
				),
				growAttribute(
						baseStats.getSpeed(),
						CharacterAttribute.VEL,
						primaryAttribute,
						secondaryAttribute,
						weaknessAttribute,
						level
				),
				growAttribute(
						baseStats.getLuck(),
						CharacterAttribute.LCK,
						primaryAttribute,
						secondaryAttribute,
						weaknessAttribute,
						level
				)
		);
	}

	public void validateGrowthAttributes(
			CharacterAttribute primaryAttribute,
			CharacterAttribute secondaryAttribute,
			CharacterAttribute weaknessAttribute
	) {
		if (primaryAttribute != null && primaryAttribute == secondaryAttribute) {
			throw new IllegalArgumentException("Enemy primary and secondary attributes must be different.");
		}

		if (weaknessAttribute != null
				&& (weaknessAttribute == primaryAttribute || weaknessAttribute == secondaryAttribute)) {
			throw new IllegalArgumentException(
					"Enemy weakness attribute must differ from primary and secondary attributes."
			);
		}
	}

	private int growAttribute(
			int baseValue,
			CharacterAttribute attribute,
			CharacterAttribute primaryAttribute,
			CharacterAttribute secondaryAttribute,
			CharacterAttribute weaknessAttribute,
			int level
	) {
		if (attribute == weaknessAttribute) {
			return Math.addExact(baseValue, level / 2);
		}

		int growthPerLevel = attribute == CharacterAttribute.ATK || attribute == CharacterAttribute.MAG ? 3 : 1;

		int value = Math.addExact(baseValue, Math.multiplyExact(growthPerLevel, level - 1));

		if (attribute == primaryAttribute) {
			value = Math.addExact(value, level / 2);
		}

		if (attribute == secondaryAttribute) {
			value = Math.addExact(value, level / 3);
		}

		return value;
	}

	private int applyRankMultiplier(int value, int multiplierPercent) {
		long scaledValue = Math.multiplyExact((long) value, multiplierPercent);
		long roundedValue = Math.addExact(scaledValue, 50L) / 100L;

		return Math.toIntExact(roundedValue);
	}
}