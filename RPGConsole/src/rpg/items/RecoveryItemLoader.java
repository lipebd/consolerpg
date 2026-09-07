package rpg.items;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

// Loads recovery item definitions from external data.
public final class RecoveryItemLoader {

	private static final int EXPECTED_FIELD_COUNT = 7;

	public RecoveryItemDefinitionCatalog load(Path filePath, ItemDefinitionCatalog itemDefinitionCatalog)
			throws IOException {
		RecoveryItemDefinitionCatalog catalog = new RecoveryItemDefinitionCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index).trim();

			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			try {
				RecoveryItemDefinition definition = parseDefinition(line, itemDefinitionCatalog);

				catalog.register(definition);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("Invalid recovery item definition at line " + (index + 1) + ": "
						+ line + ". " + exception.getMessage(), exception);
			}
		}

		return catalog;
	}

	private RecoveryItemDefinition parseDefinition(String line, ItemDefinitionCatalog itemDefinitionCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Expected " + EXPECTED_FIELD_COUNT + " fields but found " + fields.length + ".");
		}

		String itemId = fields[0].trim();

		ItemDefinition itemDefinition = itemDefinitionCatalog.getById(itemId);

		if (itemDefinition.getCategory() != ItemCategory.CONSUMABLE) {
			throw new IllegalArgumentException("Recovery item must use the CONSUMABLE category: " + itemId);
		}

		RecoveryItemTargetType targetType = parseTargetType(fields[1]);

		int hpRecovery = parseInteger(fields[2], "HP recovery");

		int mpRecovery = parseInteger(fields[3], "MP recovery");

		int spRecovery = parseInteger(fields[4], "SP recovery");

		int reviveHpRecovery = parseInteger(fields[5], "revive HP recovery");

		boolean purifiesStatusEffects = parseBoolean(fields[6], "purifies status effects");

		return new RecoveryItemDefinition(itemId, targetType, hpRecovery, mpRecovery, spRecovery, reviveHpRecovery,
				purifiesStatusEffects);
	}

	private RecoveryItemTargetType parseTargetType(String value) {
		String normalizedValue = value.trim().toUpperCase(Locale.ROOT);

		try {
			return RecoveryItemTargetType.valueOf(normalizedValue);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid recovery item target type: " + normalizedValue, exception);
		}
	}

	private int parseInteger(String value, String fieldName) {
		String normalizedValue = value.trim();

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