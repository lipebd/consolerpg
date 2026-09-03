package rpg.world.inns;

// Stores the immutable data of a city INN.
public final class InnDefinition {

	private final String id;
	private final String scenarioId;
	private final String name;
	private final String description;
	private final int price;

	public InnDefinition(
			String id,
			String scenarioId,
			String name,
			String description,
			int price) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Inn ID cannot be empty.");
		}

		if (scenarioId == null || scenarioId.isBlank()) {
			throw new IllegalArgumentException("Inn scenario ID cannot be empty.");
		}

		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Inn name cannot be empty.");
		}

		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("Inn description cannot be empty.");
		}

		if (price <= 0) {
			throw new IllegalArgumentException("Inn price must be greater than 0.");
		}

		this.id = id;
		this.scenarioId = scenarioId;
		this.name = name;
		this.description = description;
		this.price = price;
	}

	public String getId() {
		return id;
	}

	public String getScenarioId() {
		return scenarioId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getPrice() {
		return price;
	}
}