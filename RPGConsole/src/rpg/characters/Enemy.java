package rpg.characters;

import java.util.List;
import java.util.Objects;

import rpg.enemies.EnemyTargetProfile;
import rpg.items.DropEntry;
import rpg.skills.EnemySkillDefinition;

// Represents an enemy controlled by the game.
public class Enemy extends GameCharacter {

	private final String id;
	private final int level;
	private final List<DropEntry> dropTable;
	private String description;
	private final EnemyRank rank;
	private final EnemyTargetProfile targetProfile;
	private final List<EnemySkillDefinition> skillDefinitions;

	public Enemy(
			String id,
			String name,
			CharacterStats stats,
			int level,
			EnemyRank rank,
			EnemyTargetProfile targetProfile,
			List<EnemySkillDefinition> skillDefinitions,
			List<DropEntry> dropTable,
			String description
	) {
		super(name, stats);
		if (level < 1) {
			throw new IllegalArgumentException("Enemy level must be at least one.");
		}

		this.level = level;
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Enemy ID cannot be empty.");
		}

		this.id = id.trim();
		this.rank = Objects.requireNonNull(rank, "Enemy rank cannot be null.");
		this.targetProfile = Objects.requireNonNull(targetProfile, "Enemy target profile cannot be null.");

		this.skillDefinitions = List.copyOf(
				Objects.requireNonNull(skillDefinitions, "Enemy skill definitions cannot be null.")
		);

		this.dropTable = dropTable;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public String getDescription() {
		return description;
	}

	public EnemyRank getRank() {
		return rank;
	}

	public EnemyTargetProfile getTargetProfile() {
		return targetProfile;
	}

	public List<DropEntry> getDropTable() {
		return dropTable;
	}

	public List<EnemySkillDefinition> getSkillDefinitions() {
		return skillDefinitions;
	}
}
