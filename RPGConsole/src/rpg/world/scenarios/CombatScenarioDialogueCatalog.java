package rpg.world.scenarios;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class CombatScenarioDialogueCatalog {

    private final Map<Key, CombatScenarioDialogueDefinition> definitions =
            new LinkedHashMap<>();

    public void register(CombatScenarioDialogueDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException(
                    "Combat scenario dialogue cannot be null.");
        }

        Key key = new Key(
                definition.scenarioId(),
                definition.trigger(),
                definition.questId().orElse(""),
                definition.step());

        if (definitions.putIfAbsent(key, definition) != null) {
            throw new IllegalArgumentException(
                    "Combat scenario dialogue is duplicated: " + key);
        }
    }

    public Optional<CombatScenarioDialogueDefinition> find(
            String scenarioId,
            CombatScenarioDialogueDefinition.Trigger trigger,
            String questId,
            int step) {
        return Optional.ofNullable(
                definitions.get(new Key(scenarioId, trigger, questId, step)));
    }

    public Optional<CombatScenarioDialogueDefinition> findGeneral(
            String scenarioId,
            CombatScenarioDialogueDefinition.Trigger trigger) {
        return find(scenarioId, trigger, "", 0);
    }

    private record Key(
            String scenarioId,
            CombatScenarioDialogueDefinition.Trigger trigger,
            String questId,
            int step) {}
}