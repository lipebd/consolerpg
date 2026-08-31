package rpg.skills;

// Defines the type of recovery performed by a skill.
public enum RecoveryType {

	NONE("Nenhum"),
	HEAL("Cura"),
	REVIVE("Revive");

	private final String displayName;

	RecoveryType(String displayName) {
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