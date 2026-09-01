package rpg.world.scenarios;

public enum ScenarioAction {

	EXPLORE("Explorar"),
	VIEW_STATUS("Ver status"),
	INVENTORY("Inventário"),
	TRAVEL("Viajar"),

	ITEM_SHOP("Loja de Itens"),
	BLACKSMITH("Ferreiro"),
	TAVERN("Taverna"),
	LEAVE_CITY("Sair da cidade");

	private final String description;

	ScenarioAction(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}
}