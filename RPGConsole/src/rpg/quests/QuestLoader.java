package rpg.quests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Loads complete quest definitions from external quest and step data.
public final class QuestLoader {

    private static final int QUEST_FIELD_COUNT = 5;
    private static final int STEP_FIELD_COUNT = 4;

    public QuestCatalog load(Path questFilePath, Path questStepFilePath) throws IOException {
        if (questFilePath == null) {
            throw new IllegalArgumentException("Quest file path cannot be null.");
        }

        if (questStepFilePath == null) {
            throw new IllegalArgumentException("Quest step file path cannot be null.");
        }

        Map<String, QuestDraft> questDrafts = loadQuestDrafts(questFilePath);

        Map<String, List<QuestStepDefinition>> stepsByQuestId =
                loadQuestSteps(questStepFilePath, questDrafts);

        QuestCatalog catalog = new QuestCatalog();

        for (QuestDraft questDraft : questDrafts.values()) {
            List<QuestStepDefinition> questSteps =
                    new ArrayList<>(stepsByQuestId.get(questDraft.id));

            questSteps.sort(Comparator.comparingInt(QuestStepDefinition::getStep));

            try {
                QuestDefinition questDefinition =
                        new QuestDefinition(
                                questDraft.id,
                                questDraft.title,
                                questDraft.description,
                                questDraft.startingNpcId,
                                questDraft.availabilityRequirementSetId,
                                questSteps);

                catalog.register(questDefinition);
            } catch (IllegalArgumentException exception) {
                throw invalidDefinition(
                        "quest",
                        questFilePath,
                        questDraft.lineNumber,
                        questDraft.sourceLine,
                        exception);
            }
        }

        return catalog;
    }

    private Map<String, QuestDraft> loadQuestDrafts(Path questFilePath) throws IOException {
        Map<String, QuestDraft> questDrafts = new LinkedHashMap<>();
        List<String> lines = Files.readAllLines(questFilePath);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            try {
                QuestDraft questDraft = parseQuestDraft(line, index + 1);

                if (questDrafts.containsKey(questDraft.id)) {
                    throw new IllegalArgumentException("Quest ID is duplicated: " + questDraft.id);
                }

                questDrafts.put(questDraft.id, questDraft);
            } catch (IllegalArgumentException exception) {
                throw invalidDefinition("quest", questFilePath, index + 1, line, exception);
            }
        }

        return questDrafts;
    }

    private QuestDraft parseQuestDraft(String line, int lineNumber) {
        String[] fields = line.split(";", -1);

        if (fields.length != QUEST_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + QUEST_FIELD_COUNT + " fields but found " + fields.length + ".");
        }

        String id = fields[0].trim();
        String title = parseTitle(fields[1], "Quest title");
        String description = parseDescription(fields[2], "Quest description");
        String startingNpcId = fields[3].trim();
        String requirementSetId = fields[4].trim();

        Optional<String> availabilityRequirementSetId =
                requirementSetId.isEmpty() ? Optional.empty() : Optional.of(requirementSetId);

        return new QuestDraft(
                id,
                title,
                description,
                startingNpcId,
                availabilityRequirementSetId,
                lineNumber,
                line);
    }

    private Map<String, List<QuestStepDefinition>> loadQuestSteps(
            Path questStepFilePath, Map<String, QuestDraft> questDrafts) throws IOException {
        Map<String, List<QuestStepDefinition>> stepsByQuestId = new LinkedHashMap<>();

        for (String questId : questDrafts.keySet()) {
            stepsByQuestId.put(questId, new ArrayList<>());
        }

        List<String> lines = Files.readAllLines(questStepFilePath);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            try {
                addQuestStep(line, stepsByQuestId);
            } catch (IllegalArgumentException exception) {
                throw invalidDefinition(
                        "quest step", questStepFilePath, index + 1, line, exception);
            }
        }

        return stepsByQuestId;
    }

    private void addQuestStep(String line, Map<String, List<QuestStepDefinition>> stepsByQuestId) {
        String[] fields = line.split(";", -1);

        if (fields.length != STEP_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + STEP_FIELD_COUNT + " fields but found " + fields.length + ".");
        }

        String questId = fields[0].trim();

        List<QuestStepDefinition> questSteps = stepsByQuestId.get(questId);

        if (questSteps == null) {
            throw new IllegalArgumentException(
                    "Quest step references unknown quest ID: " + questId);
        }

        int step = parseInteger(fields[1], "quest step number");
        String title = parseTitle(fields[2], "Quest step title");
        String description = parseDescription(fields[3], "Quest step description");

        for (QuestStepDefinition existingStep : questSteps) {
            if (existingStep.getStep() == step) {
                throw new IllegalArgumentException(
                        "Quest step number is duplicated for quest " + questId + ": " + step);
            }
        }

        questSteps.add(new QuestStepDefinition(step, title, description));
    }

    private String parseTitle(String value, String fieldName) {
        String normalizedValue = requireText(value, fieldName);

        if (normalizedValue.contains("\\")) {
            throw new IllegalArgumentException(fieldName + " cannot contain backslashes.");
        }

        return normalizedValue;
    }

    private String parseDescription(String value, String fieldName) {
        String normalizedValue = requireText(value, fieldName);
        StringBuilder description = new StringBuilder();

        for (int index = 0; index < normalizedValue.length(); index++) {
            char character = normalizedValue.charAt(index);

            if (character != '\\') {
                description.append(character);
                continue;
            }

            if (index + 1 >= normalizedValue.length() || normalizedValue.charAt(index + 1) != 'n') {
                throw new IllegalArgumentException(
                        fieldName + " can only use backslashes as the \\\\n line-break sequence.");
            }

            description.append('\n');
            index++;
        }

        return description.toString();
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }

        return value.trim();
    }

    private int parseInteger(String value, String fieldName) {
        String normalizedValue = value.trim();

        try {
            return Integer.parseInt(normalizedValue);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid " + fieldName + ": " + normalizedValue, exception);
        }
    }

    private IllegalArgumentException invalidDefinition(
            String definitionName,
            Path filePath,
            int lineNumber,
            String line,
            IllegalArgumentException cause) {
        return new IllegalArgumentException(
                "Invalid "
                        + definitionName
                        + " definition in "
                        + filePath
                        + " at line "
                        + lineNumber
                        + ": "
                        + line
                        + ". "
                        + cause.getMessage(),
                cause);
    }

    private static final class QuestDraft {

        private final String id;
        private final String title;
        private final String description;
        private final String startingNpcId;
        private final Optional<String> availabilityRequirementSetId;
        private final int lineNumber;
        private final String sourceLine;

        private QuestDraft(
                String id,
                String title,
                String description,
                String startingNpcId,
                Optional<String> availabilityRequirementSetId,
                int lineNumber,
                String sourceLine) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.startingNpcId = startingNpcId;
            this.availabilityRequirementSetId = availabilityRequirementSetId;
            this.lineNumber = lineNumber;
            this.sourceLine = sourceLine;
        }
    }
}
