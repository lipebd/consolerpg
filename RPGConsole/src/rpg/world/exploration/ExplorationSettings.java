package rpg.world.exploration;

// Stores the exploration configuration of a combat scenario.
public final class ExplorationSettings {

    private final int length;
    private final int encounterChance;
    private final int specialAreaChance;
    private final int minEnemies;
    private final int maxEnemies;
    private final String normalAreaMessage;
    private final String specialAreaMessage;

    public ExplorationSettings(
            int length,
            int encounterChance,
            int specialAreaChance,
            int minEnemies,
            int maxEnemies,
            String normalAreaMessage,
            String specialAreaMessage
    ) {
        if (length < 1) {
            throw new IllegalArgumentException(
                    "Exploration length must be at least one."
            );
        }

        if (encounterChance < 0 || encounterChance > 100) {
            throw new IllegalArgumentException(
                    "Encounter chance must be between 0 and 100."
            );
        }

        if (
                specialAreaChance < 0
                || specialAreaChance > 100
        ) {
            throw new IllegalArgumentException(
                    "Special area chance must be between 0 and 100."
            );
        }

        if (minEnemies < 1) {
            throw new IllegalArgumentException(
                    "Minimum enemy count must be at least one."
            );
        }

        if (maxEnemies < minEnemies) {
            throw new IllegalArgumentException(
                    "Maximum enemy count cannot be lower "
                    + "than the minimum."
            );
        }
        
        if (
                normalAreaMessage == null
                || normalAreaMessage.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Normal area message cannot be empty."
            );
        }

        if (
                specialAreaMessage == null
                || specialAreaMessage.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Special area message cannot be empty."
            );
        }

        this.length = length;
        this.encounterChance = encounterChance;
        this.specialAreaChance = specialAreaChance;
        this.minEnemies = minEnemies;
        this.maxEnemies = maxEnemies;
        this.normalAreaMessage = normalAreaMessage;
        this.specialAreaMessage = specialAreaMessage;
    }

    public int getLength() {
        return length;
    }

    public int getEncounterChance() {
        return encounterChance;
    }

    public int getSpecialAreaChance() {
        return specialAreaChance;
    }

    public int getMinEnemies() {
        return minEnemies;
    }

    public int getMaxEnemies() {
        return maxEnemies;
    }
    
    public String getNormalAreaMessage() {
        return normalAreaMessage;
    }

    public String getSpecialAreaMessage() {
        return specialAreaMessage;
    }
}