package rpg.battle;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;

import rpg.battle.rewards.BattleRewards;
import rpg.battle.rewards.BattleRewardService;
import rpg.characters.Enemy;
import rpg.characters.Party;
import rpg.enemies.EnemyDefinition;
import rpg.enemies.EnemyDropCatalog;
import rpg.items.DropEntry;
import rpg.ui.BattleRewardScreen;
import rpg.world.scenarios.CombatScenario;

// Coordinates battle creation, execution and victory rewards.
public final class BattleCoordinator {

	private final Random random;
	private final Party party;
	private final Scanner scanner;
	private final EnemyDropCatalog enemyDropCatalog;
	private final BattleRewardService rewardService;
	private final BattleRewardScreen rewardScreen;

	public BattleCoordinator(Party party, Scanner scanner, EnemyDropCatalog enemyDropCatalog,
			BattleRewardService rewardService, BattleRewardScreen rewardScreen, Random random) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
		this.enemyDropCatalog = Objects.requireNonNull(enemyDropCatalog, "Enemy drop catalog cannot be null.");
		this.rewardService = Objects.requireNonNull(rewardService, "Battle reward service cannot be null.");
		this.rewardScreen = Objects.requireNonNull(rewardScreen, "Battle reward screen cannot be null.");
		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");
	}

	public BattleResult start(EnemyDefinition enemyDefinition, CombatScenario combatScenario) {
		Objects.requireNonNull(enemyDefinition, "Enemy definition cannot be null.");

		return start(List.of(enemyDefinition), combatScenario);
	}

	public BattleResult start(List<EnemyDefinition> enemyDefinitions, CombatScenario combatScenario) {
		Objects.requireNonNull(enemyDefinitions, "Enemy definition list cannot be null.");

		Objects.requireNonNull(combatScenario, "Combat scenario cannot be null.");

		if (enemyDefinitions.isEmpty()) {
			throw new IllegalArgumentException("Enemy definition list cannot be empty.");
		}

		List<Enemy> enemies = createEnemies(enemyDefinitions);

		EnemyBattleGroup enemyGroup = new EnemyBattleGroup(enemies);

		Battle battle = new Battle(party, enemyGroup, scanner, random);

		BattleResult result = battle.start();

		if (result == BattleResult.VICTORY) {
			grantVictoryRewards(enemies, combatScenario);
		}

		return result;
	}

	private List<Enemy> createEnemies(List<EnemyDefinition> enemyDefinitions) {
		List<Enemy> enemies = new ArrayList<>();

		for (EnemyDefinition enemyDefinition : enemyDefinitions) {

			Objects.requireNonNull(enemyDefinition, "Enemy definition cannot be null.");

			enemies.add(createEnemy(enemyDefinition));
		}

		return List.copyOf(enemies);
	}

	private Enemy createEnemy(EnemyDefinition enemyDefinition) {
		List<DropEntry> dropTable = enemyDropCatalog.getDropsByEnemyId(enemyDefinition.getId());

		return enemyDefinition.createEnemy(dropTable);
	}

	private void grantVictoryRewards(List<Enemy> defeatedEnemies, CombatScenario combatScenario) {
		BattleRewards rewards = rewardService.grantRewards(party, defeatedEnemies, combatScenario);

		rewardScreen.show(rewards);
	}
}