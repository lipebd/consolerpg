package rpg.effects;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import rpg.characters.CharacterAttribute;

// Tracks all mutable temporary attribute states of one character.
public final class CombatEffectState {

	private boolean attributesDistorted;
	private final Map<CharacterAttribute, AttributeStageState> attributeStates;

	public CombatEffectState() {
		attributeStates = new EnumMap<>(CharacterAttribute.class);

		for (CharacterAttribute attribute : CharacterAttribute.values()) {

			attributeStates.put(attribute, new AttributeStageState());
		}
		attributesDistorted = false;
	}

	public boolean applyTimedStages(CharacterAttribute attribute, int stageChange, int duration) {
		Objects.requireNonNull(attribute, "Character attribute cannot be null.");

		if (attribute == CharacterAttribute.VEL) {
			throw new IllegalArgumentException("VEL stages must use the pending speed-stage lifecycle.");
		}

		return getState(attribute).applyTimedStages(stageChange, duration);
	}

	public boolean applyPendingSpeedStages(int stageChange) {
		return getState(CharacterAttribute.VEL).applyPendingStages(stageChange);
	}

	public void finishConsumedTurn() {
		for (CharacterAttribute attribute : CharacterAttribute.values()) {

			if (attribute != CharacterAttribute.VEL) {
				getState(attribute).finishConsumedTurn();
			}
		}
	}

	public boolean hasPositiveStates() {
		for (AttributeStageState state : attributeStates.values()) {

			if (state.hasPositiveState()) {
				return true;
			}
		}

		return false;
	}

	public boolean hasNegativeStates() {
		for (AttributeStageState state : attributeStates.values()) {

			if (state.hasNegativeState()) {
				return true;
			}
		}

		return false;
	}

	public boolean purifyAllNegativeStates() {
	    return purifyNegativeStates(EnumSet.allOf(CharacterAttribute.class));
	}

	public boolean purifyNegativeStates(Set<CharacterAttribute> attributes) {
	    Objects.requireNonNull(attributes, "Purification attributes cannot be null.");

	    boolean changed = false;

	    for (CharacterAttribute attribute : attributes) {
	        changed |= getState(attribute).clearNegative();
	    }

	    return changed;
	}

	public boolean dissipateAllPositiveStates() {
		boolean changed = false;

		for (AttributeStageState state : attributeStates.values()) {

			changed |= state.clearPositive();
		}

		return changed;
	}

	public boolean dissipatePositiveStates(Set<CharacterAttribute> attributes) {
		Objects.requireNonNull(attributes, "Attributes cannot be null.");

		boolean changed = false;

		for (CharacterAttribute attribute : attributes) {
			changed |= getState(attribute).clearPositive();
		}

		return changed;
	}

	public boolean consumePendingSpeedStages() {
		AttributeStageState speedState = getState(CharacterAttribute.VEL);

		boolean changed = speedState.clearPositive();

		changed |= speedState.clearNegative();

		return changed;
	}

	public void toggleDistortedAttributes() {
		attributesDistorted = !attributesDistorted;
	}

	public boolean hasDistortedAttributes() {
		return attributesDistorted;
	}

	public void clearAll() {
		for (AttributeStageState state : attributeStates.values()) {

			state.clearAll();
		}

		attributesDistorted = false;
	}

	public int getEffectiveStage(CharacterAttribute attribute) {
		return getState(attribute).getEffectiveStage();
	}

	public int getPositiveStages(CharacterAttribute attribute) {
		return getState(attribute).getPositiveStages();
	}

	public int getPositiveRemainingTurns(CharacterAttribute attribute) {
		return getState(attribute).getPositiveRemainingTurns();
	}

	public int getNegativeStages(CharacterAttribute attribute) {
		return getState(attribute).getNegativeStages();
	}

	public int getNegativeRemainingTurns(CharacterAttribute attribute) {
		return getState(attribute).getNegativeRemainingTurns();
	}

	private AttributeStageState getState(CharacterAttribute attribute) {
		Objects.requireNonNull(attribute, "Character attribute cannot be null.");

		return attributeStates.get(attribute);
	}
}