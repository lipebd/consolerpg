package rpg.world.exploration;

import java.util.Objects;
import java.util.Random;

// Tracks the mutable state of an exploration session.
public final class ExplorationSession {

    private final ExplorationSettings settings;
    private int currentPosition;

    public ExplorationSession(
            ExplorationSettings settings
    ) {
        this.settings = Objects.requireNonNull(
                settings,
                "Exploration settings cannot be null."
        );

        this.currentPosition = 0;
    }

    public int advance() {
        if (isComplete()) {
            throw new IllegalStateException(
                    "Exploration is already complete."
            );
        }

        currentPosition++;

        return currentPosition;
    }

    public boolean determineSpecialArea(Random random) {
        if (
                currentPosition == 1
                || currentPosition == settings.getLength()
        ) {
            return false;
        }

        int roll = random.nextInt(100) + 1;

        return roll <= settings.getSpecialAreaChance();
    }

    public boolean isComplete() {
        return currentPosition >= settings.getLength();
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public ExplorationSettings getSettings() {
        return settings;
    }
}