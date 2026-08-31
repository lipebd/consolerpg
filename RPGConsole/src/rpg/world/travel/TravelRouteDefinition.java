package rpg.world.travel;

// Stores the immutable data of a travel route.
public final class TravelRouteDefinition {

	private final String id;
	private final String sourceScenarioId;
	private final String destinationScenarioId;
	private final boolean initiallyUnlocked;
	private final String displayName;
	private final String description;

	public TravelRouteDefinition(String id, String sourceScenarioId, String destinationScenarioId,
			boolean initiallyUnlocked, String displayName, String description) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Travel route ID cannot be empty.");
		}

		if (sourceScenarioId == null || sourceScenarioId.isBlank()) {
			throw new IllegalArgumentException("Travel source scenario ID cannot be empty.");
		}

		if (destinationScenarioId == null || destinationScenarioId.isBlank()) {
			throw new IllegalArgumentException("Travel destination scenario ID cannot be empty.");
		}

		if (sourceScenarioId.equals(destinationScenarioId)) {
			throw new IllegalArgumentException("A travel route cannot lead to its source scenario.");
		}

		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException("Travel route display name cannot be empty.");
		}

		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("Travel route description cannot be empty.");
		}

		this.id = id;
		this.sourceScenarioId = sourceScenarioId;
		this.destinationScenarioId = destinationScenarioId;
		this.initiallyUnlocked = initiallyUnlocked;
		this.displayName = displayName;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public String getSourceScenarioId() {
		return sourceScenarioId;
	}

	public String getDestinationScenarioId() {
		return destinationScenarioId;
	}

	public boolean isInitiallyUnlocked() {
		return initiallyUnlocked;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}
}