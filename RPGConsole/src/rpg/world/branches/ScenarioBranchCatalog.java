package rpg.world.branches;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and resolves fixed branches by scenario and exploration position.
public final class ScenarioBranchCatalog {

	private final Map<String, ScenarioBranchDefinition> branchesById;

	private final Map<
			String,
			Map<Integer, List<ScenarioBranchDefinition>>>
			branchesBySourceScenarioIdAndPosition;

	public ScenarioBranchCatalog() {
		branchesById = new LinkedHashMap<>();
		branchesBySourceScenarioIdAndPosition = new LinkedHashMap<>();
	}

	public void register(ScenarioBranchDefinition branchDefinition) {
		if (branchDefinition == null) {
			throw new IllegalArgumentException(
					"Scenario branch definition cannot be null.");
		}

		String branchId = branchDefinition.getId();

		if (branchesById.containsKey(branchId)) {
			throw new IllegalArgumentException(
					"Scenario branch ID is already registered: "
							+ branchId);
		}

		String sourceScenarioId =
				branchDefinition.getSourceScenarioId();

		int explorationPosition =
				branchDefinition.getExplorationPosition();

		Map<Integer, List<ScenarioBranchDefinition>>
				branchesByPosition =
				branchesBySourceScenarioIdAndPosition.computeIfAbsent(
						sourceScenarioId,
						id -> new LinkedHashMap<>());

		List<ScenarioBranchDefinition> positionBranches =
				branchesByPosition.computeIfAbsent(
						explorationPosition,
						position -> new ArrayList<>());

		for (ScenarioBranchDefinition existingBranch
				: positionBranches) {
			if (existingBranch.getDestinationScenarioId().equals(
					branchDefinition.getDestinationScenarioId())) {
				throw new IllegalArgumentException(
						"Scenario branch destination is already "
								+ "registered at source position: "
								+ sourceScenarioId + ", position "
								+ explorationPosition);
			}
		}

		branchesById.put(branchId, branchDefinition);
		positionBranches.add(branchDefinition);
	}

	public boolean containsId(String branchId) {
	    return branchesById.containsKey(branchId);
	}

	public ScenarioBranchDefinition getById(String branchId) {
		ScenarioBranchDefinition branchDefinition =
				branchesById.get(branchId);

		if (branchDefinition == null) {
			throw new IllegalArgumentException(
					"Scenario branch not found: " + branchId);
		}

		return branchDefinition;
	}

	public List<ScenarioBranchDefinition> getBranchesAt(
			String sourceScenarioId,
			int explorationPosition) {
		Map<Integer, List<ScenarioBranchDefinition>>
				branchesByPosition =
				branchesBySourceScenarioIdAndPosition.getOrDefault(
						sourceScenarioId,
						Map.of());

		List<ScenarioBranchDefinition> branches =
				branchesByPosition.getOrDefault(
						explorationPosition,
						List.of());

		return List.copyOf(branches);
	}

	public boolean hasBranchesAt(
			String sourceScenarioId,
			int explorationPosition) {
		return !getBranchesAt(
				sourceScenarioId,
				explorationPosition).isEmpty();
	}

	public List<ScenarioBranchDefinition> getAll() {
		return List.copyOf(branchesById.values());
	}
}