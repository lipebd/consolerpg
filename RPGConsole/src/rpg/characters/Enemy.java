package rpg.characters;

import java.util.List;
import rpg.items.DropEntry;

// Represents an enemy controlled by the game.
public class Enemy extends GameCharacter {
	
	private final List<DropEntry> dropTable;
    private String description;
    private final EnemyRank rank;
    
    public Enemy(
            String name,
            CharacterStats stats,
            EnemyRank rank,
            List<DropEntry> dropTable,
            String description
    ) {
        super(name, stats);

        this.rank = rank;
        this.dropTable = dropTable;
        this.description = description;
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