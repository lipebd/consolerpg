package rpg.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import rpg.world.WorldClock;

// Stores world progress and fictional time in a campaign snapshot.
public final class WorldSnapshot {

	private final String currentScenarioId;
	private final long elapsedHours;
	private final List<String> discoveredScenarioIds;
	private final List<String> unlockedRouteIds;

	public WorldSnapshot(
			String currentScenarioId,
			long elapsedHours,
			List<String> discoveredScenarioIds,
			List<String> unlockedRouteIds
	) {
		if (currentScenarioId == null || currentScenarioId.isBlank()) {
			throw new IllegalArgumentException("Current scenario ID cannot be null or blank.");
		}

		if (elapsedHours < WorldClock.INITIAL_ELAPSED_HOURS) {
			throw new IllegalArgumentException("Elapsed world hours cannot be lower than the initial campaign time.");
		}

		Objects.requireNonNull(discoveredScenarioIds, "Discovered scenario IDs cannot be null.");
		Objects.requireNonNull(unlockedRouteIds, "Unlocked route IDs cannot be null.");

		validateUniqueIds(discoveredScenarioIds, "Discovered scenario");
		validateUniqueIds(unlockedRouteIds, "Unlocked route");

		if (!discoveredScenarioIds.contains(currentScenarioId)) {
			throw new IllegalArgumentException("The current scenario must be included among the discovered scenarios.");
		}

		this.currentScenarioId = currentScenarioId;
		this.elapsedHours = elapsedHours;
		this.discoveredScenarioIds = List.copyOf(discoveredScenarioIds);
		this.unlockedRouteIds = List.copyOf(unlockedRouteIds);
	}

	public String getCurrentScenarioId() {
		return currentScenarioId;
	}

	public long getElapsedHours() {
		return elapsedHours;
	}

	public List<String> getDiscoveredScenarioIds() {
		return discoveredScenarioIds;
	}

	public List<String> getUnlockedRouteIds() {
		return unlockedRouteIds;
	}

	private void validateUniqueIds(List<String> ids, String valueName) {
		Set<String> uniqueIds = new HashSet<>();

		for (String id : ids) {
			if (id == null || id.isBlank()) {
				throw new IllegalArgumentException(valueName + " ID cannot be null or blank.");
			}

			if (!uniqueIds.add(id)) {
				throw new IllegalArgumentException(valueName + " ID is duplicated: " + id);
			}
		}
	}
}