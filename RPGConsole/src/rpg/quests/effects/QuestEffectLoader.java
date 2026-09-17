package rpg.quests.effects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import rpg.flags.FlagCatalog;
import rpg.quests.QuestCatalog;
import rpg.quests.QuestDefinition;
import rpg.world.travel.TravelCatalog;

// Loads permanent quest-effect definitions from external data.
public final class QuestEffectLoader {

    private static final int EXPECTED_FIELD_COUNT = 7;

    public QuestEffectCatalog load(
            Path filePath,
            QuestCatalog questCatalog,
            FlagCatalog flagCatalog,
            TravelCatalog travelCatalog)
            throws IOException {
        if (questCatalog == null) {
            throw new IllegalArgumentException("Quest catalog cannot be null.");
        }

        if (flagCatalog == null) {
            throw new IllegalArgumentException("Flag catalog cannot be null.");
        }

        if (travelCatalog == null) {
            throw new IllegalArgumentException("Travel catalog cannot be null.");
        }

        QuestEffectCatalog effectCatalog = new QuestEffectCatalog();
        List<String> lines = Files.readAllLines(filePath);

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                continue;
            }

            QuestEffectDefinition effectDefinition =
                    parseDefinition(trimmedLine, questCatalog, flagCatalog, travelCatalog);

            effectCatalog.register(effectDefinition);
        }

        return effectCatalog;
    }

    private QuestEffectDefinition parseDefinition(
            String line,
            QuestCatalog questCatalog,
            FlagCatalog flagCatalog,
            TravelCatalog travelCatalog) {
        String[] fields = line.split(";", -1);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Invalid quest effect definition. Expected "
                            + EXPECTED_FIELD_COUNT
                            + " fields but found "
                            + fields.length
                            + ": "
                            + line);
        }

        String questId = fields[0].trim();
        int step = parseInteger(fields[1], "quest effect step");
        QuestEffectTrigger trigger = parseTrigger(fields[2]);
        int order = parseInteger(fields[3], "quest effect order");
        QuestEffectType effectType = parseEffectType(fields[4]);
        String targetId = fields[5].trim();
        Optional<String> displayText = parseDisplayText(fields[6]);

        QuestDefinition questDefinition = questCatalog.getById(questId);

        validateStepReference(questDefinition, step, trigger);
        validateTargetReference(effectType, targetId, flagCatalog, travelCatalog);

        return new QuestEffectDefinition(
                questId, step, trigger, order, effectType, targetId, displayText);
    }

    private void validateStepReference(
            QuestDefinition questDefinition, int step, QuestEffectTrigger trigger) {
        if (trigger != QuestEffectTrigger.STEP_COMPLETE) {
            return;
        }

        if (step < 1 || step > questDefinition.getSteps().size()) {
            throw new IllegalArgumentException(
                    "Quest effect references an unknown step for quest "
                            + questDefinition.getId()
                            + ": "
                            + step);
        }
    }

    private void validateTargetReference(
            QuestEffectType effectType,
            String targetId,
            FlagCatalog flagCatalog,
            TravelCatalog travelCatalog) {
        switch (effectType) {
            case SET_FLAG -> flagCatalog.getById(targetId);
            case UNLOCK_ROUTE -> travelCatalog.getById(targetId);
        }
    }

    private QuestEffectTrigger parseTrigger(String value) {
        String normalizedValue = value.trim();

        try {
            return QuestEffectTrigger.valueOf(normalizedValue);

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid quest effect trigger: " + normalizedValue, exception);
        }
    }

    private QuestEffectType parseEffectType(String value) {
        String normalizedValue = value.trim();

        try {
            return QuestEffectType.valueOf(normalizedValue);

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid quest effect type: " + normalizedValue, exception);
        }
    }

    private Optional<String> parseDisplayText(String value) {
        String trimmedValue = value.trim();

        if (trimmedValue.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(decodeText(trimmedValue));
    }

    private String decodeText(String value) {
        StringBuilder decodedText = new StringBuilder();

        for (int index = 0; index < value.length(); index++) {
            char currentCharacter = value.charAt(index);

            if (currentCharacter != '\\') {
                decodedText.append(currentCharacter);
                continue;
            }

            if (index + 1 >= value.length() || value.charAt(index + 1) != 'n') {
                throw new IllegalArgumentException(
                        "Quest effect display text contains an invalid escape sequence: " + value);
            }

            decodedText.append('\n');
            index++;
        }

        return decodedText.toString();
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
}
