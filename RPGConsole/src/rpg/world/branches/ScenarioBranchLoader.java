package rpg.world.branches;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.world.scenarios.CombatScenarioDefinition;
import rpg.world.scenarios.CombatScenarioDefinitionCatalog;
import rpg.world.scenarios.ScenarioDefinition;
import rpg.world.scenarios.ScenarioDefinitionCatalog;
import rpg.world.scenarios.ScenarioType;

// Loads fixed exploration branches from external data.
public final class ScenarioBranchLoader {

	private static final int EXPECTED_FIELD_COUNT = 7;

	public ScenarioBranchCatalog load(
			Path filePath,
			ScenarioDefinitionCatalog scenarioDefinitionCatalog,
			CombatScenarioDefinitionCatalog
					combatScenarioDefinitionCatalog)
			throws IOException {
		ScenarioBranchCatalog catalog =
				new ScenarioBranchCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.isEmpty()
					|| trimmedLine.startsWith("#")) {
				continue;
			}

			ScenarioBranchDefinition branchDefinition =
					parseDefinition(
							trimmedLine,
							scenarioDefinitionCatalog,
							combatScenarioDefinitionCatalog);

			catalog.register(branchDefinition);
		}

		return catalog;
	}

	private ScenarioBranchDefinition parseDefinition(
			String line,
			ScenarioDefinitionCatalog scenarioDefinitionCatalog,
			CombatScenarioDefinitionCatalog
					combatScenarioDefinitionCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != EXPECTED_FIELD_COUNT) {
			throw new IllegalArgumentException(
					"Invalid scenario branch definition. Expected "
							+ EXPECTED_FIELD_COUNT
							+ " fields but found "
							+ fields.length + ": " + line);
		}

		String id = fields[0].trim();
		String sourceScenarioId = fields[1].trim();

		int explorationPosition = parseInteger(
				fields[2],
				"branch exploration position");

		String destinationScenarioId = fields[3].trim();

		ScenarioDefinition sourceDefinition =
				scenarioDefinitionCatalog.getById(
						sourceScenarioId);

		scenarioDefinitionCatalog.getById(
				destinationScenarioId);

		if (sourceDefinition.getType() == ScenarioType.CITY) {
			throw new IllegalArgumentException(
					"Scenario branch source must be a combat scenario: "
							+ sourceScenarioId);
		}

		CombatScenarioDefinition combatDefinition =
				combatScenarioDefinitionCatalog.getByScenarioId(
						sourceScenarioId);

		int explorationLength = combatDefinition
				.getExplorationSettings()
				.getLength();

		if (explorationPosition > explorationLength) {
			throw new IllegalArgumentException(
					"Scenario branch position exceeds exploration length: "
							+ sourceScenarioId
							+ ", position "
							+ explorationPosition
							+ ", length "
							+ explorationLength);
		}

		if (sourceDefinition.getType() == ScenarioType.DUNGEON
				&& explorationPosition == explorationLength) {
			throw new IllegalArgumentException(
					"A scenario branch cannot use the final dungeon position: "
							+ sourceScenarioId
							+ ", position "
							+ explorationPosition);
		}

		boolean initiallyUnlocked =
		        parseBoolean(fields[4], "initially unlocked");

		String displayName = fields[5].trim();

		String description = fields[6]
				.trim()
				.replace("\\n", "\n");

		return new ScenarioBranchDefinition(
		        id,
		        sourceScenarioId,
		        explorationPosition,
		        destinationScenarioId,
		        initiallyUnlocked,
		        displayName,
		        description);
	}

	private int parseInteger(String value, String fieldName) {
		String normalizedValue = value.trim();

		try {
			return Integer.parseInt(normalizedValue);
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Invalid " + fieldName + ": "
							+ normalizedValue,
					exception);
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

	    throw new IllegalArgumentException(
	            "Invalid " + fieldName + ": " + normalizedValue);
	}
}