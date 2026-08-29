package rpg.characters;

// Represents an enemy controlled by the game.
public class Enemy extends GameCharacter {
	
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

	    this.description = description;
	    this.rank = rank;
	}
	
	public String getDescription() {
	    return description;
	}
	
	public EnemyRank getRank() {
	    return rank;
	}
}