package rpg.items;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RewardModifierItemLoader {

	private static final int EXPECTED_FIELD_COUNT = 4;

	public Map<String, RewardModifierItemDefinition> load(Path filePath, ItemDefinitionCatalog itemDefinitions)
			throws IOException {
		Map<String, RewardModifierItemDefinition> definitions = new LinkedHashMap<>();

		int lineNumber = 0;

		for (String rawLine : Files.readAllLines(filePath)) {
			lineNumber++;
			String line = rawLine.trim();

			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			try {
				String[] fields = line.split(";", -1);

				if (fields.length != EXPECTED_FIELD_COUNT) {
					throw new IllegalArgumentException("Expected four fields but found " + fields.length + ".");
				}

				String itemId = fields[0].trim();
				ItemDefinition item = itemDefinitions.getById(itemId);

				if (item.getCategory() != ItemCategory.CONSUMABLE) {
					throw new IllegalArgumentException("Reward modifier item must be CONSUMABLE: " + itemId);
				}

				RewardModifierItemDefinition definition = new RewardModifierItemDefinition(
						itemId,
						Integer.parseInt(fields[1].trim()),
						Integer.parseInt(fields[2].trim()),
						Integer.parseInt(fields[3].trim())
				);

				if (definitions.putIfAbsent(itemId, definition) != null) {
					throw new IllegalArgumentException("Duplicate reward modifier item: " + itemId);
				}
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException(
						"Invalid reward modifier item at line " + lineNumber + ": " + line + ". "
								+ exception.getMessage(),
						exception
				);
			}
		}

		return Map.copyOf(definitions);
	}
}