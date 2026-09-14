package rpg.characters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

// Loads and validates names available for random character naming.
public final class CharacterNameLoader {

	public CharacterNameCatalog load(Path filePath) throws IOException {
		Objects.requireNonNull(filePath, "Character-name file path cannot be null.");

		CharacterNameCatalog catalog = new CharacterNameCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
			String line = lines.get(lineIndex);

			if (line.isBlank() || line.stripLeading().startsWith("#")) {
				continue;
			}

			try {
				catalog.register(line);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid character name at line " + (lineIndex + 1) + " in " + filePath + ": "
								+ exception.getMessage(),
						exception
				);
			}
		}

		if (catalog.size() < Party.MAXIMUM_SIZE) {
			throw new IllegalArgumentException(
					"Character-name catalog must contain at least " + Party.MAXIMUM_SIZE
							+ " unique valid names, but found " + catalog.size() + "."
			);
		}

		return catalog;
	}
}