package rpg.world.scenarios;

public enum CityAction {

    ITEM_SHOP("Loja de Itens"),
    BLACKSMITH("Ferreiro"),
    TAVERN("Taverna"),
    LEAVE("Sair da cidade");

    private final String description;

    CityAction(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}