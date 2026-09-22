package rpg.classes.paths;

import java.util.Optional;

// Describes one expected class-path transition validation failure.
public final class ClassPathTransitionFailure {

    private final ClassPathTransitionFailureType failureType;
    private final Optional<String> itemId;
    private final int requiredQuantity;
    private final int availableQuantity;

    private ClassPathTransitionFailure(
            ClassPathTransitionFailureType failureType,
            Optional<String> itemId,
            int requiredQuantity,
            int availableQuantity) {
        if (failureType == null) {
            throw new IllegalArgumentException(
                    "Class path transition failure type cannot be null.");
        }

        if (itemId == null) {
            throw new IllegalArgumentException(
                    "Class path transition failure item ID cannot be null.");
        }

        this.failureType = failureType;
        this.itemId = itemId;
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;
    }

    public static ClassPathTransitionFailure simple(ClassPathTransitionFailureType failureType) {
        return new ClassPathTransitionFailure(failureType, Optional.empty(), 0, 0);
    }

    public static ClassPathTransitionFailure itemCapacityExceeded(
            String itemId, int requiredQuantity, int availableQuantity) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("Capacity failure item ID cannot be empty.");
        }

        if (requiredQuantity <= 0) {
            throw new IllegalArgumentException("Required item quantity must be positive.");
        }

        if (availableQuantity < 0) {
            throw new IllegalArgumentException("Available item capacity cannot be negative.");
        }

        return new ClassPathTransitionFailure(
                ClassPathTransitionFailureType.ITEM_CAPACITY_EXCEEDED,
                Optional.of(itemId.trim()),
                requiredQuantity,
                availableQuantity);
    }

    public ClassPathTransitionFailureType getFailureType() {
        return failureType;
    }

    public Optional<String> getItemId() {
        return itemId;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
