package rpg.quests;

import java.util.List;
import java.util.Optional;

// Stores the immutable data of one quest and its sequential steps.
public final class QuestDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private final String id;
	private final String title;
	private final String description;
	private final String startingNpcId;
	private final Optional<String> availabilityRequirementSetId;
	private final List<QuestStepDefinition> steps;

	public QuestDefinition(
			String id,
			String title,
			String description,
			String startingNpcId,
			Optional<String> availabilityRequirementSetId,
			List<QuestStepDefinition> steps
	) {
		this.id = validateId(id, "Quest");
		this.title = validateTitle(title);
		this.description = validateText(description, "Quest description");
		this.startingNpcId = validateId(startingNpcId, "Starting NPC");

		if (availabilityRequirementSetId == null) {
			throw new IllegalArgumentException("Quest availability requirement set cannot be null.");
		}

		this.availabilityRequirementSetId = availabilityRequirementSetId
				.map(value -> validateId(value, "Availability requirement set"));

		validateSteps(steps);
		this.steps = List.copyOf(steps);
	}

	private static void validateSteps(List<QuestStepDefinition> steps) {
		if (steps == null || steps.isEmpty()) {
			throw new IllegalArgumentException("Quest must contain at least one step.");
		}

		for (int index = 0; index < steps.size(); index++) {
			QuestStepDefinition stepDefinition = steps.get(index);

			if (stepDefinition == null) {
				throw new IllegalArgumentException("Quest steps cannot contain null entries.");
			}

			int expectedStep = index + 1;

			if (stepDefinition.getStep() != expectedStep) {
				throw new IllegalArgumentException(
						"Quest steps must start at 1 and be consecutive. Expected step " + expectedStep + " but found "
								+ stepDefinition.getStep() + "."
				);
			}
		}
	}

	private static String validateId(String value, String fieldName) {
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

	private static String validateTitle(String value) {
		String validatedTitle = validateText(value, "Quest title");

		if (validatedTitle.contains("{") || validatedTitle.contains("}")) {
			throw new IllegalArgumentException("Quest title cannot contain placeholders.");
		}

		return validatedTitle;
	}

	private static String validateText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " cannot be empty.");
		}

		return value.trim();
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getStartingNpcId() {
		return startingNpcId;
	}

	public Optional<String> getAvailabilityRequirementSetId() {
		return availabilityRequirementSetId;
	}

	public List<QuestStepDefinition> getSteps() {
		return steps;
	}
}
