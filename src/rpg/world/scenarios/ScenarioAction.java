package rpg.world.scenarios;

// Defines the actions available in game scenarios.
public enum ScenarioAction {

    EXPLORE("Explorar"),
    VIEW_STATUS("Ver status"),
    TRAVEL("Viajar");

    private final String displayName;

    ScenarioAction(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}