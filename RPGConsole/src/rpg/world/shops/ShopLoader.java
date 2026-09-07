package rpg.world.shops;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import rpg.world.scenarios.CombatScenarioDefinition;
import rpg.world.scenarios.CombatScenarioDefinitionCatalog;
import rpg.world.scenarios.ScenarioDefinition;
import rpg.world.scenarios.ScenarioDefinitionCatalog;
import rpg.world.scenarios.ScenarioType;

// Loads shop definitions from external data.
public final class ShopLoader {

	private static final int EXPECTED_FIELD_COUNT = 6;

	public ShopCatalog load(
			Path filePath,
			ScenarioDefinitionCatalog scenarioDefinitionCatalog,
			CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog
	) throws IOException {
		ShopCatalog catalog = new ShopCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index).trim();

			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			try {
				ShopDefinition shopDefinition = parseDefinition(line, scenarioDefinitionCatalog);

				catalog.register(shopDefinition);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid shop definition at line " + (index + 1) + ": " + line + ". " + exception.getMessage(),
						exception
				);
			}
		}

		validateExplorationMerchantAvailability(catalog, combatScenarioDefinitionCatalog);

		return catalog;
	}

	private ShopDefinition parseDefinition(String line, ScenarioDefinitionCatalog scenarioDefinitionCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Expected " + EXPECTED_FIELD_COUNT + " fields but found " + fields.length + "."
			);
		}

		String id = fields[0].trim();
		String scenarioId = fields[1].trim();
		ShopAccessType accessType = parseAccessType(fields[2]);
		int offerSlots = parseInteger(fields[3], "shop offer slots");
		String name = fields[4].trim();
		String description = fields[5].trim().replace("\\n", "\n");

		ShopDefinition shopDefinition = new ShopDefinition(id, scenarioId, accessType, offerSlots, name, description);

		validateScenarioType(shopDefinition, scenarioDefinitionCatalog.getById(scenarioId));

		return shopDefinition;
	}

	private ShopAccessType parseAccessType(String value) {
		String normalizedValue = value.trim().toUpperCase(Locale.ROOT);

		try {
			return ShopAccessType.valueOf(normalizedValue);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid shop access type: " + normalizedValue, exception);
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

	private void validateScenarioType(ShopDefinition shopDefinition, ScenarioDefinition scenarioDefinition) {
		ShopAccessType accessType = shopDefinition.getAccessType();
		ScenarioType scenarioType = scenarioDefinition.getType();

		if (accessType == ShopAccessType.CITY && scenarioType != ScenarioType.CITY) {
			throw new IllegalArgumentException(
					"CITY shop must reference a city scenario: " + shopDefinition.getScenarioId()
			);
		}

		if (accessType == ShopAccessType.EXPLORATION && scenarioType != ScenarioType.FIELD
				&& scenarioType != ScenarioType.DUNGEON) {
			throw new IllegalArgumentException(
					"EXPLORATION shop must reference a field or dungeon scenario: " + shopDefinition.getScenarioId()
			);
		}
	}

	private void validateExplorationMerchantAvailability(
			ShopCatalog shopCatalog,
			CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog
	) {
		for (CombatScenarioDefinition combatScenarioDefinition : combatScenarioDefinitionCatalog.getAll()) {
			if (combatScenarioDefinition.getExplorationSettings().getMerchantChance() <= 0) {
				continue;
			}

			boolean hasExplorationMerchant = shopCatalog.getByScenarioId(combatScenarioDefinition.getScenarioId())
					.stream().anyMatch(shopDefinition -> shopDefinition.getAccessType() == ShopAccessType.EXPLORATION);

			if (!hasExplorationMerchant) {
				throw new IllegalArgumentException(
						"Combat scenario with a positive merchant chance " + "must have at least one EXPLORATION shop: "
								+ combatScenarioDefinition.getScenarioId()
				);
			}
		}
	}
}