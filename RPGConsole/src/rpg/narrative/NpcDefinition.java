package rpg.narrative;

// Stores the immutable identity of one ordinary NPC.
public final class NpcDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String id;
    private final String name;
    private final String description;
    private final String defaultDialogueId;

    public NpcDefinition(String id, String name, String description, String defaultDialogueId) {
        this.id = validateId(id, "NPC");
        this.name = NarrativeTextProcessor.requireLiteral(name, "NPC name");
        this.description = NarrativeTextProcessor.decodeLiteral(description, "NPC description");
        this.defaultDialogueId = validateId(defaultDialogueId, "NPC default dialogue");
    }

    private static String validateId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
        }

        String normalizedValue = value.trim();

        if (!normalizedValue.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    fieldName
                            + " ID must use lowercase letters, numbers and single underscores: "
                            + normalizedValue);
        }

        return normalizedValue;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultDialogueId() {
        return defaultDialogueId;
    }
}
