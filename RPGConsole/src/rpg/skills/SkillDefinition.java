package rpg.skills;

import java.util.List;
import java.util.Objects;

// Stores the complete immutable definition of a playable skill.
public final class SkillDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private final String id;
	private final String classId;
	private final SkillAcquisitionType acquisitionType;
	private final int levelRequirement;
	private final int mpCost;
	private final int spCost;
	private final String name;
	private final String description;
	private final List<SkillEffectDefinition> effects;

	public SkillDefinition(
			String id,
			String classId,
			SkillAcquisitionType acquisitionType,
			int levelRequirement,
			int mpCost,
			int spCost,
			String name,
			String description,
			List<SkillEffectDefinition> effects
	) {
		this.id = validateId(id, "Skill");
		this.classId = validateId(classId, "Playable class");
		this.acquisitionType = Objects.requireNonNull(acquisitionType, "Skill acquisition type cannot be null.");

		if (levelRequirement < 1) {
			throw new IllegalArgumentException("Skill level requirement must be positive: " + this.id);
		}

		if (mpCost < 0 || spCost < 0) {
			throw new IllegalArgumentException("Skill resource costs cannot be negative: " + this.id);
		}

		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Skill name cannot be empty: " + this.id);
		}

		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("Skill description cannot be empty: " + this.id);
		}

		Objects.requireNonNull(effects, "Skill effects cannot be null.");

		if (effects.isEmpty()) {
			throw new IllegalArgumentException("Skill must contain at least one effect: " + this.id);
		}

		this.effects = List.copyOf(effects);

		validateEffectOrder();
		validateEffectiveComponents();
		validateDamagePowerModifiers();
		validateRecoilDamageEffects();

		this.levelRequirement = levelRequirement;
		this.mpCost = mpCost;
		this.spCost = spCost;
		this.name = name.trim();
		this.description = description.trim();
	}

	private String validateId(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
		}

		String trimmedValue = value.trim();

		if (!trimmedValue.matches(ID_PATTERN)) {
			throw new IllegalArgumentException(
					fieldName + " ID must use lowercase letters, " + "numbers and single underscores: " + trimmedValue
			);
		}

		return trimmedValue;
	}

	private void validateEffectOrder() {
		int previousSequence = 0;
		SkillExecutionPhase previousPhase = null;

		for (SkillEffectDefinition effect : effects) {
			if (effect.getSequence() <= previousSequence) {
				throw new IllegalArgumentException(
						"Skill effect sequences must be positive, " + "unique and declared in ascending order: " + id
				);
			}

			if (previousPhase != null && getPhaseOrder(effect.getPhase()) < getPhaseOrder(previousPhase)) {
				throw new IllegalArgumentException("Skill effect phases cannot move backwards: " + id);
			}

			previousSequence = effect.getSequence();
			previousPhase = effect.getPhase();
		}
	}

	private int getPhaseOrder(SkillExecutionPhase phase) {
		return switch (phase) {
		case PRE_ACTION -> 0;
		case PRIMARY -> 1;
		case POST_ACTION -> 2;
		};
	}

	private void validateEffectiveComponents() {
		boolean hasEffectiveComponent = effects.stream()
				.anyMatch(effect -> effect.getEffectType() != SkillEffectType.DAMAGE_POWER_MODIFIER);

		if (!hasEffectiveComponent) {
			throw new IllegalArgumentException(
					"Skill must contain an effect capable of " + "affecting a target: " + id
			);
		}
	}

	private void validateDamagePowerModifiers() {
		for (int index = 0; index < effects.size(); index++) {
			SkillEffectDefinition effect = effects.get(index);

			if (effect.getEffectType() != SkillEffectType.DAMAGE_POWER_MODIFIER) {
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
						"Damage-power modifier must precede " + "a damage effect with the same target: " + id
				);
			}
		}
	}

	private void validateRecoilDamageEffects() {
		for (SkillEffectDefinition effect : effects) {
			if (effect.getEffectType() == SkillEffectType.RECOIL_DAMAGE) {

				getRecoilSourceEffect(effect);
			}
		}
	}

	public String getId() {
		return id;
	}

	public String getClassId() {
		return classId;
	}

	public SkillAcquisitionType getAcquisitionType() {
		return acquisitionType;
	}

	public int getLevelRequirement() {
		return levelRequirement;
	}

	public boolean isStarterSkill() {
		return acquisitionType == SkillAcquisitionType.STARTER;
	}

	public boolean isLevelSkill() {
		return acquisitionType == SkillAcquisitionType.LEVEL;
	}

	public boolean isScrollSkill() {
		return acquisitionType == SkillAcquisitionType.SCROLL;
	}

	public int getMpCost() {
		return mpCost;
	}

	public int getSpCost() {
		return spCost;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<SkillEffectDefinition> getEffects() {
		return effects;
	}

	public List<SkillEffectDefinition> getEffectsByPhase(SkillExecutionPhase phase) {
		Objects.requireNonNull(phase, "Skill execution phase cannot be null.");

		return effects.stream().filter(effect -> effect.getPhase() == phase).toList();
	}

	public SkillEffectDefinition getRecoilSourceEffect(SkillEffectDefinition recoilEffect) {
		Objects.requireNonNull(recoilEffect, "Recoil effect cannot be null.");

		if (recoilEffect.getEffectType() != SkillEffectType.RECOIL_DAMAGE) {

			throw new IllegalArgumentException("Effect must be RECOIL_DAMAGE: " + id);
		}

		int recoilIndex = effects.indexOf(recoilEffect);

		if (recoilIndex < 1) {
			throw new IllegalArgumentException(
					"Recoil damage must immediately follow " + "its source damage effect: " + id
			);
		}

		SkillEffectDefinition sourceEffect = effects.get(recoilIndex - 1);

		boolean isCompatibleSource = sourceEffect.getEffectType() == SkillEffectType.DAMAGE
				&& sourceEffect.getPhase() == recoilEffect.getPhase()
				&& sourceEffect.getTargetType() == SkillTargetType.SINGLE_ENEMY;

		if (!isCompatibleSource) {
			throw new IllegalArgumentException(
					"Recoil damage must immediately follow " + "a single-enemy damage effect " + "in the same phase: "
							+ id
			);
		}

		return sourceEffect;
	}
}