package rpg.world.scenarios;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.narrative.DialogueCatalog;
import rpg.requirements.RequirementCatalog;

// Loads and validates persistent dungeon boss gates.
public final class DungeonBossGateLoader {

	private static final int EXPECTED_FIELD_COUNT = 4;

	public DungeonBossGateCatalog load(
			Path filePath,
			ScenarioDefinitionCatalog scenarioDefinitionCatalog,
			DungeonDefinitionCatalog dungeonDefinitionCatalog,
			RequirementCatalog requirementCatalog,
			DialogueCatalog dialogueCatalog
	) throws IOException {
		if (filePath == null) {
			throw new IllegalArgumentException("Dungeon boss gate file path cannot be null.");
		}

		if (scenarioDefinitionCatalog == null) {
			throw new IllegalArgumentException("Scenario definition catalog cannot be null.");
		}

		if (dungeonDefinitionCatalog == null) {
			throw new IllegalArgumentException("Dungeon definition catalog cannot be null.");
		}

		if (requirementCatalog == null) {
			throw new IllegalArgumentException("Requirement catalog cannot be null.");
		}

		if (dialogueCatalog == null) {
			throw new IllegalArgumentException("Dialogue catalog cannot be null.");
		}

		List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

		DungeonBossGateCatalog catalog = new DungeonBossGateCatalog();

		for (int index = 0; index < lines.size(); index++) {
			String sourceLine = lines.get(index);
			int physicalLine = index + 1;

			if (sourceLine.isBlank() || sourceLine.stripLeading().startsWith("#")) {
				continue;
			}

			loadDefinition(
					sourceLine,
					physicalLine,
					filePath,
					scenarioDefinitionCatalog,
					dungeonDefinitionCatalog,
					requirementCatalog,
					dialogueCatalog,
					catalog
			);
		}

		return catalog;
	}

	private void loadDefinition(
			String sourceLine,
			int physicalLine,
			Path filePath,
			ScenarioDefinitionCatalog scenarioDefinitionCatalog,
			DungeonDefinitionCatalog dungeonDefinitionCatalog,
			RequirementCatalog requirementCatalog,
			DialogueCatalog dialogueCatalog,
			DungeonBossGateCatalog catalog
	) {
		try {
			String[] fields = sourceLine.split(";", -1);

			if (fields.length != EXPECTED_FIELD_COUNT) {
				throw new IllegalArgumentException(
						"Expected " + EXPECTED_FIELD_COUNT + " fields but found " + fields.length + "."
				);
			}

			String dungeonId = fields[0].trim();
			String availabilityRequirementSetId = fields[1].trim();
			String defaultBlockedDialogueId = fields[2].trim();
			String blockedDestinationScenarioId = fields[3].trim();

			dungeonDefinitionCatalog.getByScenarioId(dungeonId);
			requirementCatalog.getById(availabilityRequirementSetId);
			dialogueCatalog.getById(defaultBlockedDialogueId);
			scenarioDefinitionCatalog.getById(blockedDestinationScenarioId);

			catalog.register(
					new DungeonBossGateDefinition(
							dungeonId,
							availabilityRequirementSetId,
							defaultBlockedDialogueId,
							blockedDestinationScenarioId
					)
			);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Invalid dungeon boss gate in " + filePath + " at line " + physicalLine + ": "
							+ exception.getMessage(),
					exception
			);
		}
	}
}