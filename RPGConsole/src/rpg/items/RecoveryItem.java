package rpg.items;

import java.util.Set;

import rpg.characters.CharacterAttribute;
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
			return recoveryDefinition.resolveReviveHpRecovery(target.getMaxHp()) > 0;
		}

		return (recoveryDefinition.resolveHpRecovery(target.getMaxHp()) > 0
				&& target.getCurrentHp() < target.getMaxHp())
				|| (recoveryDefinition.resolveMpRecovery(target.getMaxMp()) > 0
						&& target.getCurrentMp() < target.getMaxMp())
				|| (recoveryDefinition.resolveSpRecovery(target.getMaxSp()) > 0
						&& target.getCurrentSp() < target.getMaxSp())
				|| recoveryDefinition.getPurificationAttributes().stream()
						.anyMatch(attribute -> target.getNegativeAttributeStages(attribute) > 0);
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
			revivedHp = target.revive(recoveryDefinition.resolveReviveHpRecovery(target.getMaxHp()));

			if (revivedHp == 0) {
				return new RecoveryItemUseResult(0, 0, 0, 0, false);
			}
		}

		recoveredHp = target.recoverHp(recoveryDefinition.resolveHpRecovery(target.getMaxHp()));

		recoveredMp = target.recoverMp(recoveryDefinition.resolveMpRecovery(target.getMaxMp()));

		recoveredSp = target.recoverSp(recoveryDefinition.resolveSpRecovery(target.getMaxSp()));

		boolean purifiedStatusEffects = purifiesStatusEffects()
				&& target.purifyNegativeAttributeStates(recoveryDefinition.getPurificationAttributes());

		return new RecoveryItemUseResult(revivedHp, recoveredHp, recoveredMp, recoveredSp, purifiedStatusEffects);
	}

	public boolean hasAttributeStage() {
		return recoveryDefinition.getAttribute() != null;
	}

	public boolean canApplyAttributeStage(PlayableCharacter target, PlayableCharacter actingCharacter) {
		if (!hasAttributeStage() || target == null || !target.isAlive()) {
			return false;
		}

		CharacterAttribute attribute = recoveryDefinition.getAttribute();
		int positiveStages = target.getPositiveAttributeStages(attribute);

		if (attribute == CharacterAttribute.VEL) {
			return positiveStages < 3;
		}

		int remainingTurns = target.getPositiveAttributeRemainingTurns(attribute);

		if (target == actingCharacter && remainingTurns == 1) {
			positiveStages = 0;
			remainingTurns = 0;
		} else if (target == actingCharacter && remainingTurns > 1) {
			remainingTurns--;
		}

		return positiveStages < 3 || remainingTurns < recoveryDefinition.getDuration();
	}

	public boolean applyAttributeStage(PlayableCharacter target) {
		if (!hasAttributeStage() || target == null || !target.isAlive()) {
			return false;
		}

		CharacterAttribute attribute = recoveryDefinition.getAttribute();
		int stages = recoveryDefinition.getStages();

		if (attribute == CharacterAttribute.VEL) {
			return target.applyPendingSpeedStages(stages);
		}

		return target.applyTimedAttributeStages(attribute, stages, recoveryDefinition.getDuration());
	}

	public int getStageCount() {
	    return recoveryDefinition.getStages();
	}

	public Integer getStageDuration() {
	    return recoveryDefinition.getDuration();
	}

	public CharacterAttribute getStageAttribute() {
	    return recoveryDefinition.getAttribute();
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

	public int getHpRecoveryPercent() {
		return recoveryDefinition.getHpRecoveryPercent();
	}

	public int getMpRecoveryPercent() {
		return recoveryDefinition.getMpRecoveryPercent();
	}

	public int getSpRecoveryPercent() {
		return recoveryDefinition.getSpRecoveryPercent();
	}

	public int getReviveHpRecoveryPercent() {
		return recoveryDefinition.getReviveHpRecoveryPercent();
	}

	public boolean purifiesStatusEffects() {
		return recoveryDefinition.purifiesStatusEffects();
	}

	public Set<CharacterAttribute> getPurificationAttributes() {
		return recoveryDefinition.getPurificationAttributes();
	}
}