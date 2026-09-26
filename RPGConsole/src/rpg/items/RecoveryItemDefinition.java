package rpg.items;

import java.util.Set;

import rpg.characters.CharacterAttribute;

// Stores the immutable specialized data of a recovery item.
public final class RecoveryItemDefinition {

	private final String itemId;
	private final RecoveryItemTargetType targetType;
	private final RecoveryAmount hpRecovery;
	private final RecoveryAmount mpRecovery;
	private final RecoveryAmount spRecovery;
	private final RecoveryAmount reviveHpRecovery;
	private final Set<CharacterAttribute> purificationAttributes;
	private final CharacterAttribute attribute;
	private final Integer stages;
	private final Integer duration;

	public RecoveryItemDefinition(
			String itemId,
			RecoveryItemTargetType targetType,
			RecoveryAmount hpRecovery,
			RecoveryAmount mpRecovery,
			RecoveryAmount spRecovery,
			RecoveryAmount reviveHpRecovery,
			Set<CharacterAttribute> purificationAttributes,
			CharacterAttribute attribute,
			Integer stages,
			Integer duration
	) {
		if (itemId == null || itemId.isBlank()) {
			throw new IllegalArgumentException("Recovery item ID cannot be empty.");
		}

		if (targetType == null) {
			throw new IllegalArgumentException("Recovery item target type cannot be null.");
		}

		if (hpRecovery == null || mpRecovery == null || spRecovery == null || reviveHpRecovery == null) {
			throw new IllegalArgumentException("Recovery item values cannot be null.");
		}
		if (purificationAttributes == null) {
			throw new IllegalArgumentException("Purification attributes cannot be null.");
		}

		if (attribute == null) {
			if (stages != null || duration != null) {
				throw new IllegalArgumentException("Stages and duration require an attribute.");
			}
		} else {
			if (stages == null || stages < 1 || stages > 3) {
				throw new IllegalArgumentException("Item stages must be between 1 and 3.");
			}

			if (attribute == CharacterAttribute.VEL) {
				if (duration != null) {
					throw new IllegalArgumentException("VEL item stages cannot define a duration.");
				}
			} else if (duration == null || duration < 1) {
				throw new IllegalArgumentException("Non-VEL item stages require a positive duration.");
			}
		}

		if (!hpRecovery.isPresent() && !mpRecovery.isPresent() && !spRecovery.isPresent()
				&& !reviveHpRecovery.isPresent() && purificationAttributes.isEmpty() && attribute == null) {
			throw new IllegalArgumentException("Recovery item must have at least one effective component.");
		}

		if (attribute != null && targetType != RecoveryItemTargetType.SINGLE_ALLY) {
		    throw new IllegalArgumentException(
		            "Attribute-stage items must target a single ally.");
		}

		this.itemId = itemId;
		this.targetType = targetType;
		this.hpRecovery = hpRecovery;
		this.mpRecovery = mpRecovery;
		this.spRecovery = spRecovery;
		this.reviveHpRecovery = reviveHpRecovery;
		this.purificationAttributes = Set.copyOf(purificationAttributes);
		this.attribute = attribute;
		this.stages = stages;
		this.duration = duration;
	}

	public String getItemId() {
		return itemId;
	}

	public RecoveryItemTargetType getTargetType() {
		return targetType;
	}

	public int getHpRecovery() {
		return hpRecovery.fixedAmount();
	}

	public int getMpRecovery() {
		return mpRecovery.fixedAmount();
	}

	public int getSpRecovery() {
		return spRecovery.fixedAmount();
	}

	public int getReviveHpRecovery() {
		return reviveHpRecovery.fixedAmount();
	}

	public int getHpRecoveryPercent() {
		return hpRecovery.percent();
	}

	public int getMpRecoveryPercent() {
		return mpRecovery.percent();
	}

	public int getSpRecoveryPercent() {
		return spRecovery.percent();
	}

	public int getReviveHpRecoveryPercent() {
		return reviveHpRecovery.percent();
	}

	public int resolveHpRecovery(int maximumHp) {
		return hpRecovery.resolve(maximumHp);
	}

	public int resolveMpRecovery(int maximumMp) {
		return mpRecovery.resolve(maximumMp);
	}

	public int resolveSpRecovery(int maximumSp) {
		return spRecovery.resolve(maximumSp);
	}

	public int resolveReviveHpRecovery(int maximumHp) {
		return reviveHpRecovery.resolve(maximumHp);
	}

	public boolean purifiesStatusEffects() {
	    return !purificationAttributes.isEmpty();
	}

	public Set<CharacterAttribute> getPurificationAttributes() {
	    return purificationAttributes;
	}

	public CharacterAttribute getAttribute() {
	    return attribute;
	}

	public Integer getStages() {
	    return stages;
	}

	public Integer getDuration() {
	    return duration;
	}

	public record RecoveryAmount(int fixedAmount, int percent) {

		public RecoveryAmount {
			if (fixedAmount < 0 || percent < 0 || percent > 100 || (fixedAmount > 0 && percent > 0)) {
				throw new IllegalArgumentException(
						"Recovery must have one non-negative fixed amount or a percentage from 1 to 100."
				);
			}
		}

		public boolean isPresent() {
			return fixedAmount > 0 || percent > 0;
		}

		public int resolve(int maximum) {
			if (maximum < 0) {
				throw new IllegalArgumentException("Maximum resource cannot be negative.");
			}

			return percent == 0 ? fixedAmount : (int) (((long) maximum * percent + 99) / 100);
		}
	}
}