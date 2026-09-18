package rpg.enemies;

import java.util.List;

import rpg.characters.CharacterStats;
import rpg.characters.Enemy;
import rpg.characters.EnemyRank;
import rpg.items.DropEntry;

// Represents the data required to create an enemy.
public final class EnemyDefinition {

    private final String id;
    private final String name;
    private final CharacterStats stats;
    private final EnemyRank rank;
    private final List<DropEntry> dropTable;
    private final String description;

    public EnemyDefinition(
            String id,
            String name,
            CharacterStats stats,
            EnemyRank rank,
            List<DropEntry> dropTable,
            String description
    ) {
        this.id = id;
        this.name = name;
        this.stats = stats;
        this.rank = rank;
        this.dropTable = List.copyOf(dropTable);
        this.description = description;
    }

    public Enemy createEnemy() {
        return new Enemy(
                id,
                name,
                stats,
                rank,
                dropTable,
                description
        );
    }

    public Enemy createEnemy(List<DropEntry> dropTable) {
        return new Enemy(
                id,
                name,
                stats,
                rank,
                dropTable,
                description
        );
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CharacterStats getStats() {
        return stats;
    }

    public EnemyRank getRank() {
        return rank;
    }

    public List<DropEntry> getDropTable() {
        return dropTable;
    }

    public String getDescription() {
        return description;
    }
}