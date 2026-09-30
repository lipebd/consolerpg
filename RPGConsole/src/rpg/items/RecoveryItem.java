package rpg.items;

import rpg.characters.PlayableCharacter;

// Represents an item with one or more recovery effects.
public final class RecoveryItem extends Item {

	private final RecoveryItemDefinition recoveryDefinition;

	public RecoveryItem(ItemDefinition itemDefinition, RecoveryItemDefinition recoveryDefinition) {
		super(itemDefinition);

		if (recoveryDefinition == null) {
			throw new IllegalArgumentException("Recovery item definition cannot be null.");
		}

		if (!getId().equals(recoveryDefinition.getItemId())) {
			throw new IllegalArgumentException("Item and recovery definitions must use the same ID.");
		}

		if (getCategory() != ItemCategory.CONSUMABLE) {
			throw new IllegalArgumentException("Recovery item must use the CONSUMABLE category: " + getId());
		}

		this.recoveryDefinition = recoveryDefinition;
	}

	public boolean canAffect(PlayableCharacter target) {
		if (target == null) {
			return false;
		}

		if (!target.isAlive()) {
			return getReviveHpRecovery() > 0;
		}

		return (getHpRecovery() > 0 && target.getCurrentHp() < target.getMaxHp())
				|| (getMpRecovery() > 0 && target.getCurrentMp() < target.getMaxMp())
				|| (getSpRecovery() > 0 && target.getCurrentSp() < target.getMaxSp())
				|| (purifiesStatusEffects() && target.hasNegativeAttributeStates());
	}

	public RecoveryItemUseResult use(PlayableCharacter target) {
		if (target == null) {
			throw new IllegalArgumentException("Recovery item target cannot be null.");
		}

		int revivedHp = 0;
		int recoveredHp = 0;
		int recoveredMp = 0;
		int recoveredSp = 0;

		if (!target.isAlive()) {
			revivedHp = target.revive(getReviveHpRecovery());

			if (revivedHp == 0) {
				return new RecoveryItemUseResult(0, 0, 0, 0, false);
			}
		}

		recoveredHp = target.recoverHp(getHpRecovery());

		recoveredMp = target.recoverMp(getMpRecovery());

		recoveredSp = target.recoverSp(getSpRecovery());

		boolean purifiedStatusEffects = purifiesStatusEffects() && target.purifyNegativeAttributeStates();

		return new RecoveryItemUseResult(revivedHp, recoveredHp, recoveredMp, recoveredSp, purifiedStatusEffects);
	}

	public RecoveryItemTargetType getTargetType() {
		return recoveryDefinition.getTargetType();
	}

	public int getHpRecovery() {
		return recoveryDefinition.getHpRecovery();
	}

	public int getMpRecovery() {
		return recoveryDefinition.getMpRecovery();
	}

	public int getSpRecovery() {
		return recoveryDefinition.getSpRecovery();
	}

	public int getReviveHpRecovery() {
		return recoveryDefinition.getReviveHpRecovery();
	}

	public boolean purifiesStatusEffects() {
		return recoveryDefinition.purifiesStatusEffects();
	}
}