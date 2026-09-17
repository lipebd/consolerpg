package rpg.requirements;

import java.util.Optional;
import java.util.OptionalInt;

import rpg.characters.PlayableCharacterLevelRules;
import rpg.items.Inventory;

// Defines one condition inside a requirement set.
public final class RequirementDefinition {

    private final int order;
    private final RequirementType type;
    private final String targetId;
    private final Integer requiredValue;

    public RequirementDefinition(
            int order, RequirementType type, String targetId, Integer requiredValue) {

        if (order <= 0) {
            throw new IllegalArgumentException("Requirement order must be positive: " + order);
        }

        if (type == null) {
            throw new IllegalArgumentException("Requirement type cannot be null.");
        }

        validateFields(type, targetId, requiredValue);

        this.order = order;
        this.type = type;
        this.targetId = targetId;
        this.requiredValue = requiredValue;
    }

    public int getOrder() {
        return order;
    }

    public RequirementType getType() {
        return type;
    }

    public Optional<String> getTargetId() {
        return Optional.ofNullable(targetId);
    }

    public OptionalInt getRequiredValue() {
        if (requiredValue == null) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(requiredValue);
    }

    private static void validateFields(
            RequirementType type, String targetId, Integer requiredValue) {

        switch (type) {
            case LEADER_LEVEL_AT_LEAST -> validateLeaderLevel(targetId, requiredValue);

            case ITEM_QUANTITY_AT_LEAST -> validateItemQuantity(targetId, requiredValue);

            case QUEST_NOT_STARTED, QUEST_ACTIVE, QUEST_COMPLETED, FLAG_SET, FLAG_UNSET ->
                    validateReferenceOnly(type, targetId, requiredValue);
        }
    }

    private static void validateLeaderLevel(String targetId, Integer requiredValue) {

        requireAbsentTarget(RequirementType.LEADER_LEVEL_AT_LEAST, targetId);
        requirePresentValue(RequirementType.LEADER_LEVEL_AT_LEAST, requiredValue);

        PlayableCharacterLevelRules.validateLevel(requiredValue);
    }

    private static void validateItemQuantity(String targetId, Integer requiredValue) {

        requirePresentTarget(RequirementType.ITEM_QUANTITY_AT_LEAST, targetId);
        requirePresentValue(RequirementType.ITEM_QUANTITY_AT_LEAST, requiredValue);

        if (requiredValue <= 0 || requiredValue > Inventory.MAXIMUM_QUANTITY_PER_ITEM) {

            throw new IllegalArgumentException(
                    "ITEM_QUANTITY_AT_LEAST value must be between 1 and "
                            + Inventory.MAXIMUM_QUANTITY_PER_ITEM
                            + ": "
                            + requiredValue);
        }
    }

    private static void validateReferenceOnly(
            RequirementType type, String targetId, Integer requiredValue) {

        requirePresentTarget(type, targetId);

        if (requiredValue != null) {
            throw new IllegalArgumentException(type + " cannot define a required value.");
        }
    }

    private static void requirePresentTarget(RequirementType type, String targetId) {

        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException(type + " requires a target ID.");
        }

        if (!targetId.equals(targetId.trim())) {
            throw new IllegalArgumentException(
                    type + " target ID cannot contain surrounding whitespace: " + targetId);
        }
    }

    private static void requireAbsentTarget(RequirementType type, String targetId) {

        if (targetId != null) {
            throw new IllegalArgumentException(type + " cannot define a target ID.");
        }
    }

    private static void requirePresentValue(RequirementType type, Integer requiredValue) {

        if (requiredValue == null) {
            throw new IllegalArgumentException(type + " requires a value.");
        }
    }
}
