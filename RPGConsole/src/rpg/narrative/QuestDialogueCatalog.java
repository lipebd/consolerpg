package rpg.narrative;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Stores quest dialogue associations in stable declaration order.
public final class QuestDialogueCatalog {

    private final Map<QuestDialogueKey, QuestDialogueDefinition> definitionsByKey;

    public QuestDialogueCatalog() {
        definitionsByKey = new LinkedHashMap<>();
    }

    public void register(QuestDialogueDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Quest dialogue definition cannot be null.");
        }

        QuestDialogueKey key = QuestDialogueKey.from(definition);

        if (definitionsByKey.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Quest dialogue association is duplicated: " + describe(key));
        }

        definitionsByKey.put(key, definition);
    }

    public Optional<QuestDialogueDefinition> find(
            String questId, int step, QuestDialogueContext context) {
        if (questId == null || questId.isBlank()) {
            throw new IllegalArgumentException("Quest ID cannot be empty.");
        }

        if (context == null) {
            throw new IllegalArgumentException("Quest dialogue context cannot be null.");
        }

        return Optional.ofNullable(
                definitionsByKey.get(new QuestDialogueKey(questId.trim(), step, context)));
    }

    public List<QuestDialogueDefinition> getByQuestId(String questId) {
        if (questId == null || questId.isBlank()) {
            throw new IllegalArgumentException("Quest ID cannot be empty.");
        }

        String normalizedQuestId = questId.trim();
        List<QuestDialogueDefinition> definitions = new ArrayList<>();

        for (QuestDialogueDefinition definition : definitionsByKey.values()) {
            if (definition.getQuestId().equals(normalizedQuestId)) {
                definitions.add(definition);
            }
        }

        return List.copyOf(definitions);
    }

    public List<QuestDialogueDefinition> getAll() {
        return List.copyOf(definitionsByKey.values());
    }

    private static String describe(QuestDialogueKey key) {
        return key.questId() + ", step " + key.step() + ", " + key.context();
    }

    private record QuestDialogueKey(String questId, int step, QuestDialogueContext context) {
        private static QuestDialogueKey from(QuestDialogueDefinition definition) {
            return new QuestDialogueKey(
                    definition.getQuestId(), definition.getStep(), definition.getContext());
        }
    }
}
