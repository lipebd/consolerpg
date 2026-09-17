package rpg.quests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Loads complete quest definitions from external quest, step and objective data.
public final class QuestLoader {

    private static final int QUEST_FIELD_COUNT = 5;
    private static final int STEP_FIELD_COUNT = 4;
    private static final int OBJECTIVE_FIELD_COUNT = 8;

    public QuestCatalog load(
            Path questFilePath, Path questStepFilePath, Path questObjectiveFilePath)
            throws IOException {
        if (questFilePath == null) {
            throw new IllegalArgumentException("Quest file path cannot be null.");
        }

        if (questStepFilePath == null) {
            throw new IllegalArgumentException("Quest step file path cannot be null.");
        }

        if (questObjectiveFilePath == null) {
            throw new IllegalArgumentException("Quest objective file path cannot be null.");
        }

        Map<String, QuestDraft> questDrafts = loadQuestDrafts(questFilePath);

        Map<String, List<QuestStepDraft>> stepsByQuestId =
                loadQuestSteps(questStepFilePath, questDrafts);

        loadQuestObjectives(questObjectiveFilePath, stepsByQuestId);

        return buildCatalog(questFilePath, questStepFilePath, questDrafts, stepsByQuestId);
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

    private Map<String, List<QuestStepDraft>> loadQuestSteps(
            Path questStepFilePath, Map<String, QuestDraft> questDrafts) throws IOException {
        Map<String, List<QuestStepDraft>> stepsByQuestId = new LinkedHashMap<>();

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
                addQuestStep(line, index + 1, stepsByQuestId);
            } catch (IllegalArgumentException exception) {
                throw invalidDefinition(
                        "quest step", questStepFilePath, index + 1, line, exception);
            }
        }

        return stepsByQuestId;
    }

    private void addQuestStep(
            String line, int lineNumber, Map<String, List<QuestStepDraft>> stepsByQuestId) {
        String[] fields = line.split(";", -1);

        if (fields.length != STEP_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + STEP_FIELD_COUNT + " fields but found " + fields.length + ".");
        }

        String questId = fields[0].trim();
        List<QuestStepDraft> questSteps = stepsByQuestId.get(questId);

        if (questSteps == null) {
            throw new IllegalArgumentException(
                    "Quest step references unknown quest ID: " + questId);
        }

        int step = parseInteger(fields[1], "quest step number");
        String title = parseTitle(fields[2], "Quest step title");
        String description = parseDescription(fields[3], "Quest step description");

        for (QuestStepDraft existingStep : questSteps) {
            if (existingStep.step == step) {
                throw new IllegalArgumentException(
                        "Quest step number is duplicated for quest " + questId + ": " + step);
            }
        }

        questSteps.add(new QuestStepDraft(step, title, description, lineNumber, line));
    }

    private void loadQuestObjectives(
            Path questObjectiveFilePath, Map<String, List<QuestStepDraft>> stepsByQuestId)
            throws IOException {
        Set<String> objectiveIds = new HashSet<>();
        List<String> lines = Files.readAllLines(questObjectiveFilePath);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            try {
                addQuestObjective(line, stepsByQuestId, objectiveIds);
            } catch (IllegalArgumentException exception) {
                throw invalidDefinition(
                        "quest objective", questObjectiveFilePath, index + 1, line, exception);
            }
        }
    }

    private void addQuestObjective(
            String line,
            Map<String, List<QuestStepDraft>> stepsByQuestId,
            Set<String> objectiveIds) {
        String[] fields = line.split(";", -1);

        if (fields.length != OBJECTIVE_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected "
                            + OBJECTIVE_FIELD_COUNT
                            + " fields but found "
                            + fields.length
                            + ".");
        }

        String id = fields[0].trim();
        String questId = fields[1].trim();
        int step = parseInteger(fields[2], "objective quest step");
        int order = parseInteger(fields[3], "quest objective order");
        QuestObjectiveType objectiveType = parseObjectiveType(fields[4]);
        String targetValue = fields[5].trim();
        int requiredQuantity = parseInteger(fields[6], "quest objective required quantity");
        String text = parseDescription(fields[7], "Quest objective text");

        List<QuestStepDraft> questSteps = stepsByQuestId.get(questId);

        if (questSteps == null) {
            throw new IllegalArgumentException(
                    "Quest objective references unknown quest ID: " + questId);
        }

        QuestStepDraft targetStep = null;

        for (QuestStepDraft questStep : questSteps) {
            if (questStep.step == step) {
                targetStep = questStep;
                break;
            }
        }

        if (targetStep == null) {
            throw new IllegalArgumentException(
                    "Quest objective references unknown step "
                            + step
                            + " for quest "
                            + questId
                            + ".");
        }

        Optional<String> targetId =
                targetValue.isEmpty() ? Optional.empty() : Optional.of(targetValue);

        QuestObjectiveDefinition objective =
                new QuestObjectiveDefinition(
                        id, order, objectiveType, targetId, requiredQuantity, text);

        if (objectiveIds.contains(objective.getId())) {
            throw new IllegalArgumentException(
                    "Quest objective ID is duplicated: " + objective.getId());
        }

        for (QuestObjectiveDefinition existingObjective : targetStep.objectives) {
            if (existingObjective.getOrder() == objective.getOrder()) {
                throw new IllegalArgumentException(
                        "Quest objective order is duplicated for quest "
                                + questId
                                + " step "
                                + step
                                + ": "
                                + objective.getOrder());
            }

            if (existingObjective.getObjectiveType() == objective.getObjectiveType()
                    && existingObjective.getTargetId().equals(objective.getTargetId())) {
                throw new IllegalArgumentException(
                        "Quest step cannot repeat objective type and target: "
                                + objective.getObjectiveType()
                                + ":"
                                + objective.getTargetId().orElse(""));
            }
        }

        objectiveIds.add(objective.getId());
        targetStep.objectives.add(objective);
    }

    private QuestCatalog buildCatalog(
            Path questFilePath,
            Path questStepFilePath,
            Map<String, QuestDraft> questDrafts,
            Map<String, List<QuestStepDraft>> stepsByQuestId) {
        QuestCatalog catalog = new QuestCatalog();

        for (QuestDraft questDraft : questDrafts.values()) {
            List<QuestStepDraft> questStepDrafts =
                    new ArrayList<>(stepsByQuestId.get(questDraft.id));

            questStepDrafts.sort(Comparator.comparingInt(questStepDraft -> questStepDraft.step));

            List<QuestStepDefinition> questSteps = new ArrayList<>();

            for (QuestStepDraft stepDraft : questStepDrafts) {
                List<QuestObjectiveDefinition> objectives = new ArrayList<>(stepDraft.objectives);

                objectives.sort(Comparator.comparingInt(QuestObjectiveDefinition::getOrder));

                try {
                    questSteps.add(
                            new QuestStepDefinition(
                                    stepDraft.step,
                                    stepDraft.title,
                                    stepDraft.description,
                                    objectives));
                } catch (IllegalArgumentException exception) {
                    throw invalidDefinition(
                            "quest step",
                            questStepFilePath,
                            stepDraft.lineNumber,
                            stepDraft.sourceLine,
                            exception);
                }
            }

            try {
                catalog.register(
                        new QuestDefinition(
                                questDraft.id,
                                questDraft.title,
                                questDraft.description,
                                questDraft.startingNpcId,
                                questDraft.availabilityRequirementSetId,
                                questSteps));
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

    private QuestObjectiveType parseObjectiveType(String value) {
        String normalizedValue = value.trim();

        try {
            return QuestObjectiveType.valueOf(normalizedValue);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid quest objective type: " + normalizedValue, exception);
        }
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
                        fieldName
                                + " can only use backslashes as the \\\\n "
                                + "line-break sequence.");
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

    private static final class QuestStepDraft {

        private final int step;
        private final String title;
        private final String description;
        private final int lineNumber;
        private final String sourceLine;
        private final List<QuestObjectiveDefinition> objectives;

        private QuestStepDraft(
                int step, String title, String description, int lineNumber, String sourceLine) {
            this.step = step;
            this.title = title;
            this.description = description;
            this.lineNumber = lineNumber;
            this.sourceLine = sourceLine;
            this.objectives = new ArrayList<>();
        }
    }
}
