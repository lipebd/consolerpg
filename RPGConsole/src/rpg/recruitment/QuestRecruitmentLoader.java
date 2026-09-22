package rpg.recruitment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.quests.QuestCatalog;
import rpg.quests.QuestDefinition;

// Loads and validates external quest recruitment associations.
public final class QuestRecruitmentLoader {

	private static final int EXPECTED_FIELD_COUNT = 4;

	public QuestRecruitmentCatalog load(Path filePath, QuestCatalog questCatalog)
			throws IOException {
		if (filePath == null) {
			throw new IllegalArgumentException("Quest recruitment file path cannot be null.");
		}

		if (questCatalog == null) {
			throw new IllegalArgumentException("Quest catalog cannot be null.");
		}

		List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

		QuestRecruitmentCatalog catalog = new QuestRecruitmentCatalog();

		for (int index = 0; index < lines.size(); index++) {
			String sourceLine = lines.get(index);
			int physicalLine = index + 1;

			if (sourceLine.isBlank() || sourceLine.stripLeading().startsWith("#")) {
				continue;
			}

			loadDefinition(sourceLine, physicalLine, filePath, questCatalog, catalog);
		}

		return catalog;
	}

	private static void loadDefinition(
			String sourceLine,
			int physicalLine,
			Path filePath,
			QuestCatalog questCatalog,
			QuestRecruitmentCatalog catalog) {
		try {
			String[] fields = sourceLine.split(";", -1);

			if (fields.length != EXPECTED_FIELD_COUNT) {
				throw new IllegalArgumentException(
						"Expected "
								+ EXPECTED_FIELD_COUNT
								+ " fields but found "
								+ fields.length
								+ ".");
			}

			String questId = fields[0].trim();
			int step = parseStep(fields[1]);
			String candidateId = fields[2].trim();
			int allyLevel = parseAllyLevel(fields[3]);

			QuestRecruitmentDefinition definition =
					new QuestRecruitmentDefinition(questId, step, candidateId, allyLevel);

			QuestDefinition questDefinition = questCatalog.getById(definition.getQuestId());

			validateQuestStep(questDefinition, definition.getStep());

			catalog.register(definition);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException(
					"Invalid quest recruitment in "
							+ filePath
							+ " at line "
							+ physicalLine
							+ ": "
							+ exception.getMessage(),
							exception);
		}
	}

	private static int parseStep(String value) {
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Recruitment quest step must be a valid integer: " + value, exception);
		}
	}

	private static void validateQuestStep(QuestDefinition questDefinition, int step) {
		if (step > questDefinition.getSteps().size()) {
			throw new IllegalArgumentException(
					"Quest " + questDefinition.getId() + " does not contain step " + step + ".");
		}
	}

	private static int parseAllyLevel(String value) {
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException(
					"Recruitment ally level must be a valid integer: "
							+ value,
							exception
					);
		}
	}
}
