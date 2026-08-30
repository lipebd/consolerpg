package rpg.battle.rewards;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.List;

import rpg.characters.Enemy;
import rpg.characters.Player;
import rpg.items.DropEntry;
import rpg.items.Item;
import rpg.world.scenarios.CombatScenario;

// Calculates and grants rewards after a battle victory.
public final class BattleRewardService {

	private final Random random;

	public BattleRewardService(Random random) {
		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");
	}

	// sobrecarga pra tipos de parametros diferentes
	public BattleRewards grantRewards(Player player, Enemy defeatedEnemy, CombatScenario combatScenario) {
		Objects.requireNonNull(defeatedEnemy, "Defeated enemy cannot be null.");

		return grantRewards(player, List.of(defeatedEnemy), combatScenario);
	}

	public BattleRewards grantRewards(Player player, List<Enemy> defeatedEnemies, CombatScenario combatScenario) {
		Objects.requireNonNull(player, "Player cannot be null.");

		Objects.requireNonNull(defeatedEnemies, "Defeated enemy list cannot be null.");

		Objects.requireNonNull(combatScenario, "Combat scenario cannot be null.");

		if (defeatedEnemies.isEmpty()) {
			throw new IllegalArgumentException("Defeated enemy list cannot be empty.");
		}

		int experienceReward = 0;
		int goldReward = 0;

		for (Enemy defeatedEnemy : defeatedEnemies) {
			Objects.requireNonNull(defeatedEnemy, "Defeated enemy cannot be null.");

			double rankMultiplier = defeatedEnemy.getRank().getRewardMultiplier();

			experienceReward += (int) Math.round(combatScenario.getBaseExperienceReward() * rankMultiplier);

			goldReward += (int) Math.round(combatScenario.getBaseGoldReward() * rankMultiplier);
		}

		int levelsGained = player.gainExperience(experienceReward);

		player.addGold(goldReward);

		Map<Item, Integer> collectedItems = collectEnemyDrops(defeatedEnemies);

		addCollectedItemsToInventory(player, collectedItems);

		return new BattleRewards(experienceReward, goldReward, levelsGained, collectedItems);
	}

	private Map<Item, Integer> collectEnemyDrops(List<Enemy> defeatedEnemies) {
		Map<Item, Integer> collectedItems = new LinkedHashMap<>();

		for (Enemy defeatedEnemy : defeatedEnemies) {
			for (DropEntry dropEntry : defeatedEnemy.getDropTable()) {

				int quantity = dropEntry.rollQuantity(random);

				if (quantity <= 0) {
					continue;
				}

				Item item = dropEntry.getItem();

				collectedItems.merge(item, quantity, Integer::sum);
			}
		}

		return collectedItems;
	}

	private void addCollectedItemsToInventory(Player player, Map<Item, Integer> collectedItems) {
		for (Map.Entry<Item, Integer> entry : collectedItems.entrySet()) {

			player.getInventory().addItem(entry.getKey(), entry.getValue());
		}
	}
}