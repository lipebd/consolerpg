package rpg.items;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.EnumSet;
import java.util.Set;

import rpg.characters.CharacterAttribute;
import rpg.items.RecoveryItemDefinition.RecoveryAmount;

// Loads recovery item definitions from external data.
public final class RecoveryItemLoader {

	private static final int EXPECTED_FIELD_COUNT = 10;

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

		RecoveryAmount hpRecovery = parseRecoveryAmount(fields[2], "HP recovery");
		RecoveryAmount mpRecovery = parseRecoveryAmount(fields[3], "MP recovery");
		RecoveryAmount spRecovery = parseRecoveryAmount(fields[4], "SP recovery");
		RecoveryAmount reviveHpRecovery = parseRecoveryAmount(fields[5], "revive HP recovery");
		Set<CharacterAttribute> purificationAttributes =
		        parsePurificationAttributes(fields[6]);

		CharacterAttribute attribute = fields[7].isBlank()
		        ? null
		        : parseAttribute(fields[7]);

		Integer stages = fields[8].isBlank()
		        ? null
		        : parseInteger(fields[8], "stages");

		Integer duration = fields[9].isBlank()
		        ? null
		        : parseInteger(fields[9], "duration");

		return new RecoveryItemDefinition(
		        itemId,
		        targetType,
		        hpRecovery,
		        mpRecovery,
		        spRecovery,
		        reviveHpRecovery,
		        purificationAttributes,
		        attribute,
		        stages,
		        duration
		);
	}

	private RecoveryAmount parseRecoveryAmount(String value, String fieldName) {
		String normalizedValue = value.trim();

		if (normalizedValue.endsWith("%")) {
			int percent = parseInteger(normalizedValue.substring(0, normalizedValue.length() - 1), fieldName);

			if (percent < 1 || percent > 100) {
				throw new IllegalArgumentException(fieldName + " percentage must be between 1 and 100.");
			}

			return new RecoveryAmount(0, percent);
		}

		return new RecoveryAmount(parseInteger(normalizedValue, fieldName), 0);
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

	private Set<CharacterAttribute> parsePurificationAttributes(String value) {
	    String normalizedValue = value.trim();

	    if (normalizedValue.equalsIgnoreCase("false")) {
	        return EnumSet.noneOf(CharacterAttribute.class);
	    }

	    if (normalizedValue.equalsIgnoreCase("true")) {
	        return EnumSet.allOf(CharacterAttribute.class);
	    }

	    Set<CharacterAttribute> attributes =
	            EnumSet.noneOf(CharacterAttribute.class);

	    for (String entry : normalizedValue.split(",", -1)) {
	        CharacterAttribute attribute = parseAttribute(entry);

	        if (!attributes.add(attribute)) {
	            throw new IllegalArgumentException(
	                    "Duplicate purification attribute: " + attribute);
	        }
	    }

	    return attributes;
	}

	private CharacterAttribute parseAttribute(String value) {
	    String normalizedValue = value.trim().toUpperCase(Locale.ROOT);

	    try {
	        return CharacterAttribute.valueOf(normalizedValue);
	    } catch (IllegalArgumentException exception) {
	        throw new IllegalArgumentException(
	                "Invalid character attribute: " + normalizedValue,
	                exception
	        );
	    }
	}
}