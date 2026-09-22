package rpg.effects;

// Defines the internal balance rules for temporary attribute stages.
public final class AttributeStageRules {

	public static final int MAXIMUM_STAGE_MAGNITUDE = 3;

	private AttributeStageRules() {
	}

	public static int applyMultiplier(int structuralValue, int effectiveStage) {
		if (structuralValue < 0) {
			throw new IllegalArgumentException("Structural attribute value cannot be negative.");
		}

		int multiplierPercentage = getMultiplierPercentage(effectiveStage);

		long roundedValue = ((long) structuralValue * multiplierPercentage + 50L) / 100L;

		if (roundedValue > Integer.MAX_VALUE) {
			throw new ArithmeticException("Effective attribute value exceeds the supported integer range.");
		}

		return (int) roundedValue;
	}

	public static int getMultiplierPercentage(int effectiveStage) {
		return switch (effectiveStage) {
		case 3 -> 160;
		case 2 -> 140;
		case 1 -> 120;
		case 0 -> 100;
		case -1 -> 80;
		case -2 -> 60;
		case -3 -> 40;

		default -> throw new IllegalArgumentException("Effective attribute stage must be between -3 and 3.");
		};
	}
}