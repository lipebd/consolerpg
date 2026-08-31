package rpg.world.encounters;

// Defines where an encounter entry is allowed to appear.
public enum EncounterAreaRestriction {

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