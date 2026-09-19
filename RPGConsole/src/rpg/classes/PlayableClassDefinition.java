package rpg.classes;

import java.util.Objects;

import rpg.characters.CharacterAttribute;
import rpg.characters.CharacterStats;
import rpg.items.equipment.ArmorWeight;

// Stores the immutable data of a playable class.
public final class PlayableClassDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private static final int REQUIRED_RESOURCE_TOTAL = 260;
	private static final int MINIMUM_BASE_HP = 50;
	private static final int MINIMUM_BASE_MP = 25;
	private static final int MINIMUM_BASE_SP = 25;

	private static final int REQUIRED_ATTRIBUTE_TOTAL = 70;
	private static final int MINIMUM_BASE_ATTRIBUTE = 8;
	private static final int MAXIMUM_BASE_ATTRIBUTE = 15;

	private final String id;
	private final String name;
	private final String symbol;
	private final CharacterStats baseStats;
	private final CharacterAttribute primaryAttribute;
	private final CharacterAttribute secondaryAttribute;
	private final ArmorWeight armorWeight;
	private final String description;

	public PlayableClassDefinition(
			String id,
			String name,
			String symbol,
			CharacterStats baseStats,
			CharacterAttribute primaryAttribute,
			CharacterAttribute secondaryAttribute,
			ArmorWeight armorWeight,
			String description
	) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Playable class ID cannot be empty.");
		}

		String trimmedId = id.trim();

		if (!trimmedId.matches(ID_PATTERN)) {
			throw new IllegalArgumentException(
					"Playable class ID must use lowercase letters, numbers and single underscores: " + trimmedId
			);
		}

		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Playable class name cannot be empty: " + trimmedId);
		}

		String trimmedSymbol = symbol.trim();

		if (trimmedSymbol.isBlank()) {
		        throw new IllegalArgumentException("Playable class symbol cannot be empty: " + trimmedId);
		}

		int symbolCodePoint = trimmedSymbol.codePointAt(0);
		int symbolType = Character.getType(symbolCodePoint);

		if (trimmedSymbol.codePointCount(0, trimmedSymbol.length()) != 1
		                || !Character.isDefined(symbolCodePoint)
		                || Character.isISOControl(symbolCodePoint)
		                || Character.isWhitespace(symbolCodePoint)
		                || Character.isIdentifierIgnorable(symbolCodePoint)
		                || symbolType == Character.FORMAT
		                || symbolType == Character.SURROGATE) {
		        throw new IllegalArgumentException(
		                        "Playable class symbol must contain exactly one visible Unicode code point: " + trimmedId
		        );
		}

		Objects.requireNonNull(baseStats, "Playable class base stats cannot be null.");

		Objects.requireNonNull(primaryAttribute, "Playable class primary attribute cannot be null.");

		Objects.requireNonNull(secondaryAttribute, "Playable class secondary attribute cannot be null.");

		if (primaryAttribute == secondaryAttribute) {
			throw new IllegalArgumentException(
					"Playable class primary and secondary attributes must be different: " + trimmedId
			);
		}

		Objects.requireNonNull(armorWeight, "Playable class armor weight cannot be null.");

		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("Playable class description cannot be empty: " + trimmedId);
		}

		validateResourceDistribution(trimmedId, baseStats);
		validateAttributeDistribution(trimmedId, baseStats);

		this.id = trimmedId;
		this.name = name.trim();
		this.symbol = trimmedSymbol;
		this.baseStats = baseStats;
		this.primaryAttribute = primaryAttribute;
		this.secondaryAttribute = secondaryAttribute;
		this.armorWeight = armorWeight;
		this.description = description.trim();
	}

	private void validateResourceDistribution(String classId, CharacterStats stats) {
		if (stats.getMaxHp() < MINIMUM_BASE_HP) {
			throw new IllegalArgumentException(
					"Playable class base HP must be at least " + MINIMUM_BASE_HP + ": " + classId
			);
		}

		if (stats.getMaxMp() < MINIMUM_BASE_MP) {
			throw new IllegalArgumentException(
					"Playable class base MP must be at least " + MINIMUM_BASE_MP + ": " + classId
			);
		}

		if (stats.getMaxSp() < MINIMUM_BASE_SP) {
			throw new IllegalArgumentException(
					"Playable class base SP must be at least " + MINIMUM_BASE_SP + ": " + classId
			);
		}

		long total = (long) stats.getMaxHp() + stats.getMaxMp() + stats.getMaxSp();

		if (total != REQUIRED_RESOURCE_TOTAL) {
			throw new IllegalArgumentException(
					"Playable class base HP, MP and SP must total " + REQUIRED_RESOURCE_TOTAL + ", but " + classId
							+ " totals " + total + "."
			);
		}
	}

	private void validateAttributeDistribution(String classId, CharacterStats stats) {
		validateBaseAttribute(classId, CharacterAttribute.ATK, stats.getAttack());

		validateBaseAttribute(classId, CharacterAttribute.DEF, stats.getDefense());

		validateBaseAttribute(classId, CharacterAttribute.MAG, stats.getMagic());

		validateBaseAttribute(classId, CharacterAttribute.RES, stats.getResistance());

		validateBaseAttribute(classId, CharacterAttribute.VEL, stats.getSpeed());

		validateBaseAttribute(classId, CharacterAttribute.LCK, stats.getLuck());

		long total = (long) stats.getAttack() + stats.getDefense() + stats.getMagic() + stats.getResistance()
				+ stats.getSpeed() + stats.getLuck();

		if (total != REQUIRED_ATTRIBUTE_TOTAL) {
			throw new IllegalArgumentException(
					"Playable class regular base attributes must total " + REQUIRED_ATTRIBUTE_TOTAL + ", but " + classId
							+ " totals " + total + "."
			);
		}
	}

	private void validateBaseAttribute(String classId, CharacterAttribute attribute, int value) {
		if (value < MINIMUM_BASE_ATTRIBUTE || value > MAXIMUM_BASE_ATTRIBUTE) {
			throw new IllegalArgumentException(
					"Playable class base " + attribute + " must be between " + MINIMUM_BASE_ATTRIBUTE + " and "
							+ MAXIMUM_BASE_ATTRIBUTE + ": " + classId
			);
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getTitle() {
		return symbol + " " + name;
	}

	public CharacterStats getBaseStats() {
		return baseStats;
	}

	public CharacterAttribute getPrimaryAttribute() {
		return primaryAttribute;
	}

	public CharacterAttribute getSecondaryAttribute() {
		return secondaryAttribute;
	}

	public ArmorWeight getArmorWeight() {
		return armorWeight;
	}

	public String getDescription() {
		return description;
	}
}