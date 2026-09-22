package rpg.quests.rewards;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rpg.items.ItemCatalog;
import rpg.quests.QuestCatalog;

// Loads material quest rewards from external data.
public final class QuestRewardLoader {

    private static final int FIELD_COUNT = 5;

    public QuestRewardCatalog load(
            Path filePath, QuestCatalog questCatalog, ItemCatalog itemCatalog) throws IOException {

        if (filePath == null) {
            throw new IllegalArgumentException("Quest reward file path cannot be null.");
        }

        if (questCatalog == null) {
            throw new IllegalArgumentException("Quest catalog cannot be null.");
        }

        if (itemCatalog == null) {
            throw new IllegalArgumentException("Item catalog cannot be null.");
        }

        Map<String, List<RewardDraft>> draftsByQuestId = new LinkedHashMap<>();

        List<String> lines = Files.readAllLines(filePath);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int lineNumber = index + 1;

            try {
                RewardDraft draft = parseDraft(line, lineNumber, questCatalog, itemCatalog);

                draftsByQuestId
                        .computeIfAbsent(
                                draft.definition().getQuestId(), ignored -> new ArrayList<>())
                        .add(draft);
            } catch (IllegalArgumentException exception) {
                throw invalidDefinition(filePath, lineNumber, line, exception);
            }
        }

        return buildCatalog(filePath, draftsByQuestId);
    }

    private RewardDraft parseDraft(
            String line, int lineNumber, QuestCatalog questCatalog, ItemCatalog itemCatalog) {

        String[] fields = line.split(";", -1);

        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + FIELD_COUNT + " fields but found " + fields.length + ".");
        }

        String questId = fields[0].trim();
        int order = parseInteger(fields[1], "quest reward order");

        QuestRewardType rewardType = parseRewardType(fields[2]);

        String rewardId = parseOptionalText(fields[3]);

        int quantity = parseInteger(fields[4], "quest reward quantity");

        QuestRewardDefinition definition =
                new QuestRewardDefinition(questId, order, rewardType, rewardId, quantity);

        questCatalog.getById(definition.getQuestId());

        if (definition.getRewardType() == QuestRewardType.ITEM) {

            itemCatalog.getById(definition.getRewardId().orElseThrow());
        }

        return new RewardDraft(definition, lineNumber, line);
    }

    private QuestRewardCatalog buildCatalog(
            Path filePath, Map<String, List<RewardDraft>> draftsByQuestId) {

        QuestRewardCatalog catalog = new QuestRewardCatalog();

        for (List<RewardDraft> questDrafts : draftsByQuestId.values()) {

            questDrafts.sort(Comparator.comparingInt(draft -> draft.definition().getOrder()));

            for (RewardDraft draft : questDrafts) {
                try {
                    catalog.register(draft.definition());
                } catch (IllegalArgumentException exception) {
                    throw invalidDefinition(
                            filePath, draft.lineNumber(), draft.lineContent(), exception);
                }
            }
        }

        return catalog;
    }

    private QuestRewardType parseRewardType(String value) {

        String normalizedValue = value.trim();

        try {
            return QuestRewardType.valueOf(normalizedValue);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unknown quest reward type: " + normalizedValue, exception);
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

    private String parseOptionalText(String value) {
        String normalizedValue = value.trim();

        if (normalizedValue.isEmpty()) {
            return null;
        }

        return normalizedValue;
    }

    private IllegalArgumentException invalidDefinition(
            Path filePath, int lineNumber, String line, IllegalArgumentException cause) {

        return new IllegalArgumentException(
                "Invalid quest reward definition in "
                        + filePath
                        + " at line "
                        + lineNumber
                        + ": "
                        + line
                        + ". "
                        + cause.getMessage(),
                cause);
    }

    private record RewardDraft(
            QuestRewardDefinition definition, int lineNumber, String lineContent) {}
}
