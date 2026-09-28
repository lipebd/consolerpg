package rpg.enemies;

import java.util.List;
import java.util.Objects;

import rpg.characters.CharacterAttribute;
import rpg.characters.CharacterStats;
import rpg.characters.Enemy;
import rpg.characters.EnemyRank;
import rpg.items.DropEntry;
import rpg.skills.EnemySkillDefinition;

// Represents the data required to create an enemy.
public final class EnemyDefinition {

	private static final EnemyGrowthCalculator GROWTH_CALCULATOR = new EnemyGrowthCalculator();

	private final String id;
	private final String name;
	private final CharacterStats stats;
	private final EnemyRank rank;
	private final List<DropEntry> dropTable;
	private final String description;
	private final CharacterAttribute primaryAttribute;
	private final CharacterAttribute secondaryAttribute;
	private final CharacterAttribute weaknessAttribute;
	private final EnemyTargetProfile targetProfile;
	private final double height;
	private final double weight;

	public EnemyDefinition(
			String id,
			String name,
			CharacterStats stats,
			CharacterAttribute primaryAttribute,
			CharacterAttribute secondaryAttribute,
			CharacterAttribute weaknessAttribute,
			EnemyRank rank,
			EnemyTargetProfile targetProfile,
			List<DropEntry> dropTable,
			String description,
			double height,
			double weight
	) {
		GROWTH_CALCULATOR.validateGrowthAttributes(primaryAttribute, secondaryAttribute, weaknessAttribute);

		if (!Double.isFinite(height) || height <= 0) {
			throw new IllegalArgumentException("Enemy height must be finite and greater than zero.");
		}

		if (!Double.isFinite(weight) || weight <= 0) {
			throw new IllegalArgumentException("Enemy weight must be finite and greater than zero.");
		}

		this.id = id;
		this.name = name;
		this.stats = stats;
		this.rank = rank;
		this.dropTable = List.copyOf(dropTable);
		this.description = description;
		this.primaryAttribute = primaryAttribute;
		this.secondaryAttribute = secondaryAttribute;
		this.weaknessAttribute = weaknessAttribute;
		this.targetProfile = Objects.requireNonNull(targetProfile, "Enemy target profile cannot be null.");
		this.height = height;
		this.weight = weight;
	}

	public Enemy createEnemy(
			int level,
			List<EnemySkillDefinition> skillDefinitions,
			List<DropEntry> dropTable
	) {
		return new Enemy(
				id,
				name,
				calculateNaturalStats(level),
				level,
				rank,
				targetProfile,
				skillDefinitions,
				dropTable,
				description
		);
	}

	public CharacterStats calculateNaturalStats(int level) {
		return GROWTH_CALCULATOR.calculateNaturalStats(
				stats,
				primaryAttribute,
				secondaryAttribute,
				weaknessAttribute,
				rank,
				level
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

	public CharacterAttribute getPrimaryAttribute() {
		return primaryAttribute;
	}

	public CharacterAttribute getSecondaryAttribute() {
		return secondaryAttribute;
	}

	public CharacterAttribute getWeaknessAttribute() {
		return weaknessAttribute;
	}

	public EnemyTargetProfile getTargetProfile() {
		return targetProfile;
	}

	public double getHeight() {
		return height;
	}

	public double getWeight() {
		return weight;
	}
}
