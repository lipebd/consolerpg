package rpg.world.scenarios;

// Defines the actions available in cities.
public enum CityAction {

	VISIT_LOCATION("Visitar um local"),
	LEAVE_CITY("Sair da cidade"),
	REGION_MAP("Mapa"),
	INVENTORY("Inventário"),
	QUESTS("Diário"),
	VIEW_STATUS("Ver status");

	private final String displayName;

	CityAction(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
