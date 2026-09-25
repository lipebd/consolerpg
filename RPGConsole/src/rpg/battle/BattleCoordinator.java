package rpg.battle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import rpg.battle.rewards.BattleRewardService;
import rpg.battle.rewards.BattleRewards;
import rpg.characters.Enemy;
import rpg.characters.Party;
import rpg.enemies.EnemyDefinition;
import rpg.enemies.EnemyDropCatalog;
import rpg.items.DropEntry;
import rpg.items.Item;
import rpg.quests.QuestProgressEvent;
import rpg.quests.QuestProgressEventType;
import rpg.ui.BattleRewardScreen;
import rpg.ui.ConsoleInput;
import rpg.ui.QuestProgressCoordinator;
import rpg.world.scenarios.CombatScenario;

// Coordinates battle creation, execution and victory rewards.
public final class BattleCoordinator {

	private final QuestProgressCoordinator questProgressCoordinator;
	private final Random random;
	private final Party party;
	private final ConsoleInput consoleInput;
	private final EnemyDropCatalog enemyDropCatalog;
	private final BattleRewardService rewardService;
	private final BattleRewardScreen rewardScreen;

	public BattleCoordinator(
			Party party,
			ConsoleInput consoleInput,
			EnemyDropCatalog enemyDropCatalog,
			BattleRewardService rewardService,
			BattleRewardScreen rewardScreen,
			QuestProgressCoordinator questProgressCoordinator,
			Random random
	) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.enemyDropCatalog = Objects.requireNonNull(enemyDropCatalog, "Enemy drop catalog cannot be null.");
		this.rewardService = Objects.requireNonNull(rewardService, "Battle reward service cannot be null.");
		this.rewardScreen = Objects.requireNonNull(rewardScreen, "Battle reward screen cannot be null.");
		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");
		this.questProgressCoordinator = Objects
				.requireNonNull(questProgressCoordinator, "Quest progress coordinator cannot be null.");
	}

	public BattleResult start(EnemyDefinition enemyDefinition, CombatScenario combatScenario) {
		Objects.requireNonNull(enemyDefinition, "Enemy definition cannot be null.");

		return start(List.of(enemyDefinition), combatScenario, BattleType.NORMAL);
	}

	public BattleResult start(List<EnemyDefinition> enemyDefinitions, CombatScenario combatScenario) {
		return start(enemyDefinitions, combatScenario, BattleType.NORMAL);
	}

	public BattleResult
			start(List<EnemyDefinition> enemyDefinitions, CombatScenario combatScenario, BattleType battleType) {
		Objects.requireNonNull(enemyDefinitions, "Enemy definition list cannot be null.");
		Objects.requireNonNull(combatScenario, "Combat scenario cannot be null.");
		Objects.requireNonNull(battleType, "Battle type cannot be null.");

		if (enemyDefinitions.isEmpty()) {
			throw new IllegalArgumentException("Enemy definition list cannot be empty.");
		}

		List<Enemy> enemies = createEnemies(enemyDefinitions);
		EnemyBattleGroup enemyGroup = new EnemyBattleGroup(enemies);

		Battle battle = new Battle(party, enemyGroup, consoleInput, random, battleType, combatScenario.getName());
		BattleResult result = battle.start();

		List<QuestProgressEvent> progressEvents = new ArrayList<>();

		addAnalysisProgressEvents(progressEvents, battle.getAnalyzedEnemyIds());

		if (result == BattleResult.VICTORY) {
			BattleRewards rewards = grantVictoryRewards(enemies, combatScenario);

			addVictoryProgressEvents(progressEvents, enemies, rewards, combatScenario);
		}

		if (!progressEvents.isEmpty()) {
			questProgressCoordinator.processBattleEventsAndDisplay(progressEvents);
		}

		return result;
	}

	private static void
			addAnalysisProgressEvents(List<QuestProgressEvent> progressEvents, List<String> analyzedEnemyIds) {
		for (String enemyId : analyzedEnemyIds) {
			progressEvents.add(new QuestProgressEvent(QuestProgressEventType.ENEMY_ANALYZED, Optional.of(enemyId), 1));
		}
	}

	private static void addVictoryProgressEvents(
			List<QuestProgressEvent> progressEvents,
			List<Enemy> defeatedEnemies,
			BattleRewards rewards,
			CombatScenario combatScenario
	) {
		Map<String, Integer> defeatedQuantitiesById = new LinkedHashMap<>();

		for (Enemy enemy : defeatedEnemies) {
			defeatedQuantitiesById.merge(enemy.getId(), 1, Integer::sum);
		}

		for (Map.Entry<String, Integer> defeatedQuantity : defeatedQuantitiesById.entrySet()) {
			progressEvents.add(
					new QuestProgressEvent(
							QuestProgressEventType.ENEMY_DEFEATED,
							Optional.of(defeatedQuantity.getKey()),
							defeatedQuantity.getValue()
					)
			);
		}

		progressEvents
				.add(new QuestProgressEvent(QuestProgressEventType.BATTLE_WON, Optional.of(combatScenario.getId()), 1));

		for (Map.Entry<Item, Integer> collectedItem : rewards.getCollectedItems().entrySet()) {
			progressEvents.add(
					new QuestProgressEvent(
							QuestProgressEventType.ITEM_OBTAINED,
							Optional.of(collectedItem.getKey().getId()),
							collectedItem.getValue()
					)
			);
		}
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

	private BattleRewards grantVictoryRewards(List<Enemy> defeatedEnemies, CombatScenario combatScenario) {
		BattleRewards rewards = rewardService.grantRewards(party, defeatedEnemies, combatScenario);

		rewardScreen.show(rewards);

		return rewards;
	}
}
