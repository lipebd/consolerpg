package rpg.narrative;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import rpg.requirements.RequirementCatalog;

// Loads fixed NPC placements from external data.
public final class NpcPlacementLoader {

    private static final int EXPECTED_FIELD_COUNT = 3;

    public NpcPlacementCatalog load(
            Path filePath,
            NpcCatalog npcCatalog,
            NarrativeLocationCatalog locationCatalog,
            RequirementCatalog requirementCatalog)
            throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("NPC placement file path cannot be null.");
        }

        if (npcCatalog == null) {
            throw new IllegalArgumentException("NPC catalog cannot be null.");
        }

        if (locationCatalog == null) {
            throw new IllegalArgumentException("Narrative location catalog cannot be null.");
        }

        if (requirementCatalog == null) {
            throw new IllegalArgumentException("Requirement catalog cannot be null.");
        }

        NpcPlacementCatalog placementCatalog = new NpcPlacementCatalog();

        List<String> lines = Files.readAllLines(filePath);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            try {
                NpcPlacementDefinition definition = parseDefinition(line);

                validateReferences(definition, npcCatalog, locationCatalog, requirementCatalog);

                placementCatalog.register(definition);

            } catch (IllegalArgumentException exception) {
                throw invalidDefinition(filePath, index + 1, line, exception);
            }
        }

        return placementCatalog;
    }

    private NpcPlacementDefinition parseDefinition(String line) {
        String[] fields = line.split(";", -1);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected "
                            + EXPECTED_FIELD_COUNT
                            + " fields but found "
                            + fields.length
                            + ".");
        }

        String requirementSetValue = fields[2].trim();

        Optional<String> requirementSetId =
                requirementSetValue.isEmpty() ? Optional.empty() : Optional.of(requirementSetValue);

        return new NpcPlacementDefinition(
                fields[0].trim(),
                fields[1].trim(),
                requirementSetId);
    }

    private void validateReferences(
            NpcPlacementDefinition definition,
            NpcCatalog npcCatalog,
            NarrativeLocationCatalog locationCatalog,
            RequirementCatalog requirementCatalog) {
        npcCatalog.getById(definition.getNpcId());

        locationCatalog.getById(definition.getNarrativeLocationId());

        definition.getRequirementSetId().ifPresent(requirementCatalog::getById);
    }

    private IllegalArgumentException invalidDefinition(
            Path filePath, int lineNumber, String line, IllegalArgumentException cause) {
        return new IllegalArgumentException(
                "Invalid NPC placement definition in "
                        + filePath
                        + " at line "
                        + lineNumber
                        + ": "
                        + line
                        + ". "
                        + cause.getMessage(),
                cause);
    }
}
