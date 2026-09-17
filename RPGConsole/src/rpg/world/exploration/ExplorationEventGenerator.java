package rpg.world.exploration;

import java.util.Objects;
import java.util.Random;

// Selects a random event using the current exploration settings.
public final class ExplorationEventGenerator {

    private final Random random;

    public ExplorationEventGenerator(Random random) {
        this.random = Objects.requireNonNull(
                random,
                "Random generator cannot be null."
        );
    }

    public ExplorationEventType generate(
            ExplorationSettings settings,
            boolean specialArea,
            boolean merchantAvailable,
            boolean epicEncounterAvailable
    ) {
        int roll = random.nextInt(100) + 1;

        int accumulatedChance =
                settings.getBattleChance();

        if (roll <= accumulatedChance) {
            return ExplorationEventType.BATTLE;
        }

        accumulatedChance +=
                settings.getRewardChance();

        if (roll <= accumulatedChance) {
            return ExplorationEventType.REWARD;
        }

        accumulatedChance +=
                settings.getMerchantChance();

        if (roll <= accumulatedChance) {
            if (merchantAvailable) {
                return ExplorationEventType.MERCHANT;
            }

            return specialArea
                    ? ExplorationEventType.REWARD
                    : ExplorationEventType.NOTHING;
        }

        accumulatedChance +=
                settings.getEpicEncounterChance();

        if (roll <= accumulatedChance) {
            if (epicEncounterAvailable) {
                return ExplorationEventType.EPIC_ENCOUNTER;
            }

            return ExplorationEventType.BATTLE;
        }

        return specialArea
                ? ExplorationEventType.REWARD
                : ExplorationEventType.NOTHING;
    }


}
