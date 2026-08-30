package rpg.battle.rewards;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import rpg.characters.Enemy;
import rpg.characters.Player;
import rpg.items.DropEntry;
import rpg.items.Item;
import rpg.world.scenarios.CombatScenario;

// Calculates and grants rewards after a battle victory.
public final class BattleRewardService {

    private final Random random;

    public BattleRewardService(Random random) {
        this.random = Objects.requireNonNull(
                random,
                "Random generator cannot be null."
        );
    }

    public BattleRewards grantRewards(
            Player player,
            Enemy defeatedEnemy,
            CombatScenario combatScenario
    ) {
        double rankMultiplier =
                defeatedEnemy
                .getRank()
                .getRewardMultiplier();

        int experienceReward = (int) Math.round(
                combatScenario.getBaseExperienceReward()
                * rankMultiplier
        );

        int goldReward = (int) Math.round(
                combatScenario.getBaseGoldReward()
                * rankMultiplier
        );

        int levelsGained =
                player.gainExperience(experienceReward);

        player.addGold(goldReward);

        Map<Item, Integer> collectedItems =
                collectEnemyDrops(
                        player,
                        defeatedEnemy
                );

        return new BattleRewards(
                experienceReward,
                goldReward,
                levelsGained,
                collectedItems
        );
    }

    private Map<Item, Integer> collectEnemyDrops(
            Player player,
            Enemy defeatedEnemy
    ) {
        Map<Item, Integer> collectedItems =
                new LinkedHashMap<>();

        for (DropEntry dropEntry
                : defeatedEnemy.getDropTable()) {

            int quantity =
                    dropEntry.rollQuantity(random);

            if (quantity <= 0) {
                continue;
            }

            Item item = dropEntry.getItem();

            player.getInventory().addItem(
                    item,
                    quantity
            );

            int currentQuantity =
                    collectedItems.getOrDefault(
                            item,
                            0
                    );

            collectedItems.put(
                    item,
                    currentQuantity + quantity
            );
        }

        return collectedItems;
    }
}