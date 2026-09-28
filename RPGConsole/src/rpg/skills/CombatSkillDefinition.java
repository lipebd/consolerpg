package rpg.skills;

import java.util.List;
import java.util.Objects;

// Defines the execution data and component rules shared by playable and enemy skills.
public interface CombatSkillDefinition {

	String getId();

	int getMpCost();

	int getSpCost();

	String getName();

	List<SkillEffectDefinition> getEffects();

	default List<SkillEffectDefinition> getEffectsByPhase(SkillExecutionPhase phase) {
		Objects.requireNonNull(phase, "Skill execution phase cannot be null.");

		return getEffects().stream().filter(effect -> effect.getPhase() == phase).toList();
	}

	default SkillEffectDefinition getRecoilSourceEffect(SkillEffectDefinition recoilEffect) {
		Objects.requireNonNull(recoilEffect, "Recoil effect cannot be null.");

		return findRecoilSourceEffect(getId(), getEffects(), recoilEffect);
	}

	static void validateEffects(String skillId, List<SkillEffectDefinition> effects) {
		Objects.requireNonNull(skillId, "Skill ID cannot be null.");
		Objects.requireNonNull(effects, "Skill effects cannot be null.");

		if (effects.isEmpty()) {
			throw new IllegalArgumentException("Skill must contain at least one effect: " + skillId);
		}

		validateEffectOrder(skillId, effects);
		validateEffectiveComponents(skillId, effects);
		validateDamagePowerModifiers(skillId, effects);
		validateRecoilDamageEffects(skillId, effects);
	}

	private static void validateEffectOrder(String skillId, List<SkillEffectDefinition> effects) {
		int previousSequence = 0;
		SkillExecutionPhase previousPhase = null;

		for (SkillEffectDefinition effect : effects) {
			if (effect.getSequence() <= previousSequence) {
				throw new IllegalArgumentException(
						"Skill effect sequences must be positive, unique and declared in ascending order: " + skillId
				);
			}

			if (previousPhase != null && getPhaseOrder(effect.getPhase()) < getPhaseOrder(previousPhase)) {
				throw new IllegalArgumentException("Skill effect phases cannot move backwards: " + skillId);
			}

			previousSequence = effect.getSequence();
			previousPhase = effect.getPhase();
		}
	}

	private static int getPhaseOrder(SkillExecutionPhase phase) {
		return switch (phase) {
		case PRE_ACTION -> 0;
		case PRIMARY -> 1;
		case POST_ACTION -> 2;
		};
	}

	private static void validateEffectiveComponents(String skillId, List<SkillEffectDefinition> effects) {
		boolean hasEffectiveComponent = effects.stream()
				.anyMatch(effect -> effect.getEffectType() != SkillEffectType.DAMAGE_POWER_MODIFIER);

		if (!hasEffectiveComponent) {
			throw new IllegalArgumentException(
					"Skill must contain an effect capable of affecting a target: " + skillId
			);
		}
	}

	private static void validateDamagePowerModifiers(String skillId, List<SkillEffectDefinition> effects) {
		for (int index = 0; index < effects.size(); index++) {
			SkillEffectDefinition effect = effects.get(index);

			if (effect.getEffectType() != SkillEffectType.DAMAGE_POWER_MODIFIER) {
				continue;
			}

			if (effect.getPhase() == SkillExecutionPhase.PRIMARY) {
				if (index + 1 >= effects.size()) {
					throw new IllegalArgumentException(
							"A PRIMARY damage-power modifier must immediately precede damage with the same target type: "
									+ skillId
					);
				}

				SkillEffectDefinition nextEffect = effects.get(index + 1);

				if (nextEffect.getPhase() != SkillExecutionPhase.PRIMARY
						|| nextEffect.getEffectType() != SkillEffectType.DAMAGE
						|| nextEffect.getTargetType() != effect.getTargetType()) {
					throw new IllegalArgumentException(
							"A PRIMARY damage-power modifier must immediately precede damage with the same target type: "
									+ skillId
					);
				}

				continue;
			}

			boolean hasCompatibleLaterDamage = false;

			for (int laterIndex = index + 1; laterIndex < effects.size(); laterIndex++) {
				SkillEffectDefinition laterEffect = effects.get(laterIndex);

				if (laterEffect.getEffectType() == SkillEffectType.DAMAGE
						&& laterEffect.getTargetType() == effect.getTargetType()) {
					hasCompatibleLaterDamage = true;
					break;
				}
			}

			if (!hasCompatibleLaterDamage) {
				throw new IllegalArgumentException(
						"Damage-power modifier must precede a damage effect with the same target: " + skillId
				);
			}
		}
	}

	private static void validateRecoilDamageEffects(String skillId, List<SkillEffectDefinition> effects) {
		for (SkillEffectDefinition effect : effects) {
			if (effect.getEffectType() == SkillEffectType.RECOIL_DAMAGE) {
				findRecoilSourceEffect(skillId, effects, effect);
			}
		}
	}

	private static SkillEffectDefinition findRecoilSourceEffect(
			String skillId,
			List<SkillEffectDefinition> effects,
			SkillEffectDefinition recoilEffect
	) {
		if (recoilEffect.getEffectType() != SkillEffectType.RECOIL_DAMAGE) {
			throw new IllegalArgumentException("Effect must be RECOIL_DAMAGE: " + skillId);
		}

		int recoilIndex = effects.indexOf(recoilEffect);

		if (recoilIndex < 1) {
			throw new IllegalArgumentException(
					"Recoil damage must immediately follow its source damage effect: " + skillId
			);
		}

		SkillEffectDefinition sourceEffect = effects.get(recoilIndex - 1);

		boolean isCompatibleSource = sourceEffect.getEffectType() == SkillEffectType.DAMAGE
				&& sourceEffect.getPhase() == recoilEffect.getPhase()
				&& sourceEffect.getTargetType() == SkillTargetType.SINGLE_ENEMY;

		if (!isCompatibleSource) {
			throw new IllegalArgumentException(
					"Recoil damage must immediately follow a single-enemy damage effect in the same phase: " + skillId
			);
		}

		return sourceEffect;
	}
}