package rpg.items.equipment;

// Defines the supported armor-weight categories.
public enum ArmorWeight {

	LIGHT("Leve"),
	MEDIUM("Média"),
	HEAVY("Pesada");

	private final String displayName;

	ArmorWeight(String displayName) {
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