package rpg.items.equipment;

import java.util.Objects;

import rpg.items.ItemCategory;

// Stores the immutable specialized data of a trinket.
public final class TrinketDefinition extends EquipmentDefinition {

	private final TrinketType trinketType;
	private final int experienceModifierPercent;
	private final int goldModifierPercent;

	public TrinketDefinition(
			String itemId,
			TrinketType trinketType,
			int levelRequirement,
			int hpBonus,
			int mpBonus,
			int spBonus,
			int attackBonus,
			int defenseBonus,
			int magicBonus,
			int resistanceBonus,
			int speedBonus,
			int luckBonus,
			int experienceModifierPercent,
			int goldModifierPercent
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

		Objects.requireNonNull(trinketType, "Trinket type cannot be null.");

		this.trinketType = trinketType;
		this.experienceModifierPercent = requireNonNegativeBonus("EXP modifier", experienceModifierPercent);
		this.goldModifierPercent = requireNonNegativeBonus("GOLD modifier", goldModifierPercent);

		if (!hasAnyStatBonus() && experienceModifierPercent == 0 && goldModifierPercent == 0) {
			throw new IllegalArgumentException("Trinket must provide at least one stat or reward bonus: " + itemId);
		}
	}

	@Override
	public ItemCategory getItemCategory() {
		return ItemCategory.TRINKET;
	}

	public TrinketType getTrinketType() {
		return trinketType;
	}

	public int getExperienceModifierPercent() {
		return experienceModifierPercent;
	}

	public int getGoldModifierPercent() {
		return goldModifierPercent;
	}
}