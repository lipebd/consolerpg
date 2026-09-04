package rpg.world.transport;

// Stores the immutable data of a paid city transport destination.
public final class CityTransportDefinition {

	private final String destinationScenarioId;
	private final int price;
	private final String travelDescription;

	public CityTransportDefinition(String destinationScenarioId, int price, String travelDescription) {
		if (destinationScenarioId == null || destinationScenarioId.isBlank()) {
			throw new IllegalArgumentException("City transport destination scenario ID cannot be empty.");
		}

		if (price <= 0) {
			throw new IllegalArgumentException("City transport price must be greater than 0.");
		}

		if (travelDescription == null || travelDescription.isBlank()) {
			throw new IllegalArgumentException("City transport description cannot be empty.");
		}

		this.destinationScenarioId = destinationScenarioId;
		this.price = price;
		this.travelDescription = travelDescription;
	}

	public String getDestinationScenarioId() {
		return destinationScenarioId;
	}

	public int getPrice() {
		return price;
	}

	public String getTravelDescription() {
		return travelDescription;
	}
}