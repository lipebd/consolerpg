package rpg.world.transport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.world.scenarios.ScenarioDefinition;
import rpg.world.scenarios.ScenarioDefinitionCatalog;
import rpg.world.scenarios.ScenarioType;

// Loads paid city transport definitions from external data.
public final class CityTransportLoader {

	private static final int EXPECTED_FIELD_COUNT = 3;

	public CityTransportCatalog load(Path filePath, ScenarioDefinitionCatalog scenarioDefinitionCatalog)
			throws IOException {
		CityTransportCatalog catalog = new CityTransportCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
				continue;
			}

			CityTransportDefinition definition = parseDefinition(trimmedLine, scenarioDefinitionCatalog);

			catalog.register(definition);
		}

		return catalog;
	}

	private CityTransportDefinition parseDefinition(String line, ScenarioDefinitionCatalog scenarioDefinitionCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Invalid city transport definition. Expected " + EXPECTED_FIELD_COUNT + " fields but found "
							+ fields.length + ": " + line
			);
		}

		String destinationScenarioId = fields[0].trim();
		int price = parseInteger(fields[1], "city transport price");
		String travelDescription = fields[2].trim().replace("\\n", "\n");

		CityTransportDefinition definition = new CityTransportDefinition(
				destinationScenarioId,
				price,
				travelDescription
		);

		ScenarioDefinition destinationDefinition = scenarioDefinitionCatalog.getById(destinationScenarioId);

		if (destinationDefinition.getType() != ScenarioType.CITY) {
			throw new IllegalArgumentException(
					"City transport destination must reference a city: " + destinationScenarioId
			);
		}

		return definition;
	}

	private int parseInteger(String value, String fieldName) {
		String normalizedValue = value.trim();

		try {
			return Integer.parseInt(normalizedValue);

		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Invalid " + fieldName + ": " + normalizedValue, exception);
		}
	}
}