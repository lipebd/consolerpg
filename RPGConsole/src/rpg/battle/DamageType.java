package rpg.battle;

// Defines the possible damage types used in combat.
public enum DamageType {

	PHYSICAL("Físico"),
	MAGICAL("Mágico");

	private final String displayName;

	DamageType(String displayName) {
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