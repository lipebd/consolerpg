package rpg.narrative;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.quests.QuestCatalog;
import rpg.quests.QuestDefinition;
import rpg.quests.QuestObjectiveDefinition;
import rpg.quests.QuestObjectiveType;
import rpg.quests.QuestStepDefinition;

// Loads and validates external quest dialogue associations.
public final class QuestDialogueLoader {

    private static final int EXPECTED_FIELD_COUNT = 4;

    public QuestDialogueCatalog load(
            Path filePath, QuestCatalog questCatalog, DialogueCatalog dialogueCatalog)
            throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("Quest dialogue file path cannot be null.");
        }

        if (questCatalog == null) {
            throw new IllegalArgumentException("Quest catalog cannot be null.");
        }

        if (dialogueCatalog == null) {
            throw new IllegalArgumentException("Dialogue catalog cannot be null.");
        }

        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        QuestDialogueCatalog catalog = new QuestDialogueCatalog();

        for (int index = 0; index < lines.size(); index++) {
            String sourceLine = lines.get(index);
            int physicalLine = index + 1;

            if (sourceLine.isBlank() || sourceLine.stripLeading().startsWith("#")) {
                continue;
            }

            loadDefinition(
                    sourceLine, physicalLine, filePath, questCatalog, dialogueCatalog, catalog);
        }

        validateRequiredAssociations(catalog, questCatalog, filePath);

        return catalog;
    }

    private void loadDefinition(
            String sourceLine,
            int physicalLine,
            Path filePath,
            QuestCatalog questCatalog,
            DialogueCatalog dialogueCatalog,
            QuestDialogueCatalog catalog) {
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
            QuestDialogueContext context = parseContext(fields[2]);
            String dialogueId = fields[3].trim();

            QuestDefinition questDefinition = questCatalog.getById(questId);

            validateQuestContext(questDefinition, step, context);

            dialogueCatalog.getById(dialogueId);

            catalog.register(new QuestDialogueDefinition(questId, step, context, dialogueId));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid quest dialogue in "
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
                    "Quest dialogue step must be a valid integer: " + value, exception);
        }
    }

    private static QuestDialogueContext parseContext(String value) {
        try {
            return QuestDialogueContext.valueOf(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unknown quest dialogue context: " + value, exception);
        }
    }

    private static void validateQuestContext(
            QuestDefinition questDefinition, int step, QuestDialogueContext context) {
        switch (context) {
            case OFFER, ACCEPT, DECLINE -> {
                if (step != 0) {
                    throw new IllegalArgumentException(
                            context
                                    + " dialogue for quest "
                                    + questDefinition.getId()
                                    + " must use step 0.");
                }
            }

            case PRE_COMPLETE, COMPLETE -> {
                QuestStepDefinition stepDefinition = getStep(questDefinition, step);

                if (!containsObjectiveType(stepDefinition, QuestObjectiveType.TALK_TO_NPC)) {
                    throw new IllegalArgumentException(
                            context
                                    + " dialogue requires a TALK_TO_NPC objective in quest "
                                    + questDefinition.getId()
                                    + ", step "
                                    + step
                                    + ".");
                }
            }

            case PENDING -> {
                QuestStepDefinition stepDefinition = getStep(questDefinition, step);

                if (!containsObjectiveType(stepDefinition, QuestObjectiveType.DELIVER_ITEM)) {
                    throw new IllegalArgumentException(
                            "PENDING dialogue requires a DELIVER_ITEM objective in quest "
                                    + questDefinition.getId()
                                    + ", step "
                                    + step
                                    + ".");
                }
            }
        }
    }

    private static QuestStepDefinition getStep(QuestDefinition questDefinition, int step) {
        if (step <= 0 || step > questDefinition.getSteps().size()) {
            throw new IllegalArgumentException(
                    "Quest " + questDefinition.getId() + " does not contain step " + step + ".");
        }

        return questDefinition.getSteps().get(step - 1);
    }

    private static boolean containsObjectiveType(
            QuestStepDefinition stepDefinition, QuestObjectiveType objectiveType) {
        for (QuestObjectiveDefinition objective : stepDefinition.getObjectives()) {
            if (objective.getObjectiveType() == objectiveType) {
                return true;
            }
        }

        return false;
    }

    private static void validateRequiredAssociations(
            QuestDialogueCatalog catalog, QuestCatalog questCatalog, Path filePath) {
        for (QuestDefinition questDefinition : questCatalog.getAll()) {
            String questId = questDefinition.getId();

            requireAssociation(catalog, questId, 0, QuestDialogueContext.OFFER, filePath);

            for (QuestStepDefinition stepDefinition : questDefinition.getSteps()) {
                if (containsObjectiveType(stepDefinition, QuestObjectiveType.TALK_TO_NPC)) {
                    requireAssociation(
                            catalog,
                            questId,
                            stepDefinition.getStep(),
                            QuestDialogueContext.COMPLETE,
                            filePath);
                }
            }
        }
    }

    private static void requireAssociation(
            QuestDialogueCatalog catalog,
            String questId,
            int step,
            QuestDialogueContext context,
            Path filePath) {
        if (catalog.find(questId, step, context).isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required "
                            + context
                            + " dialogue for quest "
                            + questId
                            + ", step "
                            + step
                            + " in "
                            + filePath
                            + ".");
        }
    }
}
