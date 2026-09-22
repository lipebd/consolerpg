package rpg.narrative;

import java.util.Optional;

// Stores one immutable fixed NPC placement.
public final class NpcPlacementDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String npcId;
    private final String narrativeLocationId;
    private final Optional<String> requirementSetId;

    public NpcPlacementDefinition(
            String npcId, String narrativeLocationId, Optional<String> requirementSetId) {
        this.npcId = validateId(npcId, "Placed NPC");
        this.narrativeLocationId = validateId(narrativeLocationId, "NPC narrative location");

        if (requirementSetId == null) {
            throw new IllegalArgumentException("NPC placement requirement set cannot be null.");
        }

        this.requirementSetId =
                requirementSetId.map(value -> validateId(value, "NPC placement requirement set"));
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

    public String getNpcId() {
        return npcId;
    }

    public String getNarrativeLocationId() {
        return narrativeLocationId;
    }

    public Optional<String> getRequirementSetId() {
        return requirementSetId;
    }
}
