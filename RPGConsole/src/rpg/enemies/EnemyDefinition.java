package rpg.enemies;

import java.util.List;

import rpg.characters.Enemy;
import rpg.characters.EnemyRank;
import rpg.items.DropEntry;

// Stores the immutable data used to create enemy instances.
public final class EnemyDefinition {

    private final String id;
    private final String name;
    private final int maxHp;
    private final int maxMp;
    private final int maxSp;
    private final int attack;
    private final int defense;
    private final int healingPower;
    private final int luck;
    private final EnemyRank rank;
    private final List<DropEntry> dropTable;
    private final String description;

    public EnemyDefinition(
            String id,
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
        this.id = id;
        this.name = name;
        this.maxHp = maxHp;
        this.maxMp = maxMp;
        this.maxSp = maxSp;
        this.attack = attack;
        this.defense = defense;
        this.healingPower = healingPower;
        this.luck = luck;
        this.rank = rank;
        this.dropTable = List.copyOf(dropTable);
        this.description = description;
    }

    public Enemy createEnemy() {
        return new Enemy(
                name,
                maxHp,
                maxMp,
                maxSp,
                attack,
                defense,
                healingPower,
                luck,
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

	public int getMaxHp() {
		return maxHp;
	}

	public int getMaxMp() {
		return maxMp;
	}

	public int getMaxSp() {
		return maxSp;
	}

	public int getAttack() {
		return attack;
	}

	public int getDefense() {
		return defense;
	}

	public int getHealingPower() {
		return healingPower;
	}

	public int getLuck() {
		return luck;
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