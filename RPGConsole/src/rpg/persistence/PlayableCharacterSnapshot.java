package rpg.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// Stores one playable character in a campaign snapshot.
public final class PlayableCharacterSnapshot {

	private final String name;
	private final String classId;
	private final int level;
	private final int experience;
	private final int currentHp;
	private final int currentMp;
	private final int currentSp;
	private final EquipmentSnapshot equipment;
	private final List<String> learnedSkillIds;

	public PlayableCharacterSnapshot(
			String name,
			String classId,
			int level,
			int experience,
			int currentHp,
			int currentMp,
			int currentSp,
			EquipmentSnapshot equipment,
			List<String> learnedSkillIds
	) {
		this.name = requireNonBlank(name, "Character name");
		this.classId = requireNonBlank(classId, "Playable class ID");

		if (level <= 0) {
			throw new IllegalArgumentException("Character level must be positive.");
		}

		if (experience < 0) {
			throw new IllegalArgumentException("Character experience cannot be negative.");
		}

		if (currentHp < 0 || currentMp < 0 || currentSp < 0) {
			throw new IllegalArgumentException("Current character resources cannot be negative.");
		}

		this.level = level;
		this.experience = experience;
		this.currentHp = currentHp;
		this.currentMp = currentMp;
		this.currentSp = currentSp;
		this.equipment = Objects.requireNonNull(equipment, "Equipment snapshot cannot be null.");

		Objects.requireNonNull(learnedSkillIds, "Learned skill IDs cannot be null.");

		Set<String> uniqueSkillIds = new HashSet<>();

		for (String skillId : learnedSkillIds) {
			String validatedSkillId = requireNonBlank(skillId, "Learned skill ID");

			if (!uniqueSkillIds.add(validatedSkillId)) {
				throw new IllegalArgumentException("Learned skill ID is duplicated: " + validatedSkillId);
			}
		}

		this.learnedSkillIds = List.copyOf(learnedSkillIds);
	}

	public String getName() {
		return name;
	}

	public String getClassId() {
		return classId;
	}

	public int getLevel() {
		return level;
	}

	public int getExperience() {
		return experience;
	}

	public int getCurrentHp() {
		return currentHp;
	}

	public int getCurrentMp() {
		return currentMp;
	}

	public int getCurrentSp() {
		return currentSp;
	}

	public EquipmentSnapshot getEquipment() {
		return equipment;
	}

	public List<String> getLearnedSkillIds() {
		return learnedSkillIds;
	}

	private String requireNonBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " cannot be null or blank.");
		}

		return value;
	}
}