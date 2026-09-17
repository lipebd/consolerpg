package rpg.quests;

import java.util.List;

// Groups ordered quest changes and completion failures from one resolution.
public final class QuestProgressResult {

    private final List<QuestProgressUpdate> updates;
    private final List<String> effectDisplayTexts;
    private final List<QuestCompletionFailure> completionFailures;

    QuestProgressResult(List<QuestProgressUpdate> updates) {
        this(updates, List.of(), List.of());
    }

    QuestProgressResult(
            List<QuestProgressUpdate> updates,
            List<String> effectDisplayTexts,
            List<QuestCompletionFailure> completionFailures) {
        validateEntries(updates, "Quest progress updates");
        validateEntries(effectDisplayTexts, "Quest effect display texts");
        validateEntries(completionFailures, "Quest completion failures");

        for (String displayText : effectDisplayTexts) {
            if (displayText.isBlank()) {
                throw new IllegalArgumentException(
                        "Quest effect display texts cannot contain blank entries.");
            }
        }

        this.updates = List.copyOf(updates);
        this.effectDisplayTexts = List.copyOf(effectDisplayTexts);
        this.completionFailures = List.copyOf(completionFailures);
    }

    private static void validateEntries(List<?> entries, String fieldName) {
        if (entries == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null.");
        }

        for (Object entry : entries) {
            if (entry == null) {
                throw new IllegalArgumentException(fieldName + " cannot contain null entries.");
            }
        }
    }

    public List<QuestProgressUpdate> getUpdates() {
        return updates;
    }

    public List<String> getEffectDisplayTexts() {
        return effectDisplayTexts;
    }

    public List<QuestCompletionFailure> getCompletionFailures() {
        return completionFailures;
    }

    public boolean hasUpdates() {
        return !updates.isEmpty();
    }

    public boolean hasEffectDisplayTexts() {
        return !effectDisplayTexts.isEmpty();
    }

    public boolean hasCompletionFailures() {
        return !completionFailures.isEmpty();
    }
}
