package rpg.enemies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rpg.items.DropEntry;
import rpg.items.Item;
import rpg.items.ItemCatalog;

// Loads enemy item drops from an external text file.
public final class EnemyDropLoader {

	public EnemyDropCatalog load(Path filePath, EnemyCatalog enemyCatalog) throws IOException {
		EnemyDropCatalog dropCatalog = new EnemyDropCatalog();

		List<String> lines = Files.readAllLines(filePath);

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index).trim();

			if (line.isBlank() || line.startsWith("#")) {
				continue;
			}

			try {
				registerDrop(line, enemyCatalog, dropCatalog);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("Invalid enemy drop at line " + (index + 1) + ": " + line,
						exception);
			}
		}

		return dropCatalog;
	}

	private void registerDrop(String line, EnemyCatalog enemyCatalog, EnemyDropCatalog dropCatalog) {
		String[] fields = line.split(";", -1);

		if (fields.length != 5) {
			throw new IllegalArgumentException("Expected 5 fields.");
		}

		String enemyId = fields[0].trim();
		String itemId = fields[1].trim();

		int dropChance = Integer.parseInt(fields[2].trim());

		int minimumQuantity = Integer.parseInt(fields[3].trim());

		int maximumQuantity = Integer.parseInt(fields[4].trim());

		// Verifies that the referenced enemy exists.
		enemyCatalog.getById(enemyId);

		Item item = ItemCatalog.getById(itemId);

		DropEntry dropEntry = new DropEntry(item, dropChance, minimumQuantity, maximumQuantity);

		dropCatalog.register(enemyId, dropEntry);
	}
}