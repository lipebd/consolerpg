package rpg.world.scenarios;

// Defines the actions available in cities.
public enum CityAction {

	VISIT_LOCATION("Visitar um local"),
	MARKETPLACE("Posto de Trocas"),
	LEAVE_CITY("Sair da cidade"),
	CITY_TRANSPORT("Viajar"),
	INVENTORY("Inventário"),
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
