package rpg.effects;

// Tracks the independent positive and negative state of one attribute.
final class AttributeStageState {

	private int positiveStages;
	private int positiveRemainingTurns;

	private int negativeStages;
	private int negativeRemainingTurns;

	public boolean applyTimedStages(int stageChange, int duration) {
		validateStageChange(stageChange);

		if (duration <= 0) {
			throw new IllegalArgumentException("Timed attribute-effect duration must be positive.");
		}

		if (stageChange > 0) {
			int previousStages = positiveStages;
			int previousDuration = positiveRemainingTurns;

			positiveStages = Math.min(AttributeStageRules.MAXIMUM_STAGE_MAGNITUDE, positiveStages + stageChange);

			positiveRemainingTurns = Math.max(positiveRemainingTurns, duration);

			return positiveStages != previousStages || positiveRemainingTurns != previousDuration;
		}

		int stageMagnitude = -stageChange;

		int previousStages = negativeStages;
		int previousDuration = negativeRemainingTurns;

		negativeStages = Math.min(AttributeStageRules.MAXIMUM_STAGE_MAGNITUDE, negativeStages + stageMagnitude);

		negativeRemainingTurns = Math.max(negativeRemainingTurns, duration);

		return negativeStages != previousStages || negativeRemainingTurns != previousDuration;
	}

	public boolean applyPendingStages(int stageChange) {
		validateStageChange(stageChange);

		if (stageChange > 0) {
			int previousStages = positiveStages;

			positiveStages = Math.min(AttributeStageRules.MAXIMUM_STAGE_MAGNITUDE, positiveStages + stageChange);

			return positiveStages != previousStages;
		}

		int previousStages = negativeStages;

		negativeStages = Math.min(AttributeStageRules.MAXIMUM_STAGE_MAGNITUDE, negativeStages - stageChange);

		return negativeStages != previousStages;
	}

	public void finishConsumedTurn() {
		if (positiveStages > 0) {
			positiveRemainingTurns--;

			if (positiveRemainingTurns <= 0) {
				clearPositive();
			}
		}

		if (negativeStages > 0) {
			negativeRemainingTurns--;

			if (negativeRemainingTurns <= 0) {
				clearNegative();
			}
		}
	}

	public boolean clearPositive() {
		if (positiveStages == 0) {
			return false;
		}

		positiveStages = 0;
		positiveRemainingTurns = 0;

		return true;
	}

	public boolean clearNegative() {
		if (negativeStages == 0) {
			return false;
		}

		negativeStages = 0;
		negativeRemainingTurns = 0;

		return true;
	}

	public void clearAll() {
		clearPositive();
		clearNegative();
	}

	public boolean hasPositiveState() {
		return positiveStages > 0;
	}

	public boolean hasNegativeState() {
		return negativeStages > 0;
	}

	public int getEffectiveStage() {
		return positiveStages - negativeStages;
	}

	public int getPositiveStages() {
		return positiveStages;
	}

	public int getPositiveRemainingTurns() {
		return positiveRemainingTurns;
	}

	public int getNegativeStages() {
		return negativeStages;
	}

	public int getNegativeRemainingTurns() {
		return negativeRemainingTurns;
	}

	private void validateStageChange(int stageChange) {
		if (stageChange == 0 || stageChange < -AttributeStageRules.MAXIMUM_STAGE_MAGNITUDE
				|| stageChange > AttributeStageRules.MAXIMUM_STAGE_MAGNITUDE) {
			throw new IllegalArgumentException("Attribute stage change must be between -3 and -1 or between 1 and 3.");
		}
	}
}