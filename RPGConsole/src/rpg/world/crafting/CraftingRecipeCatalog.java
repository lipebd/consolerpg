package rpg.world.crafting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to external crafting recipe definitions.
public final class CraftingRecipeCatalog {

	private final Map<String, CraftingRecipeDefinition> recipesById;

	public CraftingRecipeCatalog() {
		recipesById = new LinkedHashMap<>();
	}

	public void register(CraftingRecipeDefinition recipeDefinition) {
		if (recipeDefinition == null) {
			throw new IllegalArgumentException(
					"Crafting recipe definition cannot be null."
			);
		}

		String recipeId = recipeDefinition.getId();

		if (recipesById.containsKey(recipeId)) {
			throw new IllegalArgumentException(
					"Crafting recipe ID is already registered: " + recipeId
			);
		}

		recipesById.put(recipeId, recipeDefinition);
	}

	public CraftingRecipeDefinition getById(String recipeId) {
		if (recipeId == null || recipeId.isBlank()) {
			throw new IllegalArgumentException("Crafting recipe ID cannot be empty.");
		}

		CraftingRecipeDefinition recipeDefinition = recipesById.get(recipeId);

		if (recipeDefinition == null) {
			throw new IllegalArgumentException(
					"Crafting recipe not found: " + recipeId
			);
		}

		return recipeDefinition;
	}

	public List<CraftingRecipeDefinition> getAll() {
		return List.copyOf(recipesById.values());
	}
}