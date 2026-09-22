package rpg.recruitment;

import rpg.characters.PlayableCharacterLevelRules;

// Associates one quest step with one canonical recruitment candidate.
public final class QuestRecruitmentDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private final String questId;
	private final int step;
	private final String candidateId;
	private final int allyLevel;

	public QuestRecruitmentDefinition(
			String questId,
			int step,
			String candidateId,
			int allyLevel
			) {
		this.questId = validateId(questId, "Quest");
		this.step = validateStep(step);
		this.candidateId =
				validateId(
						candidateId,
						"Recruitment candidate");

		PlayableCharacterLevelRules.validateLevel(allyLevel);
		this.allyLevel = allyLevel;
	}

	private static String validateId(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
		}

		String trimmedValue = value.trim();

		if (!trimmedValue.matches(ID_PATTERN)) {
			throw new IllegalArgumentException(
					fieldName
					+ " ID must use lowercase letters, numbers and single underscores: "
					+ trimmedValue);
		}

		return trimmedValue;
	}

	private static int validateStep(int step) {
		if (step <= 0) {
			throw new IllegalArgumentException("Recruitment quest step must be positive.");
		}

		return step;
	}

	public String getQuestId() {
		return questId;
	}

	public int getStep() {
		return step;
	}

	public String getCandidateId() {
		return candidateId;
	}

	public int getAllyLevel() {
		return allyLevel;
	}
}
