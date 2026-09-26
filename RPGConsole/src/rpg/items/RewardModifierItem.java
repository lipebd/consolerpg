package rpg.items;

import java.util.Objects;

import rpg.characters.Party;

// Activates temporary party-wide battle reward bonuses.
public final class RewardModifierItem extends Item {

	private final RewardModifierItemDefinition definition;

	public RewardModifierItem(ItemDefinition itemDefinition, RewardModifierItemDefinition definition) {
		super(itemDefinition);

		this.definition = Objects.requireNonNull(definition, "Reward modifier definition cannot be null.");

		if (!getId().equals(definition.itemId())) {
			throw new IllegalArgumentException("Item and reward modifier IDs must match.");
		}

		if (getCategory() != ItemCategory.CONSUMABLE) {
			throw new IllegalArgumentException("Reward modifier item must be CONSUMABLE.");
		}
	}

	public boolean use(Party party) {
		Objects.requireNonNull(party, "Party cannot be null.");

		if (party.getInventory().getQuantity(this) < 1) {
			return false;
		}

		boolean activated = party.getRewardModifierState().activateModifiers(
				definition.experiencePercent(),
				definition.goldPercent(),
				definition.rewardedVictories()
		);

		if (!activated) {
			return false;
		}

		if (!party.getInventory().removeItem(this, 1)) {
			throw new IllegalStateException("Activated reward item could not be consumed.");
		}

		return true;
	}

	public int getExperiencePercent() {
		return definition.experiencePercent();
	}

	public int getGoldPercent() {
		return definition.goldPercent();
	}

	public int getRewardedVictories() {
		return definition.rewardedVictories();
	}
}