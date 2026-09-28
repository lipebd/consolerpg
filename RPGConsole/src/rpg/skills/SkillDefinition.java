package rpg.skills;

import java.util.List;
import java.util.Objects;

import rpg.characters.PlayableCharacterLevelRules;

// Stores the complete immutable definition of a playable skill.
public final class SkillDefinition implements CombatSkillDefinition {

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

		PlayableCharacterLevelRules.validateLevel(levelRequirement);

		if (mpCost < 0 || spCost < 0) {
			throw new IllegalArgumentException("Skill resource costs cannot be negative: " + this.id);
		}

		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Skill name cannot be empty: " + this.id);
		}

		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("Skill description cannot be empty: " + this.id);
		}

		this.effects = List.copyOf(Objects.requireNonNull(effects, "Skill effects cannot be null."));

		CombatSkillDefinition.validateEffects(this.id, this.effects);

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
					fieldName + " ID must use lowercase letters, numbers and single underscores: " + trimmedValue
			);
		}

		return trimmedValue;
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
			throw new IllegalArgumentException("Recoil damage must immediately follow its source damage effect: " + id);
		}

		SkillEffectDefinition sourceEffect = effects.get(recoilIndex - 1);

		boolean isCompatibleSource = sourceEffect.getEffectType() == SkillEffectType.DAMAGE
				&& sourceEffect.getPhase() == recoilEffect.getPhase()
				&& sourceEffect.getTargetType() == SkillTargetType.SINGLE_ENEMY;

		if (!isCompatibleSource) {
			throw new IllegalArgumentException(
					"Recoil damage must immediately follow a single-enemy damage effect in the same phase: " + id
			);
		}

		return sourceEffect;
	}
}