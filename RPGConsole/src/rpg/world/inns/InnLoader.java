package rpg.world.inns;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.world.scenarios.ScenarioDefinition;
import rpg.world.scenarios.ScenarioDefinitionCatalog;
import rpg.world.scenarios.ScenarioType;

// Loads INN definitions from external data.
public final class InnLoader {

	private static final int EXPECTED_FIELD_COUNT = 5;

	public InnCatalog load(
			Path filePath,
			ScenarioDefinitionCatalog scenarioDefinitionCatalog
	) throws IOException {
		InnCatalog catalog = new InnCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
				continue;
			}

			InnDefinition innDefinition = parseDefinition(
					trimmedLine,
					scenarioDefinitionCatalog
			);

			catalog.register(innDefinition);
		}

		return catalog;
	}

	private InnDefinition parseDefinition(
			String line,
			ScenarioDefinitionCatalog scenarioDefinitionCatalog
	) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Invalid Inn definition. Expected "
							+ EXPECTED_FIELD_COUNT
							+ " fields but found "
							+ fields.length
							+ ": "
							+ line
			);
		}

		String id = fields[0].trim();
		String scenarioId = fields[1].trim();
		String name = fields[2].trim();
		String description = fields[3].trim().replace("\\n", "\n");
		int price = parseInteger(fields[4], "Inn price");

		InnDefinition innDefinition = new InnDefinition(
				id,
				scenarioId,
				name,
				description,
				price
		);

		ScenarioDefinition scenarioDefinition =
				scenarioDefinitionCatalog.getById(scenarioId);

		if (scenarioDefinition.getType() != ScenarioType.CITY) {
			throw new IllegalArgumentException(
					"Inn scenario must reference a city: " + scenarioId
			);
		}

		return innDefinition;
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