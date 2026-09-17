package rpg.flags;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Loads permanent-flag definitions from external data.
public final class FlagLoader {

    private static final int FIELD_COUNT = 2;

    public FlagCatalog load(Path filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("Flag file path cannot be null.");
        }

        FlagCatalog catalog = new FlagCatalog();
        List<String> lines = Files.readAllLines(filePath);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            try {
                FlagDefinition flagDefinition = parseDefinition(line);

                catalog.register(flagDefinition);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Invalid flag definition in "
                                + filePath
                                + " at line "
                                + (index + 1)
                                + ": "
                                + line
                                + ". "
                                + exception.getMessage(),
                        exception);
            }
        }

        return catalog;
    }

    private FlagDefinition parseDefinition(String line) {
        String[] fields = line.split(";", -1);

        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + FIELD_COUNT + " fields but found " + fields.length + ".");
        }

        String id = fields[0].trim();
        String description = parseDescription(fields[1]);

        return new FlagDefinition(id, description);
    }

    private String parseDescription(String value) {
        String normalizedValue = value.trim();

        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException("Flag description cannot be empty.");
        }

        StringBuilder description = new StringBuilder();

        for (int index = 0; index < normalizedValue.length(); index++) {
            char character = normalizedValue.charAt(index);

            if (character != '\\') {
                description.append(character);
                continue;
            }

            if (index + 1 >= normalizedValue.length() || normalizedValue.charAt(index + 1) != 'n') {
                throw new IllegalArgumentException(
                        "Flag description can only use backslashes as "
                                + "the \\\\n line-break sequence.");
            }

            description.append('\n');
            index++;
        }

        return description.toString();
    }
}
