package rpg.world.branches;

// Stores the immutable data of a fixed exploration branch.
public final class ScenarioBranchDefinition {

	private final String id;
	private final String sourceScenarioId;
	private final int explorationPosition;
	private final String destinationScenarioId;
	private final String displayName;
	private final String description;

	public ScenarioBranchDefinition(
			String id,
			String sourceScenarioId,
			int explorationPosition,
			String destinationScenarioId,
			String displayName,
			String description) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException(
					"Scenario branch ID cannot be empty.");
		}

		if (sourceScenarioId == null || sourceScenarioId.isBlank()) {
			throw new IllegalArgumentException(
					"Branch source scenario ID cannot be empty.");
		}

		if (explorationPosition < 1) {
			throw new IllegalArgumentException(
					"Branch exploration position must be greater than 0.");
		}

		if (destinationScenarioId == null
				|| destinationScenarioId.isBlank()) {
			throw new IllegalArgumentException(
					"Branch destination scenario ID cannot be empty.");
		}

		if (sourceScenarioId.equals(destinationScenarioId)) {
			throw new IllegalArgumentException(
					"A scenario branch cannot lead to its source scenario.");
		}

		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException(
					"Scenario branch display name cannot be empty.");
		}

		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException(
					"Scenario branch description cannot be empty.");
		}

		this.id = id;
		this.sourceScenarioId = sourceScenarioId;
		this.explorationPosition = explorationPosition;
		this.destinationScenarioId = destinationScenarioId;
		this.displayName = displayName;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public String getSourceScenarioId() {
		return sourceScenarioId;
	}

	public int getExplorationPosition() {
		return explorationPosition;
	}

	public String getDestinationScenarioId() {
		return destinationScenarioId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}
}