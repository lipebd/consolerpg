package rpg.world.scenarios;

// Defines the actions available in combat scenarios.
public enum CombatScenarioAction {

	EXPLORE("Explorar"),
	FACE_BOSS("Enfrentar o boss"),
	INVENTORY("Inventário"),
	VIEW_STATUS("Ver status"),
	FOLLOW_ROUTE("Viajar"),
	FOLLOW_BRANCH("Seguir outro caminho");

	private final String displayName;

	CombatScenarioAction(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
