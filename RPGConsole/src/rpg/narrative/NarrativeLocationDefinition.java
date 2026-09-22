package rpg.narrative;

import java.util.Optional;

// Stores one immutable city narrative-location definition.
public final class NarrativeLocationDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String id;
    private final String cityId;
    private final String name;
    private final String description;
    private final Optional<String> requirementSetId;

    public NarrativeLocationDefinition(
            String id,
            String cityId,
            String name,
            String description,
            Optional<String> requirementSetId) {
        this.id = validateId(id, "Narrative location");
        this.cityId = validateId(cityId, "Narrative location city");
        this.name = NarrativeTextProcessor.requireLiteral(name, "Narrative location name");
        this.description =
                NarrativeTextProcessor.decodeLiteral(description, "Narrative location description");

        if (requirementSetId == null) {
            throw new IllegalArgumentException(
                    "Narrative location requirement set cannot be null.");
        }

        this.requirementSetId =
                requirementSetId.map(
                        value -> validateId(value, "Narrative location requirement set"));
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

    public String getCityId() {
        return cityId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Optional<String> getRequirementSetId() {
        return requirementSetId;
    }
}
