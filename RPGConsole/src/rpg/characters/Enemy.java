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
	        int maxHp,
	        int maxMp,
	        int maxSp,
	        int attack,
	        int defense,
	        int healingPower,
	        int luck,
	        EnemyRank rank,
	        List<DropEntry> dropTable,
	        String description
	) {
	    super(
	            name,
	            maxHp,
	            maxMp,
	            maxSp,
	            attack,
	            defense,
	            healingPower,
	            luck
	    );

	    this.rank = rank;
	    this.dropTable = List.copyOf(dropTable);
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