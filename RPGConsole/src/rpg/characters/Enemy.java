package rpg.characters;

import java.util.List;
import rpg.items.DropEntry;

// Represents an enemy controlled by the game.
public class Enemy extends GameCharacter {
	
	private final String id;
	private final List<DropEntry> dropTable;
    private String description;
    private final EnemyRank rank;
    
    public Enemy(
            String id,
            String name,
            CharacterStats stats,
            EnemyRank rank,
            List<DropEntry> dropTable,
            String description) {
        super(name, stats);

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Enemy ID cannot be empty.");
        }

        this.id = id.trim();
        this.rank = rank;
        this.dropTable = dropTable;
        this.description = description;
    }

    public String getId() {
        return id;
    }

	public String getDescription() {
	    return description;
	}
	
	public EnemyRank getRank() {
	    return rank;
	}
	
	public List<DropEntry> getDropTable() {
	    return dropTable;
	}
}