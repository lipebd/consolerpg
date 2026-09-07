package rpg.items;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

// Loads common item definitions from external data.
public final class ItemLoader {

	private static final int EXPECTED_FIELD_COUNT = 7;

	public ItemDefinitionCatalog load(Path filePath) throws IOException {
		ItemDefinitionCatalog catalog = new ItemDefinitionCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index).trim();

			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			try {
				ItemDefinition definition = parseDefinition(line);

				catalog.register(definition);
			} catch (IllegalArgumentException exception) {
		        throw new IllegalArgumentException(
		                        "Invalid item definition at line "
		                                        + (index + 1)
		                                        + ": " + line
		                                        + ". " + exception.getMessage(),
		                        exception);
		}
		}

		return catalog;
	}

	private ItemDefinition parseDefinition(String line) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Expected " + EXPECTED_FIELD_COUNT + " fields but found " + fields.length + ".");
		}

		String id = fields[0].trim();

		ItemCategory category = parseCategory(fields[1]);

		boolean playerTradable = parseBoolean(fields[2], "player tradable");

		Integer baseShopSalePrice = parseOptionalInteger(fields[3], "base shop sale price");

		Integer shopPurchasePrice = parseOptionalInteger(fields[4], "shop purchase price");

		String name = fields[5].trim();

		String description = fields[6].trim().replace("\\n", "\n");

		return new ItemDefinition(id, category, playerTradable, baseShopSalePrice, shopPurchasePrice, name, description);
	}

	private ItemCategory parseCategory(String value) {
		String normalizedValue = value.trim().toUpperCase(Locale.ROOT);

		try {
			return ItemCategory.valueOf(normalizedValue);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid item category: " + normalizedValue, exception);
		}
	}

	private Integer parseOptionalInteger(String value, String fieldName) {
		String normalizedValue = value.trim();

		if (normalizedValue.isEmpty()) {
			return null;
		}

		try {
			return Integer.parseInt(normalizedValue);
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Invalid " + fieldName + ": " + normalizedValue, exception);
		}
	}

	private boolean parseBoolean(String value, String fieldName) {
		String normalizedValue = value.trim();

		if (normalizedValue.equalsIgnoreCase("true")) {
			return true;
		}

		if (normalizedValue.equalsIgnoreCase("false")) {
			return false;
		}

		throw new IllegalArgumentException("Invalid " + fieldName + ": " + normalizedValue);
	}
}