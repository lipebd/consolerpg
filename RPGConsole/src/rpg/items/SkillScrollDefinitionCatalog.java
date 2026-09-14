package rpg.items;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores skill-scroll definitions by item and skill IDs.
public final class SkillScrollDefinitionCatalog {

	private final Map<String, SkillScrollDefinition> definitionsByItemId;
	private final Map<String, SkillScrollDefinition> definitionsBySkillId;

	public SkillScrollDefinitionCatalog() {
		definitionsByItemId = new LinkedHashMap<>();
		definitionsBySkillId = new LinkedHashMap<>();
	}

	public void register(SkillScrollDefinition definition) {
		if (definition == null) {
			throw new IllegalArgumentException("Skill-scroll definition cannot be null.");
		}

		String itemId = definition.getItemId();
		String skillId = definition.getSkillId();

		if (definitionsByItemId.containsKey(itemId)) {
			throw new IllegalArgumentException("Skill-scroll item definition is already registered: " + itemId);
		}

		if (definitionsBySkillId.containsKey(skillId)) {
			throw new IllegalArgumentException("Skill-scroll skill definition is already registered: " + skillId);
		}

		definitionsByItemId.put(itemId, definition);
		definitionsBySkillId.put(skillId, definition);
	}

	public SkillScrollDefinition getByItemId(String itemId) {
		validateId(itemId, "Skill-scroll item");

		SkillScrollDefinition definition = definitionsByItemId.get(itemId);

		if (definition == null) {
			throw new IllegalArgumentException("Skill-scroll definition not found for item: " + itemId);
		}

		return definition;
	}

	public SkillScrollDefinition getBySkillId(String skillId) {
		validateId(skillId, "Skill");

		SkillScrollDefinition definition = definitionsBySkillId.get(skillId);

		if (definition == null) {
			throw new IllegalArgumentException("Skill-scroll definition not found for skill: " + skillId);
		}

		return definition;
	}

	public boolean containsItemId(String itemId) {
		return definitionsByItemId.containsKey(itemId);
	}

	public boolean containsSkillId(String skillId) {
		return definitionsBySkillId.containsKey(skillId);
	}

	public List<SkillScrollDefinition> getAll() {
		return List.copyOf(definitionsByItemId.values());
	}

	private void validateId(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
		}
	}
}