package rpg.items;

// Stores the immutable specialized data of a skill-scroll item.
public final class SkillScrollDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private final String itemId;
	private final String skillId;
	private final String classId;

	public SkillScrollDefinition(String itemId, String skillId, String classId) {
		this.itemId = validateId(itemId, "Skill-scroll item");
		this.skillId = validateId(skillId, "Skill");
		this.classId = validateId(classId, "Playable class");
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

	public String getItemId() {
		return itemId;
	}

	public String getSkillId() {
		return skillId;
	}

	public String getClassId() {
		return classId;
	}
}