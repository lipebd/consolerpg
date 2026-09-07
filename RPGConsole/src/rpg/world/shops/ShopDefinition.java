package rpg.world.shops;

// Stores the immutable data of a shop.
public final class ShopDefinition {

	private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

	private final String id;
	private final String scenarioId;
	private final ShopAccessType accessType;
	private final int offerSlots;
	private final String name;
	private final String description;

	public ShopDefinition(
			String id,
			String scenarioId,
			ShopAccessType accessType,
			int offerSlots,
			String name,
			String description) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Shop ID cannot be empty.");
		}

		String trimmedId = id.trim();

		if (!trimmedId.matches(ID_PATTERN)) {
			throw new IllegalArgumentException(
					"Shop ID must use lowercase letters, numbers "
							+ "and single underscores: " + trimmedId
			);
		}

		if (scenarioId == null || scenarioId.isBlank()) {
			throw new IllegalArgumentException("Shop scenario ID cannot be empty.");
		}

		if (accessType == null) {
			throw new IllegalArgumentException("Shop access type cannot be null.");
		}

		if (offerSlots <= 0) {
			throw new IllegalArgumentException("Shop offer slots must be greater than 0.");
		}

		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Shop name cannot be empty.");
		}

		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("Shop description cannot be empty.");
		}

		this.id = trimmedId;
		this.scenarioId = scenarioId;
		this.accessType = accessType;
		this.offerSlots = offerSlots;
		this.name = name;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public String getScenarioId() {
		return scenarioId;
	}

	public ShopAccessType getAccessType() {
		return accessType;
	}

	public int getOfferSlots() {
		return offerSlots;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}