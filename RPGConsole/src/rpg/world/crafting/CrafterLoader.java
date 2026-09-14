package rpg.world.crafting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import rpg.world.scenarios.ScenarioDefinition;
import rpg.world.scenarios.ScenarioDefinitionCatalog;
import rpg.world.scenarios.ScenarioType;

// Loads crafter definitions from external data.
public final class CrafterLoader {

	private static final int FIXED_FIELD_COUNT = 4;
	private static final int MINIMUM_FIELD_COUNT = 5;

	public CrafterCatalog load(
			Path filePath,
			ScenarioDefinitionCatalog scenarioDefinitionCatalog,
			CraftingRecipeCatalog craftingRecipeCatalog) throws IOException {
		if (scenarioDefinitionCatalog == null) {
			throw new IllegalArgumentException(
					"Scenario definition catalog cannot be null."
			);
		}

		if (craftingRecipeCatalog == null) {
			throw new IllegalArgumentException(
					"Crafting recipe catalog cannot be null."
			);
		}

		CrafterCatalog catalog = new CrafterCatalog();
		List<String> lines = Files.readAllLines(filePath);

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index).trim();

			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			try {
				CrafterDefinition crafterDefinition = parseDefinition(
						line,
						scenarioDefinitionCatalog,
						craftingRecipeCatalog
				);

				catalog.register(crafterDefinition);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid crafter definition at line "
								+ (index + 1)
								+ ": "
								+ line
								+ ". "
								+ exception.getMessage(),
						exception
				);
			}
		}

		return catalog;
	}

	private CrafterDefinition parseDefinition(
			String line,
			ScenarioDefinitionCatalog scenarioDefinitionCatalog,
			CraftingRecipeCatalog craftingRecipeCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length < MINIMUM_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Expected at least "
							+ MINIMUM_FIELD_COUNT
							+ " fields but found "
							+ fields.length
							+ "."
			);
		}

		String id = fields[0].trim();
		String scenarioId = fields[1].trim();
		String name = fields[2].trim();
		String description = fields[3].trim().replace("\\n", "\n");

		ScenarioDefinition scenarioDefinition =
				scenarioDefinitionCatalog.getById(scenarioId);

		if (scenarioDefinition.getType() != ScenarioType.CITY) {
			throw new IllegalArgumentException(
					"Crafter scenario must reference a city: " + scenarioId
			);
		}

		List<String> recipeIds = new ArrayList<>();

		for (int index = FIXED_FIELD_COUNT; index < fields.length; index++) {
			String recipeId = fields[index].trim();

			craftingRecipeCatalog.getById(recipeId);
			recipeIds.add(recipeId);
		}

		return new CrafterDefinition(
				id,
				scenarioId,
				name,
				description,
				recipeIds
		);
	}
}