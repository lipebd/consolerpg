package rpg.world.scenarios;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Loads scenario definitions from external data.
public final class ScenarioLoader {

	private static final int EXPECTED_FIELD_COUNT = 4;

	public ScenarioDefinitionCatalog load(Path filePath) throws IOException {

		ScenarioDefinitionCatalog catalog = new ScenarioDefinitionCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
				continue;
			}

			ScenarioDefinition definition = parseDefinition(trimmedLine);

			catalog.register(definition);
		}

		return catalog;
	}

	private ScenarioDefinition parseDefinition(String line) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException("Invalid scenario definition. Expected " + EXPECTED_FIELD_COUNT
					+ " fields but found " + fields.length + ": " + line);
		}

		String id = fields[0].trim();

		ScenarioType type;

		try {
			type = ScenarioType.valueOf(fields[1].trim());
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid scenario type: " + fields[1].trim());
		}

		String name = fields[2].trim();

		String description = fields[3].trim().replace("\\n", "\n");

		return new ScenarioDefinition(id, type, name, description);
	}
}