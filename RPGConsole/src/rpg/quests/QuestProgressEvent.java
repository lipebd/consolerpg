package rpg.quests;

import java.util.Optional;

// Represents one confirmed gameplay event submitted to quest progress.
public final class QuestProgressEvent {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final QuestProgressEventType eventType;
    private final Optional<String> targetId;
    private final int quantity;

    public QuestProgressEvent(
            QuestProgressEventType eventType, Optional<String> targetId, int quantity) {
        if (eventType == null) {
            throw new IllegalArgumentException("Quest progress event type cannot be null.");
        }

        if (targetId == null) {
            throw new IllegalArgumentException("Quest progress event target cannot be null.");
        }

        this.eventType = eventType;
        this.targetId = validateTarget(eventType, targetId);
        this.quantity = validateQuantity(eventType, quantity);
    }

    private static Optional<String> validateTarget(
            QuestProgressEventType eventType, Optional<String> targetId) {
        if (!eventType.isTargetRequired()) {
            if (targetId.isPresent()) {
                throw new IllegalArgumentException(eventType + " event cannot define a target ID.");
            }

            return Optional.empty();
        }

        if (targetId.isEmpty()) {
            throw new IllegalArgumentException(eventType + " event must define a target ID.");
        }

        String normalizedTargetId = targetId.orElseThrow().trim();

        if (!normalizedTargetId.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    "Quest progress event target ID must use lowercase letters, numbers and single underscores: "
                            + normalizedTargetId);
        }

        return Optional.of(normalizedTargetId);
    }

    private static int validateQuantity(QuestProgressEventType eventType, int quantity) {
        if (eventType.isVariableQuantityAllowed()) {
            if (quantity <= 0) {
                throw new IllegalArgumentException(eventType + " event quantity must be positive.");
            }

            return quantity;
        }

        if (quantity != 1) {
            throw new IllegalArgumentException(eventType + " event quantity must be exactly one.");
        }

        return quantity;
    }

    public QuestProgressEventType getEventType() {
        return eventType;
    }

    public Optional<String> getTargetId() {
        return targetId;
    }

    public int getQuantity() {
        return quantity;
    }
}
