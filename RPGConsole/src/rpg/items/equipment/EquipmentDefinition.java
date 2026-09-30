package rpg.items.equipment;

import rpg.items.ItemCategory;
import rpg.characters.PlayableCharacterLevelRules;

// Stores the immutable data shared by all equipment definitions.
public abstract class EquipmentDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private final String itemId;
	private final int levelRequirement;

	private final int hpBonus;
	private final int mpBonus;
	private final int spBonus;
	private final int attackBonus;
	private final int defenseBonus;
	private final int magicBonus;
	private final int resistanceBonus;
	private final int speedBonus;
	private final int luckBonus;

	protected EquipmentDefinition(
			String itemId,
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
		if (itemId == null || itemId.isBlank()) {
			throw new IllegalArgumentException("Equipment item ID cannot be empty.");
		}

		String trimmedItemId = itemId.trim();

		if (!trimmedItemId.matches(ID_PATTERN)) {
			throw new IllegalArgumentException(
					"Equipment item ID must use lowercase letters, numbers and single underscores: " + trimmedItemId
			);
		}

		PlayableCharacterLevelRules.validateLevel(levelRequirement);

		this.itemId = trimmedItemId;
		this.levelRequirement = levelRequirement;
		this.hpBonus = requireNonNegativeBonus("HP bonus", hpBonus);
		this.mpBonus = requireNonNegativeBonus("MP bonus", mpBonus);
		this.spBonus = requireNonNegativeBonus("SP bonus", spBonus);
		this.attackBonus = requireNonNegativeBonus("ATK bonus", attackBonus);
		this.defenseBonus = requireNonNegativeBonus("DEF bonus", defenseBonus);
		this.magicBonus = requireNonNegativeBonus("MAG bonus", magicBonus);
		this.resistanceBonus = requireNonNegativeBonus("RES bonus", resistanceBonus);
		this.speedBonus = requireNonNegativeBonus("VEL bonus", speedBonus);
		this.luckBonus = requireNonNegativeBonus("LCK bonus", luckBonus);
	}

	protected final int requireNonNegativeBonus(String bonusName, int value) {
		if (value < 0) {
			throw new IllegalArgumentException("Equipment " + bonusName + " cannot be negative: " + itemId);
		}

		return value;
	}

	public final boolean hasAnyStatBonus() {
		return hpBonus > 0 || mpBonus > 0 || spBonus > 0 || attackBonus > 0 || defenseBonus > 0 || magicBonus > 0
				|| resistanceBonus > 0 || speedBonus > 0 || luckBonus > 0;
	}

	public final String getItemId() {
		return itemId;
	}

	public final int getLevelRequirement() {
		return levelRequirement;
	}

	public final int getHpBonus() {
		return hpBonus;
	}

	public final int getMpBonus() {
		return mpBonus;
	}

	public final int getSpBonus() {
		return spBonus;
	}

	public final int getAttackBonus() {
		return attackBonus;
	}

	public final int getDefenseBonus() {
		return defenseBonus;
	}

	public final int getMagicBonus() {
		return magicBonus;
	}

	public final int getResistanceBonus() {
		return resistanceBonus;
	}

	public final int getSpeedBonus() {
		return speedBonus;
	}

	public final int getLuckBonus() {
		return luckBonus;
	}

	public abstract ItemCategory getItemCategory();
}