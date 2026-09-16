package rpg.world.travel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Tracks the routes currently unlocked by world progress.
public final class TravelProgress {

	private final TravelCatalog travelCatalog;
	private final Set<String> unlockedRouteIds;

	public TravelProgress(TravelCatalog travelCatalog) {
		if (travelCatalog == null) {
			throw new IllegalArgumentException("Travel catalog cannot be null.");
		}

		this.travelCatalog = travelCatalog;
		unlockedRouteIds = new LinkedHashSet<>();

		registerInitiallyUnlockedRoutes();
	}

	private void registerInitiallyUnlockedRoutes() {
		for (TravelRouteDefinition routeDefinition : travelCatalog.getAll()) {
			if (routeDefinition.isInitiallyUnlocked()) {
				unlockedRouteIds.add(routeDefinition.getId());
			}
		}
	}

	public void unlock(String routeId) {
		travelCatalog.getById(routeId);
		unlockedRouteIds.add(routeId);
	}

	public void unlockAll() {
		for (TravelRouteDefinition routeDefinition : travelCatalog.getAll()) {
			unlock(routeDefinition.getId());
		}
	}

	public void unlockRoutesFrom(String sourceScenarioId) {
		for (TravelRouteDefinition routeDefinition : travelCatalog.getRoutesFrom(sourceScenarioId)) {
			unlock(routeDefinition.getId());
		}
	}

	public boolean isUnlocked(String routeId) {
		return unlockedRouteIds.contains(routeId);
	}

	public boolean hasUnlockedRoutesFrom(String sourceScenarioId) {
		for (TravelRouteDefinition routeDefinition : travelCatalog.getRoutesFrom(sourceScenarioId)) {
			if (isUnlocked(routeDefinition.getId())) {
				return true;
			}
		}

		return false;
	}

	public List<TravelRouteDefinition> getUnlockedRoutesFrom(String sourceScenarioId) {
		List<TravelRouteDefinition> unlockedRoutes = new ArrayList<>();

		for (TravelRouteDefinition routeDefinition : travelCatalog.getRoutesFrom(sourceScenarioId)) {
			if (isUnlocked(routeDefinition.getId())) {
				unlockedRoutes.add(routeDefinition);
			}
		}

		return List.copyOf(unlockedRoutes);
	}

	public Set<String> getUnlockedRouteIds() {
		return Set.copyOf(unlockedRouteIds);
	}
}