package rpg.world.scenarios;

// Stores reusable data shared by all scenario definitions.
public final class ScenarioDefinition {

	private final String id;
	private final ScenarioType type;
	private final String name;
	private final String description;

	public ScenarioDefinition(String id, ScenarioType type, String name, String description) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Scenario ID cannot be empty.");
		}

		if (type == null) {
			throw new IllegalArgumentException("Scenario type cannot be null.");
		}

		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Scenario name cannot be empty.");
		}

		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("Scenario description cannot be empty.");
		}

		this.id = id;
		this.type = type;
		this.name = name;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public ScenarioType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}