package rpg.narrative;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import rpg.requirements.RequirementCatalog;
import rpg.world.scenarios.ScenarioDefinition;
import rpg.world.scenarios.ScenarioDefinitionCatalog;
import rpg.world.scenarios.ScenarioType;

// Loads city narrative-location definitions from external data.
public final class NarrativeLocationLoader {

    private static final int EXPECTED_FIELD_COUNT = 5;

    public NarrativeLocationCatalog load(
            Path filePath,
            ScenarioDefinitionCatalog scenarioDefinitionCatalog,
            RequirementCatalog requirementCatalog)
            throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("Narrative location file path cannot be null.");
        }

        if (scenarioDefinitionCatalog == null) {
            throw new IllegalArgumentException("Scenario definition catalog cannot be null.");
        }

        if (requirementCatalog == null) {
            throw new IllegalArgumentException("Requirement catalog cannot be null.");
        }

        NarrativeLocationCatalog locationCatalog = new NarrativeLocationCatalog();

        List<String> lines = Files.readAllLines(filePath);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            try {
                NarrativeLocationDefinition definition = parseDefinition(line);

                validateReferences(definition, scenarioDefinitionCatalog, requirementCatalog);

                locationCatalog.register(definition);

            } catch (IllegalArgumentException exception) {
                throw invalidDefinition(filePath, index + 1, line, exception);
            }
        }

        return locationCatalog;
    }

    private NarrativeLocationDefinition parseDefinition(String line) {
        String[] fields = line.split(";", -1);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected "
                            + EXPECTED_FIELD_COUNT
                            + " fields but found "
                            + fields.length
                            + ".");
        }

        String requirementSetValue = fields[4].trim();

        Optional<String> requirementSetId =
                requirementSetValue.isEmpty() ? Optional.empty() : Optional.of(requirementSetValue);

        return new NarrativeLocationDefinition(
                fields[0].trim(),
                fields[1].trim(),
                fields[2],
                fields[3],
                requirementSetId);
    }

    private void validateReferences(
            NarrativeLocationDefinition definition,
            ScenarioDefinitionCatalog scenarioDefinitionCatalog,
            RequirementCatalog requirementCatalog) {
        ScenarioDefinition cityDefinition =
                scenarioDefinitionCatalog.getById(definition.getCityId());

        if (cityDefinition.getType() != ScenarioType.CITY) {
            throw new IllegalArgumentException(
                    "Narrative location city must reference a CITY scenario: "
                            + definition.getCityId());
        }

        definition.getRequirementSetId().ifPresent(requirementCatalog::getById);
    }

    private IllegalArgumentException invalidDefinition(
            Path filePath, int lineNumber, String line, IllegalArgumentException cause) {
        return new IllegalArgumentException(
                "Invalid narrative location definition in "
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
