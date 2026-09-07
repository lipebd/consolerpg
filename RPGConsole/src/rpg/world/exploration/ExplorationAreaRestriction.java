package rpg.world.exploration;

// Defines which exploration areas allow an entry to appear.
public enum ExplorationAreaRestriction {

    NORMAL_ONLY,
    SPECIAL_ONLY,
    ANY;

    public boolean allows(boolean specialArea) {
        return switch (this) {
            case NORMAL_ONLY -> !specialArea;
            case SPECIAL_ONLY -> specialArea;
            case ANY -> true;
        };
    }
}