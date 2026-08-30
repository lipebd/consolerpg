package rpg.world.encounters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyDefinition;

// Selects enemy definitions using scenario encounter weights.
public final class EncounterGenerator {

	private final EnemyCatalog enemyCatalog;
	private final EncounterCatalog encounterCatalog;
	private final Random random;

	public EncounterGenerator(EnemyCatalog enemyCatalog, EncounterCatalog encounterCatalog, Random random) {
		this.enemyCatalog = enemyCatalog;
		this.encounterCatalog = encounterCatalog;
		this.random = random;
	}

	public Optional<EnemyDefinition> generate(String scenarioId, int position, boolean specialArea) {
		List<EncounterEntry> availableEntries = encounterCatalog.getAvailableEntries(scenarioId, position, specialArea);

		if (availableEntries.isEmpty()) {
			return Optional.empty();
		}

		EncounterEntry selectedEntry = selectWeightedEntry(availableEntries);

		EnemyDefinition enemyDefinition = enemyCatalog.getById(selectedEntry.getEnemyId());

		return Optional.of(enemyDefinition);
	}

	public List<EnemyDefinition> generateGroup(String scenarioId, int position, boolean specialArea, int minimumEnemies,
			int maximumEnemies) {
		validateGroupSize(minimumEnemies, maximumEnemies);

		List<EncounterEntry> availableEntries = encounterCatalog.getAvailableEntries(scenarioId, position, specialArea);

		if (availableEntries.isEmpty()) {
			return List.of();
		}

		int availableCapacity = calculateAvailableCapacity(availableEntries);

		int effectiveMaximum = Math.min(maximumEnemies, availableCapacity);

		int effectiveMinimum = Math.min(minimumEnemies, effectiveMaximum);

		int groupSize = random.nextInt(effectiveMaximum - effectiveMinimum + 1) + effectiveMinimum;

		List<EnemyDefinition> generatedGroup = new ArrayList<>();

		Map<String, Integer> selectedCopies = new HashMap<>();

		while (generatedGroup.size() < groupSize) {
			List<EncounterEntry> selectableEntries = availableEntries.stream()
					.filter(entry -> canSelectEntry(entry, selectedCopies)).toList();

			if (selectableEntries.isEmpty()) {
				break;
			}

			EncounterEntry selectedEntry = selectWeightedEntry(selectableEntries);

			String enemyId = selectedEntry.getEnemyId();

			EnemyDefinition enemyDefinition = enemyCatalog.getById(enemyId);

			generatedGroup.add(enemyDefinition);

			selectedCopies.merge(enemyId, 1, Integer::sum);
		}

		return List.copyOf(generatedGroup);
	}

	private EncounterEntry selectWeightedEntry(List<EncounterEntry> entries) {
		int totalWeight = 0;

		for (EncounterEntry entry : entries) {
			totalWeight += entry.getWeight();
		}

		int roll = random.nextInt(totalWeight);
		int accumulatedWeight = 0;

		for (EncounterEntry entry : entries) {
			accumulatedWeight += entry.getWeight();

			if (roll < accumulatedWeight) {
				return entry;
			}
		}

		throw new IllegalStateException("Encounter selection failed.");
	}

	private boolean canSelectEntry(EncounterEntry entry, Map<String, Integer> selectedCopies) {
		int currentCopies = selectedCopies.getOrDefault(entry.getEnemyId(), 0);

		return currentCopies < entry.getMaxCopies();
	}

	private int calculateAvailableCapacity(List<EncounterEntry> availableEntries) {
		Map<String, Integer> maximumCopiesByEnemy = new HashMap<>();

		for (EncounterEntry entry : availableEntries) {
			maximumCopiesByEnemy.merge(entry.getEnemyId(), entry.getMaxCopies(), Math::max);
		}

		int totalCapacity = 0;

		for (int maximumCopies : maximumCopiesByEnemy.values()) {

			totalCapacity += maximumCopies;
		}

		return totalCapacity;
	}

	private void validateGroupSize(int minimumEnemies, int maximumEnemies) {
		if (minimumEnemies < 1) {
			throw new IllegalArgumentException("Minimum enemy count must be at least one.");
		}

		if (maximumEnemies < minimumEnemies) {
			throw new IllegalArgumentException("Maximum enemy count cannot be lower than the minimum.");
		}
	}
}