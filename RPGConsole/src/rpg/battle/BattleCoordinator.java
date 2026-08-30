package rpg.battle;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import rpg.battle.rewards.BattleRewards;
import rpg.battle.rewards.BattleRewardService;
import rpg.characters.Enemy;
import rpg.characters.Player;
import rpg.enemies.EnemyDefinition;
import rpg.enemies.EnemyDropCatalog;
import rpg.items.DropEntry;
import rpg.ui.BattleRewardScreen;
import rpg.world.scenarios.CombatScenario;

// Coordinates battle creation, execution and victory rewards.
public final class BattleCoordinator {

	private final Player player;
	private final Scanner scanner;
	private final EnemyDropCatalog enemyDropCatalog;
	private final BattleRewardService rewardService;
	private final BattleRewardScreen rewardScreen;

	public BattleCoordinator(Player player, Scanner scanner, EnemyDropCatalog enemyDropCatalog,
			BattleRewardService rewardService, BattleRewardScreen rewardScreen) {
		this.player = Objects.requireNonNull(player, "Player cannot be null.");

		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");

		this.enemyDropCatalog = Objects.requireNonNull(enemyDropCatalog, "Enemy drop catalog cannot be null.");

		this.rewardService = Objects.requireNonNull(rewardService, "Battle reward service cannot be null.");

		this.rewardScreen = Objects.requireNonNull(rewardScreen, "Battle reward screen cannot be null.");
	}

	public BattleResult start(EnemyDefinition enemyDefinition, CombatScenario combatScenario) {
		Enemy enemy = createEnemy(enemyDefinition);

		System.out.println("\nUm " + enemy.getName() + " apareceu!");

		Battle battle = new Battle(player, enemy, scanner);

		BattleResult result = battle.start();

		if (result == BattleResult.VICTORY) {
			grantVictoryRewards(enemy, combatScenario);
		}

		return result;
	}

	private Enemy createEnemy(EnemyDefinition enemyDefinition) {
		List<DropEntry> dropTable = enemyDropCatalog.getDropsByEnemyId(enemyDefinition.getId());

		return enemyDefinition.createEnemy(dropTable);
	}

	private void grantVictoryRewards(Enemy defeatedEnemy, CombatScenario combatScenario) {
		BattleRewards rewards = rewardService.grantRewards(player, defeatedEnemy, combatScenario);

		rewardScreen.show(player, rewards);
	}
}