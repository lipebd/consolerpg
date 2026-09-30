package rpg.items.equipment;

// Defines the descriptive power tiers of trinkets.
public enum TrinketType {

	JEWELRY("Joia"),
	AMULET("Amuleto"),
	TALISMAN("Talismã");

	private final String displayName;

	TrinketType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}