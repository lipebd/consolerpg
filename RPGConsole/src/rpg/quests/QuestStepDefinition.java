package rpg.quests;

// Stores the immutable data of one sequential quest step.
public final class QuestStepDefinition {

	private final int step;
	private final String title;
	private final String description;

	public QuestStepDefinition(int step, String title, String description) {
		if (step <= 0) {
			throw new IllegalArgumentException("Quest step number must be positive.");
		}

		this.step = step;
		this.title = validateTitle(title);
		this.description = validateText(description, "Quest step description");
	}

	private static String validateTitle(String value) {
		String validatedTitle = validateText(value, "Quest step title");

		if (validatedTitle.contains("{") || validatedTitle.contains("}")) {
			throw new IllegalArgumentException("Quest step title cannot contain placeholders.");
		}

		return validatedTitle;
	}

	private static String validateText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " cannot be empty.");
		}

		return value.trim();
	}

	public int getStep() {
		return step;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}
}
