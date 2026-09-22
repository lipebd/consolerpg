package rpg.world.scenarios;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import rpg.narrative.DialogueCatalog;
import rpg.quests.QuestCatalog;
import rpg.quests.QuestDefinition;
import rpg.world.scenarios.CombatScenarioDialogueDefinition.Trigger;

// Loads dialogues associated with dungeon boss events.
public final class CombatScenarioDialogueLoader {

	private static final int EXPECTED_FIELD_COUNT = 5;

	public CombatScenarioDialogueCatalog load(
			Path filePath,
			DungeonDefinitionCatalog dungeonDefinitionCatalog,
			QuestCatalog questCatalog,
			DialogueCatalog dialogueCatalog
	) throws IOException {
		if (filePath == null || dungeonDefinitionCatalog == null || questCatalog == null || dialogueCatalog == null) {
			throw new IllegalArgumentException("Combat scenario dialogue loader arguments cannot be null.");
		}

		List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
		CombatScenarioDialogueCatalog catalog = new CombatScenarioDialogueCatalog();

		for (int index = 0; index < lines.size(); index++) {
			String sourceLine = lines.get(index);

			if (sourceLine.isBlank() || sourceLine.stripLeading().startsWith("#")) {
				continue;
			}

			try {
				String[] fields = sourceLine.split(";", -1);

				if (fields.length != EXPECTED_FIELD_COUNT) {
					throw new IllegalArgumentException(
							"Expected " + EXPECTED_FIELD_COUNT + " fields but found " + fields.length + "."
					);
				}

				String scenarioId = fields[0].trim();
				Trigger trigger = parseTrigger(fields[1]);
				Optional<String> questId = optionalId(fields[2]);
				int step = parseStep(fields[3], questId);
				String dialogueId = fields[4].trim();

				// The triggers supported today are all dungeon boss events.
				dungeonDefinitionCatalog.getByScenarioId(scenarioId);

				if (questId.isPresent()) {
					String id = questId.orElseThrow();
					QuestDefinition quest = questCatalog.getById(id);

					if (step > quest.getSteps().size()) {
						throw new IllegalArgumentException("Quest " + id + " does not contain step " + step + ".");
					}
				}

				dialogueCatalog.getById(dialogueId);

				catalog.register(new CombatScenarioDialogueDefinition(scenarioId, trigger, questId, step, dialogueId));
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid combat scenario dialogue in " + filePath + " at line " + (index + 1) + ": "
								+ exception.getMessage(),
						exception
				);
			}
		}

		return catalog;
	}

	private static Optional<String> optionalId(String value) {
		String id = value.trim();
		return id.isEmpty() ? Optional.empty() : Optional.of(id);
	}

	private static int parseStep(String value, Optional<String> questId) {
		String text = value.trim();

		if (questId.isEmpty()) {
			if (!text.isEmpty()) {
				throw new IllegalArgumentException("Step must be empty when quest ID is empty.");
			}
			return 0;
		}

		if (text.isEmpty()) {
			throw new IllegalArgumentException("Step is required when quest ID is defined.");
		}

		try {
			int step = Integer.parseInt(text);

			if (step <= 0) {
				throw new IllegalArgumentException("Quest step must be positive.");
			}

			return step;
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Quest step must be an integer: " + text, exception);
		}
	}

	private static Trigger parseTrigger(String value) {
		String name = value.trim();

		try {
			return Trigger.valueOf(name);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid combat scenario dialogue trigger: " + name, exception);
		}
	}
}