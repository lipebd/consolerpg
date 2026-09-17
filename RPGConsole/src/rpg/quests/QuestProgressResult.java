package rpg.quests;

import java.util.List;

// Groups ordered quest-progress changes from one gameplay resolution.
public final class QuestProgressResult {

    private final List<QuestProgressUpdate> updates;

    QuestProgressResult(List<QuestProgressUpdate> updates) {
        if (updates == null) {
            throw new IllegalArgumentException("Quest progress updates cannot be null.");
        }

        for (QuestProgressUpdate update : updates) {
            if (update == null) {
                throw new IllegalArgumentException(
                        "Quest progress updates cannot contain null entries.");
            }
        }

        this.updates = List.copyOf(updates);
    }

    public List<QuestProgressUpdate> getUpdates() {
        return updates;
    }

    public boolean hasUpdates() {
        return !updates.isEmpty();
    }
}
