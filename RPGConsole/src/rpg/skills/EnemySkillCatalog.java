package rpg.skills;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Stores and provides access to all external enemy skill definitions.
public final class EnemySkillCatalog {

	private final Map<String, EnemySkillDefinition> skillsById;

	public EnemySkillCatalog() {
		skillsById = new LinkedHashMap<>();
	}

	public void register(EnemySkillDefinition skillDefinition) {
		Objects.requireNonNull(skillDefinition, "Enemy skill definition cannot be null.");

		String skillId = skillDefinition.getId();

		if (skillsById.containsKey(skillId)) {
			throw new IllegalArgumentException("Enemy skill definition ID is already registered: " + skillId);
		}

		skillsById.put(skillId, skillDefinition);
	}

	public EnemySkillDefinition getById(String id) {
		EnemySkillDefinition skillDefinition = skillsById.get(id);

		if (skillDefinition == null) {
			throw new IllegalArgumentException("Enemy skill not found: " + id);
		}

		return skillDefinition;
	}

	public List<EnemySkillDefinition> getAll() {
		return List.copyOf(skillsById.values());
	}
}