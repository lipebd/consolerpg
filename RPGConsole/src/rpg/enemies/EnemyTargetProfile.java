package rpg.enemies;

// Defines how an enemy acquires and changes individual party targets.
public enum EnemyTargetProfile {

	INSTINCTIVE("Instintiva"),
	ERRATIC("Errática"),
	PREDATOR("Predadora"),
	TACTICAL("Tática");

	private final String displayName;

	EnemyTargetProfile(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
