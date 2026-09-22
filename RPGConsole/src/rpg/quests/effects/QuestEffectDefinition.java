package rpg.quests.effects;

import java.util.Optional;
import java.util.OptionalInt;

// Stores one immutable permanent effect produced by quest progression.
public final class QuestEffectDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final OptionalInt quantity;
    private final String questId;
    private final int step;
    private final QuestEffectTrigger trigger;
    private final int order;
    private final QuestEffectType effectType;
    private final String targetId;
    private final Optional<String> displayText;

    public QuestEffectDefinition(
            String questId,
            int step,
            QuestEffectTrigger trigger,
            int order,
            QuestEffectType effectType,
            String targetId,
            OptionalInt quantity,
            Optional<String> displayText) {
        this.questId = validateId(questId, "Quest");

        if (trigger == null) {
            throw new IllegalArgumentException("Quest effect trigger cannot be null.");
        }

        validateStep(step, trigger);

        if (order <= 0) {
            throw new IllegalArgumentException("Quest effect order must be positive.");
        }

        if (effectType == null) {
            throw new IllegalArgumentException("Quest effect type cannot be null.");
        }

        if (displayText == null) {
            throw new IllegalArgumentException("Quest effect display text cannot be null.");
        }

        this.step = step;
        this.trigger = trigger;
        this.order = order;
        this.effectType = effectType;
        this.targetId = validateId(targetId, "Quest effect target");
        this.quantity = validateQuantity(effectType, quantity);
        this.displayText =
                displayText.map(
                        value -> {
                            if (value.isBlank()) {
                                throw new IllegalArgumentException(
                                        "Quest effect display text cannot be blank when present.");
                            }

                            return value.trim();
                        });
    }

    private static void validateStep(int step, QuestEffectTrigger trigger) {
        if (trigger == QuestEffectTrigger.STEP_COMPLETE) {
            if (step <= 0) {
                throw new IllegalArgumentException(
                        "STEP_COMPLETE effects require a positive quest step.");
            }

            return;
        }

        if (step != 0) {
            throw new IllegalArgumentException(trigger + " effects must use quest step 0.");
        }
    }

    private static String validateId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
        }

        String trimmedValue = value.trim();

        if (!trimmedValue.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    fieldName
                            + " ID must use lowercase letters, numbers and single underscores: "
                            + trimmedValue);
        }

        return trimmedValue;
    }

    private static OptionalInt validateQuantity(
            QuestEffectType effectType,
            OptionalInt quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException(
                    "Quest effect quantity cannot be null.");
        }

        if (effectType == QuestEffectType.GRANT_ITEM) {
            if (quantity.isEmpty() || quantity.getAsInt() <= 0) {
                throw new IllegalArgumentException(
                        "GRANT_ITEM effect requires a positive quantity.");
            }

            return quantity;
        }

        if (quantity.isPresent()) {
            throw new IllegalArgumentException(
                    effectType + " effect cannot define a quantity.");
        }

        return OptionalInt.empty();
    }

    public String getQuestId() {
        return questId;
    }

    public int getStep() {
        return step;
    }

    public QuestEffectTrigger getTrigger() {
        return trigger;
    }

    public int getOrder() {
        return order;
    }

    public QuestEffectType getEffectType() {
        return effectType;
    }

    public String getTargetId() {
        return targetId;
    }

    public Optional<String> getDisplayText() {
        return displayText;
    }

    public OptionalInt getQuantity() {
        return quantity;
    }
}
