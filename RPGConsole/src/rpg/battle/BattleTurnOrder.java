package rpg.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// Tracks the current position inside one battle round.
public final class BattleTurnOrder {

    private final List<BattleTurnEntry> entries;
    private int currentIndex;

    public BattleTurnOrder(
            List<BattleTurnEntry> entries
    ) {
        Objects.requireNonNull(
                entries,
                "Battle turn entries cannot be null."
        );

        if (entries.isEmpty()) {
            throw new IllegalArgumentException(
                    "Battle turn order cannot be empty."
            );
        }

        this.entries = List.copyOf(entries);
        this.currentIndex = 0;

        skipDefeatedEntries();
    }

    public Optional<BattleTurnEntry> getCurrentEntry() {
        skipDefeatedEntries();

        if (currentIndex >= entries.size()) {
            return Optional.empty();
        }

        return Optional.of(entries.get(currentIndex));
    }

    public Optional<BattleTurnEntry> getNextLivingEntry() {
        skipDefeatedEntries();

        for (int index = currentIndex + 1;
                index < entries.size();
                index++) {

            BattleTurnEntry entry = entries.get(index);

            if (entry.isAlive()) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    public List<BattleTurnEntry> getVisibleEntries(
            int maximumEntries
    ) {
        if (maximumEntries < 1) {
            throw new IllegalArgumentException(
                    "Maximum visible entries must be positive."
            );
        }

        skipDefeatedEntries();

        List<BattleTurnEntry> visibleEntries =
                new ArrayList<>();

        for (int index = currentIndex;
                index < entries.size()
                && visibleEntries.size() < maximumEntries;
                index++) {

            BattleTurnEntry entry = entries.get(index);

            if (entry.isAlive()) {
                visibleEntries.add(entry);
            }
        }

        return List.copyOf(visibleEntries);
    }

    public int getRemainingActionCount() {
        skipDefeatedEntries();

        int remainingActions = 0;

        for (int index = currentIndex;
                index < entries.size();
                index++) {

            if (entries.get(index).isAlive()) {
                remainingActions++;
            }
        }

        return remainingActions;
    }

    public void advance() {
        if (currentIndex < entries.size()) {
            currentIndex++;
        }

        skipDefeatedEntries();
    }

    public boolean isComplete() {
        skipDefeatedEntries();
        return currentIndex >= entries.size();
    }

    private void skipDefeatedEntries() {
        while (
                currentIndex < entries.size()
                && !entries.get(currentIndex).isAlive()
        ) {
            currentIndex++;
        }
    }
}