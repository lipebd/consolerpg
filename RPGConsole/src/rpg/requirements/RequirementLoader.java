package rpg.requirements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rpg.flags.FlagCatalog;
import rpg.items.ItemDefinitionCatalog;
import rpg.quests.QuestCatalog;

// Loads requirement-set definitions from external data.
public final class RequirementLoader {

    private static final int FIELD_COUNT = 5;

    public RequirementCatalog load(
            Path filePath,
            QuestCatalog questCatalog,
            FlagCatalog flagCatalog,
            ItemDefinitionCatalog itemDefinitionCatalog)
            throws IOException {

        if (filePath == null) {
            throw new IllegalArgumentException("Requirement file path cannot be null.");
        }

        if (questCatalog == null) {
            throw new IllegalArgumentException("Quest catalog cannot be null.");
        }

        if (flagCatalog == null) {
            throw new IllegalArgumentException("Flag catalog cannot be null.");
        }

        if (itemDefinitionCatalog == null) {
            throw new IllegalArgumentException("Item definition catalog cannot be null.");
        }

        Map<String, RequirementSetDraft> draftsById = new LinkedHashMap<>();

        List<String> lines = Files.readAllLines(filePath);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int lineNumber = index + 1;

            try {
                ParsedRequirement parsedRequirement =
                        parseRequirement(line, questCatalog, flagCatalog, itemDefinitionCatalog);

                RequirementSetDraft draft =
                        draftsById.computeIfAbsent(
                                parsedRequirement.setId(),
                                ignored ->
                                        new RequirementSetDraft(
                                                parsedRequirement.setId(), lineNumber, line));

                draft.add(parsedRequirement.definition(), lineNumber, line);
            } catch (IllegalArgumentException exception) {
                throw invalidLine(filePath, lineNumber, line, exception);
            }
        }

        RequirementCatalog catalog = new RequirementCatalog();

        for (RequirementSetDraft draft : draftsById.values()) {
            try {
                catalog.register(draft.build());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Invalid requirement set "
                                + draft.getSetId()
                                + " in "
                                + filePath
                                + ", first declared at line "
                                + draft.getFirstLineNumber()
                                + ": "
                                + draft.getFirstLineContent()
                                + ". "
                                + exception.getMessage(),
                        exception);
            }
        }

        return catalog;
    }

    private ParsedRequirement parseRequirement(
            String line,
            QuestCatalog questCatalog,
            FlagCatalog flagCatalog,
            ItemDefinitionCatalog itemDefinitionCatalog) {

        String[] fields = line.split(";", -1);

        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + FIELD_COUNT + " fields but found " + fields.length + ".");
        }

        String setId = fields[0].trim();
        int order = parseInteger(fields[1], "requirement order");
        RequirementType type = parseType(fields[2]);
        String targetId = parseOptionalText(fields[3]);
        Integer requiredValue = parseOptionalInteger(fields[4], "required value");

        RequirementDefinition definition =
                new RequirementDefinition(order, type, targetId, requiredValue);

        validateReference(definition, questCatalog, flagCatalog, itemDefinitionCatalog);

        return new ParsedRequirement(setId, definition);
    }

    private void validateReference(
            RequirementDefinition definition,
            QuestCatalog questCatalog,
            FlagCatalog flagCatalog,
            ItemDefinitionCatalog itemDefinitionCatalog) {

        switch (definition.getType()) {
            case ITEM_QUANTITY_AT_LEAST ->
                    itemDefinitionCatalog.getById(getRequiredTarget(definition));

            case QUEST_NOT_STARTED, QUEST_ACTIVE, QUEST_COMPLETED ->
                    questCatalog.getById(getRequiredTarget(definition));

            case FLAG_SET, FLAG_UNSET -> flagCatalog.getById(getRequiredTarget(definition));

            case LEADER_LEVEL_AT_LEAST -> {
                // This requirement has no catalog-backed target.
            }
        }
    }

    private String getRequiredTarget(RequirementDefinition definition) {

        return definition
                .getTargetId()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Requirement target was not validated for type: "
                                                + definition.getType()));
    }

    private RequirementType parseType(String value) {
        String normalizedValue = value.trim();

        try {
            return RequirementType.valueOf(normalizedValue);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unknown requirement type: " + normalizedValue, exception);
        }
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

    private Integer parseOptionalInteger(String value, String fieldName) {

        String normalizedValue = value.trim();

        if (normalizedValue.isEmpty()) {
            return null;
        }

        return parseInteger(normalizedValue, fieldName);
    }

    private String parseOptionalText(String value) {
        String normalizedValue = value.trim();

        if (normalizedValue.isEmpty()) {
            return null;
        }

        return normalizedValue;
    }

    private IllegalArgumentException invalidLine(
            Path filePath, int lineNumber, String line, IllegalArgumentException cause) {

        return new IllegalArgumentException(
                "Invalid requirement definition in "
                        + filePath
                        + " at line "
                        + lineNumber
                        + ": "
                        + line
                        + ". "
                        + cause.getMessage(),
                cause);
    }

    private record ParsedRequirement(String setId, RequirementDefinition definition) {}

    private static final class RequirementSetDraft {

        private final String setId;
        private final int firstLineNumber;
        private final String firstLineContent;
        private final Map<Integer, RequirementDefinition> definitionsByOrder;

        private RequirementSetDraft(String setId, int firstLineNumber, String firstLineContent) {

            this.setId = setId;
            this.firstLineNumber = firstLineNumber;
            this.firstLineContent = firstLineContent;
            this.definitionsByOrder = new LinkedHashMap<>();
        }

        private void add(RequirementDefinition definition, int lineNumber, String lineContent) {

            int order = definition.getOrder();

            if (definitionsByOrder.containsKey(order)) {
                throw new IllegalArgumentException(
                        "Requirement set "
                                + setId
                                + " repeats order "
                                + order
                                + " at line "
                                + lineNumber
                                + ": "
                                + lineContent);
            }

            definitionsByOrder.put(order, definition);
        }

        private RequirementSetDefinition build() {
            return new RequirementSetDefinition(
                    setId, new ArrayList<>(definitionsByOrder.values()));
        }

        private String getSetId() {
            return setId;
        }

        private int getFirstLineNumber() {
            return firstLineNumber;
        }

        private String getFirstLineContent() {
            return firstLineContent;
        }
    }
}
