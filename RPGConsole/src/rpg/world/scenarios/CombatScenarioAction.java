package rpg.world.scenarios;

// Defines the actions available in combat scenarios.
public enum CombatScenarioAction {

	EXPLORE("Explorar"),
	FACE_BOSS("Enfrentar o boss"),
	INVENTORY("Inventário"),
	VIEW_STATUS("Ver status"),
	FOLLOW_ROUTE("Seguir caminho"),
	CITY_TRANSPORT("Viajar"),
	FOLLOW_BRANCH("Bifurcação");

	private final String displayName;

	CombatScenarioAction(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
