package rpg.flags;

// Stores the immutable external definition of one permanent flag.
public final class FlagDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String id;
    private final String description;

    public FlagDefinition(String id, String description) {
        this.id = validateId(id);
        this.description = validateDescription(description);
    }

    private static String validateId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Flag ID cannot be empty.");
        }

        String normalizedValue = value.trim();

        if (!normalizedValue.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    "Flag ID must use lowercase letters, numbers and single underscores: "
                            + normalizedValue);
        }

        return normalizedValue;
    }

    private static String validateDescription(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Flag description cannot be empty.");
        }

        return value.trim();
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
