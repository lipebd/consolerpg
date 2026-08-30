package rpg.battle.rewards;

import java.util.LinkedHashMap;
import java.util.Map;

import rpg.items.Item;

// Stores the rewards obtained after winning a battle.
public final class BattleRewards {

    private final int experience;
    private final int gold;
    private final int levelsGained;
    private final Map<Item, Integer> collectedItems;

    public BattleRewards(
            int experience,
            int gold,
            int levelsGained,
            Map<Item, Integer> collectedItems
    ) {
        this.experience = experience;
        this.gold = gold;
        this.levelsGained = levelsGained;
        this.collectedItems = new LinkedHashMap<>(
                collectedItems
        );
    }

    public int getExperience() {
        return experience;
    }

    public int getGold() {
        return gold;
    }

    public int getLevelsGained() {
        return levelsGained;
    }

    public Map<Item, Integer> getCollectedItems() {
        return Map.copyOf(collectedItems);
    }
}