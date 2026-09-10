package rpg.battle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import rpg.characters.CharacterAttribute;
import rpg.characters.GameCharacter;

// Calculates recovery scaled by one effective character attribute.
public final class RecoveryCalculator {

	private static final int NORMALIZED_ATTRIBUTE_VALUE = 10;
	private static final int PERCENTAGE_DIVISOR = 100;

	public int calculateRecovery(GameCharacter applyingCharacter, CharacterAttribute scalingAttribute, int power) {
		Objects.requireNonNull(applyingCharacter, "Applying character cannot be null.");

		Objects.requireNonNull(scalingAttribute, "Recovery scaling attribute cannot be null.");

		if (power < 1) {
			throw new IllegalArgumentException("Recovery power must be positive.");
		}

		int effectiveAttribute = getEffectiveAttribute(applyingCharacter, scalingAttribute);

		long percentageValue = (long) effectiveAttribute - NORMALIZED_ATTRIBUTE_VALUE + PERCENTAGE_DIVISOR;

		BigDecimal calculatedRecovery = BigDecimal.valueOf(power).multiply(BigDecimal.valueOf(percentageValue))
				.divide(BigDecimal.valueOf(PERCENTAGE_DIVISOR));

		int roundedRecovery = calculatedRecovery.setScale(0, RoundingMode.HALF_UP).intValueExact();

		return Math.max(1, roundedRecovery);
	}

	private int getEffectiveAttribute(GameCharacter applyingCharacter, CharacterAttribute scalingAttribute) {
		return switch (scalingAttribute) {
		case ATK -> applyingCharacter.getAttack();
		case MAG -> applyingCharacter.getMagic();

		default -> throw new IllegalArgumentException("Recovery can scale only with ATK or MAG.");
		};
	}
}