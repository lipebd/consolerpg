package rpg.skills;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Stores and provides access to all external skill definitions.
public final class SkillCatalog {

	private final Map<String, SkillDefinition> skillsById;

	public SkillCatalog() {
		skillsById = new LinkedHashMap<>();
	}

	public void register(SkillDefinition skillDefinition) {
		Objects.requireNonNull(skillDefinition, "Skill definition cannot be null.");

		String skillId = skillDefinition.getId();

		if (skillsById.containsKey(skillId)) {
			throw new IllegalArgumentException("Skill definition ID is already registered: " + skillId);
		}

		skillsById.put(skillId, skillDefinition);
	}

	public SkillDefinition getById(String id) {
		SkillDefinition skillDefinition = skillsById.get(id);

		if (skillDefinition == null) {
			throw new IllegalArgumentException("Skill not found: " + id);
		}

		return skillDefinition;
	}

	public List<SkillDefinition> getByClassId(String classId) {
		return skillsById.values().stream().filter(skill -> skill.getClassId().equals(classId)).toList();
	}

	public List<SkillDefinition> getAll() {
		return List.copyOf(skillsById.values());
	}
}