package rpg.world.crafting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Stores the immutable data of a crafter NPC.
public final class CrafterDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private final String id;
	private final String scenarioId;
	private final String name;
	private final String description;
	private final List<String> recipeIds;

	public CrafterDefinition(
			String id,
			String scenarioId,
			String name,
			String description,
			List<String> recipeIds) {
		this.id = validateId(id, "Crafter");
		this.scenarioId = validateId(scenarioId, "Crafter scenario");
		this.name = validateText(name, "Crafter name");
		this.description = validateText(description, "Crafter description");

		if (recipeIds == null || recipeIds.isEmpty()) {
			throw new IllegalArgumentException(
					"Crafter must provide at least one crafting recipe."
			);
		}

		List<String> validatedRecipeIds = new ArrayList<>();
		Set<String> uniqueRecipeIds = new HashSet<>();

		for (String recipeId : recipeIds) {
			String validatedRecipeId =
					validateId(recipeId, "Crafter recipe");

			if (!uniqueRecipeIds.add(validatedRecipeId)) {
				throw new IllegalArgumentException(
						"Crafter cannot repeat crafting recipe ID: "
								+ validatedRecipeId
				);
			}
			validatedRecipeIds.add(validatedRecipeId);
		}

		this.recipeIds = List.copyOf(validatedRecipeIds);
	}

	private String validateId(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
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

	private String validateText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " cannot be empty.");
		}

		return value.trim();
	}

	public String getId() {
		return id;
	}

	public String getScenarioId() {
		return scenarioId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getRecipeIds() {
		return recipeIds;
	}
}