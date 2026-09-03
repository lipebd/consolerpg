package rpg.world.rewards;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Stores exploration reward entries grouped by scenario ID.
public final class ExplorationRewardCatalog {

	private final Map<String, List<ExplorationRewardEntry>> entriesByScenario;

	public ExplorationRewardCatalog() {
		entriesByScenario = new LinkedHashMap<>();
	}

	public void register(String scenarioId, ExplorationRewardEntry rewardEntry) {
		if (scenarioId == null || scenarioId.isBlank()) {
			throw new IllegalArgumentException("Scenario ID cannot be empty.");
		}

		Objects.requireNonNull(rewardEntry, "Reward entry cannot be null.");

		List<ExplorationRewardEntry> scenarioEntries = entriesByScenario.computeIfAbsent(scenarioId,
				id -> new ArrayList<>());

		boolean duplicateEntry = scenarioEntries.stream()
				.anyMatch(existingEntry -> hasSameConfiguration(existingEntry, rewardEntry));

		if (duplicateEntry) {
			throw new IllegalArgumentException("Duplicate exploration reward entry " + "for scenario: " + scenarioId);
		}

		scenarioEntries.add(rewardEntry);
	}

	public List<ExplorationRewardEntry> getAvailableEntries(String scenarioId, boolean specialArea) {
		if (scenarioId == null || scenarioId.isBlank()) {
			throw new IllegalArgumentException("Scenario ID cannot be empty.");
		}

		List<ExplorationRewardEntry> scenarioEntries = entriesByScenario.getOrDefault(scenarioId, List.of());

		return scenarioEntries.stream().filter(entry -> entry.isAvailableIn(specialArea)).toList();
	}

	private boolean hasSameConfiguration(ExplorationRewardEntry firstEntry, ExplorationRewardEntry secondEntry) {
		return firstEntry.getType() == secondEntry.getType()
				&& firstEntry.getRewardId().equals(secondEntry.getRewardId())
				&& firstEntry.getMinQuantity() == secondEntry.getMinQuantity()
				&& firstEntry.getMaxQuantity() == secondEntry.getMaxQuantity()
				&& firstEntry.getWeight() == secondEntry.getWeight()
				&& firstEntry.getAreaRestriction() == secondEntry.getAreaRestriction();
	}
}