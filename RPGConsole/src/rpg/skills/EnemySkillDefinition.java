package rpg.skills;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

// Stores the complete immutable definition of an enemy skill.
public final class EnemySkillDefinition implements CombatSkillDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private final String id;
	private final int mpCost;
	private final int spCost;
	private final String name;
	private final List<SkillEffectDefinition> effects;

	private static final EnumSet<SkillEffectType> SUPPORTED_EFFECT_TYPES = EnumSet.of(
			SkillEffectType.DAMAGE,
			SkillEffectType.ATTRIBUTE_STAGE,
			SkillEffectType.HP_RECOVERY,
			SkillEffectType.MP_RECOVERY,
			SkillEffectType.SP_RECOVERY,
			SkillEffectType.PURIFICATION,
			SkillEffectType.DISSIPATION
	);

	public EnemySkillDefinition(String id, int mpCost, int spCost, String name, List<SkillEffectDefinition> effects) {
		this.id = validateId(id);

		if (mpCost < 0 || spCost < 0) {
			throw new IllegalArgumentException("Enemy skill resource costs cannot be negative: " + this.id);
		}

		if (mpCost == 0 && spCost == 0) {
			throw new IllegalArgumentException("Enemy skill must consume MP, SP or both: " + this.id);
		}

		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Enemy skill name cannot be empty: " + this.id);
		}

		this.effects = List.copyOf(Objects.requireNonNull(effects, "Enemy skill effects cannot be null."));

		CombatSkillDefinition.validateEffects(this.id, this.effects);

		validateSupportedEffects();

		this.mpCost = mpCost;
		this.spCost = spCost;
		this.name = name.trim();
	}

	private void validateSupportedEffects() {
		for (SkillEffectDefinition effect : effects) {
			if (!SUPPORTED_EFFECT_TYPES.contains(effect.getEffectType())) {
				throw new IllegalArgumentException(
						"Enemy skill effect type is not supported: " + id + " -> " + effect.getEffectType()
				);
			}
		}
	}

	private static String validateId(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Enemy skill ID cannot be empty.");
		}

		String trimmedValue = value.trim();

		if (!trimmedValue.matches(ID_PATTERN)) {
			throw new IllegalArgumentException(
					"Enemy skill ID must use lowercase letters, numbers and single underscores: " + trimmedValue
			);
		}

		return trimmedValue;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getMpCost() {
		return mpCost;
	}

	@Override
	public int getSpCost() {
		return spCost;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<SkillEffectDefinition> getEffects() {
		return effects;
	}
}