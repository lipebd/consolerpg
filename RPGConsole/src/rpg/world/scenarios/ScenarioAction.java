package rpg.world.scenarios;

// Defines the actions available in game scenarios.
public enum ScenarioAction {

    EXPLORE("Explorar"),
    FACE_BOSS("Enfrentar o boss"),
    INVENTORY("Inventário"),
    VIEW_STATUS("Ver status"),
    TRAVEL("Viajar"),
    FOLLOW_BRANCH("Seguir outro caminho");

    private final String displayName;

    ScenarioAction(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}