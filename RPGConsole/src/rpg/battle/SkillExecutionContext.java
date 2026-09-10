package rpg.battle;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rpg.characters.GameCharacter;
import rpg.characters.PlayableCharacter;
import rpg.skills.SkillTargetType;

// Stores the acting character and the targets selected for one skill use.
public final class SkillExecutionContext {

	private final PlayableCharacter actingCharacter;
	private final Map<SkillTargetType, List<GameCharacter>> targetsByType;
	private final int weaponBaseDamage;

	public SkillExecutionContext(
			PlayableCharacter actingCharacter,
			Map<SkillTargetType, List<GameCharacter>> targetsByType,
			int weaponBaseDamage
	) {
		this.actingCharacter = Objects.requireNonNull(actingCharacter, "Acting character cannot be null.");

		Objects.requireNonNull(targetsByType, "Skill targets cannot be null.");

		if (weaponBaseDamage < 0) {
			throw new IllegalArgumentException("Weapon base damage cannot be negative.");
		}

		EnumMap<SkillTargetType, List<GameCharacter>> copiedTargets = new EnumMap<>(SkillTargetType.class);

		for (Map.Entry<SkillTargetType, List<GameCharacter>> entry : targetsByType.entrySet()) {
			SkillTargetType targetType = Objects.requireNonNull(entry.getKey(), "Skill target type cannot be null.");

			List<GameCharacter> targets = Objects.requireNonNull(entry.getValue(), "Skill target list cannot be null.");

			for (GameCharacter target : targets) {
				Objects.requireNonNull(target, "Skill target cannot be null.");
			}

			copiedTargets.put(targetType, List.copyOf(targets));
		}

		List<GameCharacter> suppliedSelfTargets = copiedTargets.get(SkillTargetType.SELF);

		if (suppliedSelfTargets != null
				&& (suppliedSelfTargets.size() != 1 || suppliedSelfTargets.get(0) != actingCharacter)) {
			throw new IllegalArgumentException("SELF must target only the acting character.");
		}

		copiedTargets.put(SkillTargetType.SELF, List.of(actingCharacter));

		this.targetsByType = Collections.unmodifiableMap(copiedTargets);

		this.weaponBaseDamage = weaponBaseDamage;
	}

	public PlayableCharacter getActingCharacter() {
		return actingCharacter;
	}

	public List<GameCharacter> getTargets(SkillTargetType targetType) {
		Objects.requireNonNull(targetType, "Skill target type cannot be null.");

		return targetsByType.getOrDefault(targetType, List.of());
	}

	public int getWeaponBaseDamage() {
		return weaponBaseDamage;
	}
}