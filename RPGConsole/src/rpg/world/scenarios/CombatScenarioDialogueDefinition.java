package rpg.world.scenarios;

import java.util.Optional;

// Associates a combat-scenario event and optional quest step with a dialogue.
public record CombatScenarioDialogueDefinition(
        String scenarioId,
        Trigger trigger,
        Optional<String> questId,
        int step,
        String dialogueId) {

    public enum Trigger {
        BOSS_GATED,
        BOSS_NARRATIVE,
        BOSS_RECURRING
    }

    public CombatScenarioDialogueDefinition {
        if (scenarioId == null || scenarioId.isBlank()) {
            throw new IllegalArgumentException("Scenario ID cannot be empty.");
        }
        if (trigger == null) {
            throw new IllegalArgumentException("Dialogue trigger cannot be null.");
        }
        if (questId == null) {
            throw new IllegalArgumentException("Quest ID cannot be null.");
        }
        if (questId.isPresent() != (step > 0)) {
            throw new IllegalArgumentException(
                    "Quest ID and positive step must be defined together.");
        }
        if (dialogueId == null || dialogueId.isBlank()) {
            throw new IllegalArgumentException("Dialogue ID cannot be empty.");
        }

        scenarioId = scenarioId.trim();
        questId = questId.map(String::trim);
        dialogueId = dialogueId.trim();
    }
}