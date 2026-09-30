package rpg.classes;

import java.util.Objects;

import rpg.characters.CharacterAttribute;
import rpg.characters.CharacterStats;
import rpg.characters.PlayableCharacterLevelRules;

// Calculates a playable character's natural stats from class and level.
public final class CharacterGrowthCalculator {

	private static final int BASE_HP_GROWTH_PER_LEVEL = 25;
	private static final int BASE_MP_GROWTH_PER_LEVEL = 10;
	private static final int BASE_SP_GROWTH_PER_LEVEL = 10;

	private static final int BASE_ATTRIBUTE_GROWTH_PER_LEVEL = 1;

	private static final int PRIMARY_ATTRIBUTE_GROWTH = 1;
	private static final int SECONDARY_ATTRIBUTE_GROWTH = 1;

	private static final int PRIMARY_GROWTH_INTERVAL = 2;
	private static final int SECONDARY_GROWTH_INTERVAL = 3;

	public CharacterStats calculateNaturalStats(PlayableClassDefinition classDefinition, int level) {
		Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.");

		PlayableCharacterLevelRules.validateLevel(level);

		CharacterStats baseStats = classDefinition.getBaseStats();

		CharacterAttribute primaryAttribute = classDefinition.getPrimaryAttribute();

		CharacterAttribute secondaryAttribute = classDefinition.getSecondaryAttribute();

		int gainedLevels = level - 1;
		int primaryGrowthOccurrences = level / PRIMARY_GROWTH_INTERVAL;
		int secondaryGrowthOccurrences = level / SECONDARY_GROWTH_INTERVAL;

		int defensiveGrowthOccurrences = getSpecializedGrowthOccurrences(
				primaryAttribute,
				secondaryAttribute,
				primaryGrowthOccurrences,
				secondaryGrowthOccurrences,
				CharacterAttribute.DEF,
				CharacterAttribute.RES
		);

		int magicGrowthOccurrences = getSpecializedGrowthOccurrences(
				primaryAttribute,
				secondaryAttribute,
				primaryGrowthOccurrences,
				secondaryGrowthOccurrences,
				CharacterAttribute.MAG
		);

		int attackGrowthOccurrences = getSpecializedGrowthOccurrences(
				primaryAttribute,
				secondaryAttribute,
				primaryGrowthOccurrences,
				secondaryGrowthOccurrences,
				CharacterAttribute.ATK
		);

		return new CharacterStats(
				calculateResource(
						baseStats.getMaxHp(),
						BASE_HP_GROWTH_PER_LEVEL,
						gainedLevels,
						defensiveGrowthOccurrences
				),
				calculateResource(baseStats.getMaxMp(), BASE_MP_GROWTH_PER_LEVEL, gainedLevels, magicGrowthOccurrences),
				calculateResource(
						baseStats.getMaxSp(),
						BASE_SP_GROWTH_PER_LEVEL,
						gainedLevels,
						attackGrowthOccurrences
				),
				calculateAttribute(
						baseStats.getAttack(),
						CharacterAttribute.ATK,
						primaryAttribute,
						secondaryAttribute,
						gainedLevels,
						primaryGrowthOccurrences,
						secondaryGrowthOccurrences
				),
				calculateAttribute(
						baseStats.getDefense(),
						CharacterAttribute.DEF,
						primaryAttribute,
						secondaryAttribute,
						gainedLevels,
						primaryGrowthOccurrences,
						secondaryGrowthOccurrences
				),
				calculateAttribute(
						baseStats.getMagic(),
						CharacterAttribute.MAG,
						primaryAttribute,
						secondaryAttribute,
						gainedLevels,
						primaryGrowthOccurrences,
						secondaryGrowthOccurrences
				),
				calculateAttribute(
						baseStats.getResistance(),
						CharacterAttribute.RES,
						primaryAttribute,
						secondaryAttribute,
						gainedLevels,
						primaryGrowthOccurrences,
						secondaryGrowthOccurrences
				),
				calculateAttribute(
						baseStats.getSpeed(),
						CharacterAttribute.VEL,
						primaryAttribute,
						secondaryAttribute,
						gainedLevels,
						primaryGrowthOccurrences,
						secondaryGrowthOccurrences
				),
				calculateAttribute(
						baseStats.getLuck(),
						CharacterAttribute.LCK,
						primaryAttribute,
						secondaryAttribute,
						gainedLevels,
						primaryGrowthOccurrences,
						secondaryGrowthOccurrences
				)
		);
	}

	private int
			calculateResource(int baseValue, int growthPerLevel, int gainedLevels, int specializedGrowthOccurrences) {
		int generalGrowth = Math.multiplyExact(growthPerLevel, gainedLevels);

		int specializedGrowth = Math.multiplyExact(growthPerLevel, specializedGrowthOccurrences);

		return Math.addExact(baseValue, Math.addExact(generalGrowth, specializedGrowth));
	}

	private int calculateAttribute(
			int baseValue,
			CharacterAttribute attribute,
			CharacterAttribute primaryAttribute,
			CharacterAttribute secondaryAttribute,
			int gainedLevels,
			int primaryGrowthOccurrences,
			int secondaryGrowthOccurrences
	) {
		int growth = Math.multiplyExact(BASE_ATTRIBUTE_GROWTH_PER_LEVEL, gainedLevels);

		if (attribute == primaryAttribute) {
			int primaryGrowth = Math.multiplyExact(PRIMARY_ATTRIBUTE_GROWTH, primaryGrowthOccurrences);

			growth = Math.addExact(growth, primaryGrowth);
		}

		if (attribute == secondaryAttribute) {
			int secondaryGrowth = Math.multiplyExact(SECONDARY_ATTRIBUTE_GROWTH, secondaryGrowthOccurrences);

			growth = Math.addExact(growth, secondaryGrowth);
		}

		return Math.addExact(baseValue, growth);
	}

	private int getSpecializedGrowthOccurrences(
			CharacterAttribute primaryAttribute,
			CharacterAttribute secondaryAttribute,
			int primaryGrowthOccurrences,
			int secondaryGrowthOccurrences,
			CharacterAttribute... specializedAttributes
	) {
		int occurrences = 0;

		if (containsAttribute(primaryAttribute, specializedAttributes)) {
			occurrences = Math.addExact(occurrences, primaryGrowthOccurrences);
		}

		if (containsAttribute(secondaryAttribute, specializedAttributes)) {
			occurrences = Math.addExact(occurrences, secondaryGrowthOccurrences);
		}

		return occurrences;
	}

	private boolean containsAttribute(CharacterAttribute attribute, CharacterAttribute[] expectedAttributes) {
		for (CharacterAttribute expectedAttribute : expectedAttributes) {

			if (attribute == expectedAttribute) {
				return true;
			}
		}

		return false;
	}
}