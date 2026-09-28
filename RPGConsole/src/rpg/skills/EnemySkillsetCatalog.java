package rpg.skills;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Stores enemy-to-skill links and resolves the pool eligible at a given level.
public final class EnemySkillsetCatalog {

	private final Map<String, List<EnemySkillsetEntry>> entriesByEnemyId;

	public EnemySkillsetCatalog() {
		entriesByEnemyId = new LinkedHashMap<>();
	}

	public void register(EnemySkillsetEntry entry) {
		Objects.requireNonNull(entry, "Enemy skillset entry cannot be null.");

		List<EnemySkillsetEntry> enemyEntries = entriesByEnemyId
				.computeIfAbsent(entry.enemyId(), ignored -> new ArrayList<>());

		boolean alreadyRegistered = enemyEntries.stream()
				.anyMatch(existing -> existing.skillDefinition().getId().equals(entry.skillDefinition().getId()));

		if (alreadyRegistered) {
			throw new IllegalArgumentException(
					"Enemy skillset link is already registered: " + entry.enemyId() + " -> "
							+ entry.skillDefinition().getId()
			);
		}

		enemyEntries.add(entry);
	}

	public List<EnemySkillsetEntry> getEntriesByEnemyId(String enemyId) {
		if (enemyId == null || enemyId.isBlank()) {
			throw new IllegalArgumentException("Enemy skillset lookup ID cannot be empty.");
		}

		return List.copyOf(entriesByEnemyId.getOrDefault(enemyId.trim(), List.of()));
	}

	public List<EnemySkillDefinition> getEligibleSkills(String enemyId, int enemyLevel) {
		if (enemyLevel < 1) {
			throw new IllegalArgumentException("Enemy skillset lookup level must be positive.");
		}

		return getEntriesByEnemyId(enemyId).stream().filter(entry -> entry.minimumLevel() <= enemyLevel)
				.map(EnemySkillsetEntry::skillDefinition).toList();
	}

	public List<EnemySkillsetEntry> getAll() {
		return entriesByEnemyId.values().stream().flatMap(List::stream).toList();
	}
}