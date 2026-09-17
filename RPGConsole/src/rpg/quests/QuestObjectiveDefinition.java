package rpg.quests;

import java.util.Optional;

// Stores the immutable data of one quest objective.
public final class QuestObjectiveDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private final String id;
	private final int order;
	private final QuestObjectiveType objectiveType;
	private final Optional<String> targetId;
	private final int requiredQuantity;
	private final String text;

	public QuestObjectiveDefinition(
			String id,
			int order,
			QuestObjectiveType objectiveType,
			Optional<String> targetId,
			int requiredQuantity,
			String text) {
		this.id = validateId(id, "Quest objective");

		if (order <= 0) {
			throw new IllegalArgumentException(
					"Quest objective order must be positive."
			);
		}

		if (objectiveType == null) {
			throw new IllegalArgumentException(
					"Quest objective type cannot be null."
			);
		}

		if (targetId == null) {
			throw new IllegalArgumentException(
					"Quest objective target cannot be null."
			);
		}

		this.order = order;
		this.objectiveType = objectiveType;
		this.targetId = validateTarget(objectiveType, targetId);
		this.requiredQuantity =
				validateRequiredQuantity(objectiveType, requiredQuantity);
		this.text = validateText(text);
	}

	private static Optional<String> validateTarget(
			QuestObjectiveType objectiveType,
			Optional<String> targetId) {
		if (objectiveType == QuestObjectiveType.WIN_BATTLE) {
			if (targetId.isPresent()) {
				throw new IllegalArgumentException(
						"WIN_BATTLE objective cannot define a target ID."
				);
			}

			return Optional.empty();
		}

		if (targetId.isEmpty()) {
			throw new IllegalArgumentException(
					objectiveType
							+ " objective must define a target ID."
			);
		}

		return Optional.of(
				validateId(targetId.orElseThrow(), "Quest objective target")
		);
	}

	private static int validateRequiredQuantity(
			QuestObjectiveType objectiveType,
			int requiredQuantity) {
		switch (objectiveType) {
			case TALK_TO_NPC:
			case ANALYZE_ENEMY:
			case REACH_SCENARIO:
			case COMPLETE_DUNGEON:
				if (requiredQuantity != 1) {
					throw new IllegalArgumentException(
							objectiveType
									+ " objective must require exactly one completion."
					);
				}

				break;

			case DEFEAT_ENEMY:
			case WIN_BATTLE:
			case OBTAIN_ITEM:
			case DELIVER_ITEM:
				if (requiredQuantity <= 0) {
					throw new IllegalArgumentException(
							objectiveType
									+ " objective quantity must be positive."
					);
				}

				break;
		}

		return requiredQuantity;
	}

	private static String validateId(
			String value,
			String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(
					fieldName + " ID cannot be empty."
			);
		}

		String trimmedValue = value.trim();

		if (!trimmedValue.matches(ID_PATTERN)) {
			throw new IllegalArgumentException(
					fieldName
							+ " ID must use lowercase letters, numbers and single underscores: "
							+ trimmedValue
			);
		}

		return trimmedValue;
	}

	private static String validateText(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(
					"Quest objective text cannot be empty."
			);
		}

		return value.trim();
	}

	public String getId() {
		return id;
	}

	public int getOrder() {
		return order;
	}

	public QuestObjectiveType getObjectiveType() {
		return objectiveType;
	}

	public Optional<String> getTargetId() {
		return targetId;
	}

	public int getRequiredQuantity() {
		return requiredQuantity;
	}

	public String getText() {
		return text;
	}
}