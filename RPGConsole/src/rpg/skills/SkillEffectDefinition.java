package rpg.skills;

import java.util.Objects;

import rpg.characters.CharacterAttribute;
import rpg.effects.SpecialBattleStateType;

// Stores one immutable ordered component of a skill.
public final class SkillEffectDefinition {

	private static final int MINIMUM_STAGE = -3;
	private static final int MAXIMUM_STAGE = 3;

	private final int sequence;
	private final SkillExecutionPhase phase;
	private final SkillEffectType effectType;
	private final SkillTargetType targetType;
	private final DamageType damageType;
	private final Integer power;
	private final CharacterAttribute attribute;
	private final Integer stages;
	private final Integer duration;
	private final int triggerChancePercent;
	private final SpecialBattleStateType specialStateType;
	private final SkillConditionType conditionType;
	private final Integer conditionValue;

	public SkillEffectDefinition(
			int sequence,
			SkillExecutionPhase phase,
			SkillEffectType effectType,
			SkillTargetType targetType,
			DamageType damageType,
			Integer power,
			CharacterAttribute attribute,
			Integer stages,
			Integer duration,
			int triggerChancePercent,
			SpecialBattleStateType specialStateType,
			SkillConditionType conditionType,
			Integer conditionValue
	) {
		if (sequence < 1) {
			throw new IllegalArgumentException("Skill effect sequence must be positive.");
		}

		this.sequence = sequence;
		this.phase = Objects.requireNonNull(phase, "Skill execution phase cannot be null.");
		this.effectType = Objects.requireNonNull(effectType, "Skill effect type cannot be null.");
		this.targetType = Objects.requireNonNull(targetType, "Skill effect target type cannot be null.");
		if (triggerChancePercent < 1 || triggerChancePercent > 100) {
			throw new IllegalArgumentException("Skill effect trigger chance " + "must be between 1 and 100.");
		}
		this.conditionType = Objects.requireNonNull(conditionType, "Skill condition type cannot be null.");

		this.damageType = damageType;
		this.power = power;
		this.attribute = attribute;
		this.stages = stages;
		this.duration = duration;
		this.triggerChancePercent = triggerChancePercent;
		this.specialStateType = specialStateType;
		this.conditionValue = conditionValue;

		validateCondition();
		validateEffectData();
	}

	private void validateCondition() {
		if (conditionType == SkillConditionType.NONE) {
			if (conditionValue != null) {
				throw new IllegalArgumentException(
						"An unconditional skill effect cannot define " + "a condition value."
				);
			}

			return;
		}

		if (conditionValue == null || conditionValue < 1 || conditionValue > 100) {
			throw new IllegalArgumentException("Conditional HP percentage must be between " + "1 and 100.");
		}
	}

	private void validateEffectData() {
		switch (effectType) {
		case DAMAGE -> validateDamage();

		case RECOIL_DAMAGE -> validateRecoilDamage();

		case DAMAGE_POWER_MODIFIER -> validateDamagePowerModifier();

		case HP_RECOVERY -> validateHpRecovery();

		case MP_RECOVERY, SP_RECOVERY -> validateFixedResourceRecovery();

		case REVIVE -> validateRevival();

		case ATTRIBUTE_STAGE -> validateAttributeStage();

		case PURIFICATION -> validatePurification();

		case DISSIPATION -> validateDissipation();

		case SPECIAL_STATE -> validateSpecialState();
		}
	}

	private void validateRecoilDamage() {
		if (phase != SkillExecutionPhase.PRIMARY) {
			throw new IllegalArgumentException("A recoil-damage effect must use PRIMARY.");
		}

		if (targetType != SkillTargetType.SELF) {
			throw new IllegalArgumentException("A recoil-damage effect must target SELF.");
		}

		requirePositivePower();

		if (power > 100) {
			throw new IllegalArgumentException("Recoil-damage percentage cannot exceed 100.");
		}

		rejectDamageType();
		rejectAttributeData();
		rejectSpecialState();
	}

	private void validateDamage() {
		if (damageType == null) {
			throw new IllegalArgumentException("A damage effect must define a valid damage type.");
		}

		requirePositivePower();

		rejectAttributeData();
		rejectSpecialState();
	}

	private void validateDamagePowerModifier() {
		if (phase != SkillExecutionPhase.PRE_ACTION) {
			throw new IllegalArgumentException("A damage-power modifier must use PRE_ACTION.");
		}

		if (!isEnemyTarget()) {
			throw new IllegalArgumentException("A damage-power modifier must target enemies.");
		}

		requirePositivePower();

		rejectDamageType();
		rejectAttributeData();
		rejectSpecialState();
	}

	private void validateHpRecovery() {
		if (!isAllyTarget()) {
			throw new IllegalArgumentException("An HP-recovery effect must target allies.");
		}

		requirePositivePower();
		requireRecoveryScalingAttribute();

		rejectDamageType();
		rejectAttributeStageData();
		rejectSpecialState();
	}

	private void validateFixedResourceRecovery() {
		if (!isAllyTarget()) {
			throw new IllegalArgumentException("An MP/SP-recovery effect must target allies.");
		}

		requirePositivePower();

		rejectDamageType();
		rejectAttributeData();
		rejectSpecialState();
	}

	private void validateRevival() {
		if (!isAllyTarget() || targetType == SkillTargetType.SELF) {

			throw new IllegalArgumentException("A revival effect must target one or more allies.");
		}

		requirePositivePower();
		requireRecoveryScalingAttribute();

		rejectDamageType();
		rejectAttributeStageData();
		rejectSpecialState();
	}

	private void validateAttributeStage() {
		rejectDamageType();
		rejectPower();
		rejectSpecialState();

		if (attribute == null) {
			throw new IllegalArgumentException("An attribute-stage effect must define an attribute.");
		}

		if (stages == null || stages < MINIMUM_STAGE || stages > MAXIMUM_STAGE || stages == 0) {
			throw new IllegalArgumentException("Attribute stages must be between -3 and 3 " + "and cannot be zero.");
		}

		if (attribute == CharacterAttribute.VEL) {
			if (duration != null) {
				throw new IllegalArgumentException("VEL stage effects cannot define " + "a conventional duration.");
			}

			return;
		}

		if (duration == null || duration < 1) {
			throw new IllegalArgumentException(
					"A non-VEL attribute-stage effect must define " + "a positive duration."
			);
		}
	}

	private void validatePurification() {
		if (!isAllyTarget()) {
			throw new IllegalArgumentException("A purification effect must target allies.");
		}

		rejectAllOptionalEffectData();
	}

	private void validateDissipation() {
		if (!isEnemyTarget()) {
			throw new IllegalArgumentException("A dissipation effect must target enemies.");
		}

		rejectAllOptionalEffectData();
	}

	private void validateSpecialState() {
		rejectDamageType();
		rejectPower();
		rejectAttributeData();

		if (specialStateType == null) {
			throw new IllegalArgumentException("A special-state effect must define " + "a special state type.");
		}

		if (specialStateType == SpecialBattleStateType.DISTORTED_ATTRIBUTES
				&& targetType != SkillTargetType.SINGLE_ENEMY) {
			throw new IllegalArgumentException("DISTORTED_ATTRIBUTES must target " + "a single enemy.");
		}
	}

	private void requirePositivePower() {
		if (power == null || power < 1) {
			throw new IllegalArgumentException("Skill effect power must be positive.");
		}
	}

	private void rejectAllOptionalEffectData() {
		rejectDamageType();
		rejectPower();
		rejectAttributeData();
		rejectSpecialState();
	}

	private void requireRecoveryScalingAttribute() {
		if (attribute != CharacterAttribute.ATK && attribute != CharacterAttribute.MAG) {

			throw new IllegalArgumentException(effectType + " must scale with ATK or MAG.");
		}
	}

	private void rejectAttributeStageData() {
		if (stages != null || duration != null) {
			throw new IllegalArgumentException(effectType + " cannot define stages or duration.");
		}
	}

	private void rejectDamageType() {
		if (damageType != null) {
			throw new IllegalArgumentException(effectType + " cannot define a damage type.");
		}
	}

	private void rejectPower() {
		if (power != null) {
			throw new IllegalArgumentException(effectType + " cannot define power.");
		}
	}

	private void rejectAttributeData() {
		if (attribute != null || stages != null || duration != null) {
			throw new IllegalArgumentException(effectType + " cannot define attribute-stage data.");
		}
	}

	private void rejectSpecialState() {
		if (specialStateType != null) {
			throw new IllegalArgumentException(effectType + " cannot define a special state.");
		}
	}

	private boolean isAllyTarget() {
		return switch (targetType) {
		case SELF, SINGLE_ALLY, ALL_ALLIES, ALL_BUT_SELF, RANDOM_ALLY -> true;

		default -> false;
		};
	}

	private boolean isEnemyTarget() {
		return switch (targetType) {
		case SINGLE_ENEMY, ALL_ENEMIES, RANDOM_ENEMY -> true;

		default -> false;
		};
	}

	public int getSequence() {
		return sequence;
	}

	public SkillExecutionPhase getPhase() {
		return phase;
	}

	public SkillEffectType getEffectType() {
		return effectType;
	}

	public SkillTargetType getTargetType() {
		return targetType;
	}

	public DamageType getDamageType() {
		return damageType;
	}

	public Integer getPower() {
		return power;
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

	public int getTriggerChancePercent() {
		return triggerChancePercent;
	}

	public SpecialBattleStateType getSpecialStateType() {
		return specialStateType;
	}

	public SkillConditionType getConditionType() {
		return conditionType;
	}

	public Integer getConditionValue() {
		return conditionValue;
	}
}