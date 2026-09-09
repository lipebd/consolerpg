package rpg.classes;

// Defines the armor weight supported by a playable class.
public enum PlayableClassWeight {

	LIGHT("Leve"), MEDIUM("Média"), HEAVY("Pesada");

	private final String displayName;

	PlayableClassWeight(String displayName) {
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