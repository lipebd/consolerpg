package rpg.enemies;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import rpg.items.DropEntry;

// Stores item drops grouped by enemy ID.
public final class EnemyDropCatalog {

	private final Map<String, List<DropEntry>> dropsByEnemyId;

	public EnemyDropCatalog() {
		this.dropsByEnemyId = new LinkedHashMap<>();
	}

	public void register(String enemyId, DropEntry dropEntry) {
		String normalizedEnemyId = normalizeEnemyId(enemyId);

		Objects.requireNonNull(dropEntry, "Drop entry cannot be null.");

		List<DropEntry> enemyDrops = dropsByEnemyId.computeIfAbsent(normalizedEnemyId, id -> new ArrayList<>());

		boolean itemAlreadyRegistered = enemyDrops.stream()
				.anyMatch(existingDrop -> existingDrop.getItem().equals(dropEntry.getItem()));

		if (itemAlreadyRegistered) {
			throw new IllegalArgumentException("Item drop already registered for enemy: " + normalizedEnemyId + " -> "
					+ dropEntry.getItem().getId());
		}

		enemyDrops.add(dropEntry);
	}

	public List<DropEntry> getDropsByEnemyId(String enemyId) {
		String normalizedEnemyId = normalizeEnemyId(enemyId);

		List<DropEntry> enemyDrops = dropsByEnemyId.getOrDefault(normalizedEnemyId, List.of());

		return List.copyOf(enemyDrops);
	}

	private String normalizeEnemyId(String enemyId) {
		if (enemyId == null || enemyId.isBlank()) {
			throw new IllegalArgumentException("Enemy ID cannot be empty.");
		}

		return enemyId.trim().toLowerCase(Locale.ROOT);
	}
}