package rpg.world.crafting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import rpg.items.ItemDefinitionCatalog;

// Loads crafting recipe definitions from external data.
public final class CraftingRecipeLoader {

	private static final int FIXED_FIELD_COUNT = 4;
	private static final int MINIMUM_FIELD_COUNT = 6;

	public CraftingRecipeCatalog load(
			Path filePath,
			ItemDefinitionCatalog itemDefinitionCatalog) throws IOException {
		if (itemDefinitionCatalog == null) {
			throw new IllegalArgumentException(
					"Item definition catalog cannot be null."
			);
		}

		CraftingRecipeCatalog catalog = new CraftingRecipeCatalog();
		List<String> lines = Files.readAllLines(filePath);

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index).trim();

			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			try {
				CraftingRecipeDefinition recipeDefinition =
						parseDefinition(line, itemDefinitionCatalog);

				catalog.register(recipeDefinition);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid crafting recipe definition at line "
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

	private CraftingRecipeDefinition parseDefinition(
			String line,
			ItemDefinitionCatalog itemDefinitionCatalog) {
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

		if ((fields.length - FIXED_FIELD_COUNT) % 2 != 0) {
			throw new IllegalArgumentException(
					"Crafting materials must use item ID and quantity pairs."
			);
		}

		String id = fields[0].trim();
		String outputItemId = fields[1].trim();
		String name = fields[2].trim();
		String description = fields[3].trim().replace("\\n", "\n");

		itemDefinitionCatalog.getById(outputItemId);

		List<CraftingMaterial> materials = new ArrayList<>();

		for (int index = FIXED_FIELD_COUNT; index < fields.length; index += 2) {
			String materialItemId = fields[index].trim();
			int requiredQuantity = parseInteger(
					fields[index + 1],
					"crafting material quantity"
			);

			itemDefinitionCatalog.getById(materialItemId);

			materials.add(
					new CraftingMaterial(materialItemId, requiredQuantity)
			);
		}

		return new CraftingRecipeDefinition(
				id,
				outputItemId,
				name,
				description,
				materials
		);
	}

	private int parseInteger(String value, String fieldName) {
		String normalizedValue = value.trim();

		try {
			return Integer.parseInt(normalizedValue);
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Invalid " + fieldName + ": " + normalizedValue,
					exception
			);
		}
	}
}