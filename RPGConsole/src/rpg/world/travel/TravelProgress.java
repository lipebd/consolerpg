package rpg.world.travel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import rpg.world.branches.ScenarioBranchCatalog;
import rpg.world.branches.ScenarioBranchDefinition;

// Tracks the routes currently unlocked by world progress.
public final class TravelProgress {

	private final ScenarioBranchCatalog scenarioBranchCatalog;
	private final TravelCatalog travelCatalog;
	private final Set<String> unlockedRouteIds;

	public TravelProgress(
	        TravelCatalog travelCatalog,
	        ScenarioBranchCatalog scenarioBranchCatalog) {
	    if (travelCatalog == null) {
	        throw new IllegalArgumentException(
	                "Travel catalog cannot be null.");
	    }

	    if (scenarioBranchCatalog == null) {
	        throw new IllegalArgumentException(
	                "Scenario branch catalog cannot be null.");
	    }

	    this.travelCatalog = travelCatalog;
	    this.scenarioBranchCatalog = scenarioBranchCatalog;
	    unlockedRouteIds = new LinkedHashSet<>();
	    validateUniquePathIds();
	    registerInitiallyUnlockedRoutes();
	}

	private void registerInitiallyUnlockedRoutes() {
	    for (TravelRouteDefinition routeDefinition : travelCatalog.getAll()) {
	        if (routeDefinition.isInitiallyUnlocked()) {
	            unlockedRouteIds.add(routeDefinition.getId());
	        }
	    }

	    for (ScenarioBranchDefinition branchDefinition
	            : scenarioBranchCatalog.getAll()) {
	        if (branchDefinition.isInitiallyUnlocked()) {
	            unlockedRouteIds.add(branchDefinition.getId());
	        }
	    }
	}

	public void unlock(String routeId) {
	    if (!travelCatalog.containsId(routeId)
	            && !scenarioBranchCatalog.containsId(routeId)) {
	        throw new IllegalArgumentException(
	                "Travel route or scenario branch not found: " + routeId);
	    }

	    unlockedRouteIds.add(routeId);
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

	public List<ScenarioBranchDefinition> getUnlockedBranchesAt(
	        String sourceScenarioId,
	        int explorationPosition) {
	    List<ScenarioBranchDefinition> unlockedBranches =
	            new ArrayList<>();

	    for (ScenarioBranchDefinition branchDefinition
	            : scenarioBranchCatalog.getBranchesAt(
	                    sourceScenarioId,
	                    explorationPosition)) {
	        if (isUnlocked(branchDefinition.getId())) {
	            unlockedBranches.add(branchDefinition);
	        }
	    }

	    return List.copyOf(unlockedBranches);
	}

	private void validateUniquePathIds() {
	    for (ScenarioBranchDefinition branchDefinition
	            : scenarioBranchCatalog.getAll()) {
	        if (travelCatalog.containsId(branchDefinition.getId())) {
	            throw new IllegalArgumentException(
	                    "Travel route and scenario branch cannot share ID: "
	                            + branchDefinition.getId());
	        }
	    }
	}
}