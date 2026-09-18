package rpg.narrative;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Loads ordinary NPC identity definitions from external data.
public final class NpcLoader {

    private static final int EXPECTED_FIELD_COUNT = 4;

    public NpcCatalog load(Path filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("NPC file path cannot be null.");
        }

        NpcCatalog npcCatalog = new NpcCatalog();
        List<String> lines = Files.readAllLines(filePath);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            try {
                NpcDefinition npcDefinition = parseDefinition(line);

                npcCatalog.register(npcDefinition);

            } catch (IllegalArgumentException exception) {
                throw invalidDefinition(filePath, index + 1, line, exception);
            }
        }

        return npcCatalog;
    }

    private NpcDefinition parseDefinition(String line) {
        String[] fields = line.split(";", -1);

        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected "
                            + EXPECTED_FIELD_COUNT
                            + " fields but found "
                            + fields.length
                            + ".");
        }

        return new NpcDefinition(
                fields[0].trim(),
                fields[1],
                fields[2],
                fields[3].trim());
    }

    private IllegalArgumentException invalidDefinition(
            Path filePath, int lineNumber, String line, IllegalArgumentException cause) {
        return new IllegalArgumentException(
                "Invalid NPC definition in "
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
