package rpg.battle.rewards;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rpg.items.Item;

// Stores the rewards obtained after winning a battle.
public final class BattleRewards {

	private final List<CharacterProgressionResult> characterProgressionResults;

	private final int gold;
	private final Map<Item, Integer> collectedItems;

	public BattleRewards(List<CharacterProgressionResult> characterProgressionResults, int gold,
			Map<Item, Integer> collectedItems) {
		this.characterProgressionResults = List.copyOf(
				Objects.requireNonNull(characterProgressionResults, "Character progression results cannot be null."));

		this.gold = gold;

		this.collectedItems = new LinkedHashMap<>(
				Objects.requireNonNull(collectedItems, "Collected items cannot be null."));
	}

	public List<CharacterProgressionResult> getCharacterProgressionResults() {

		return characterProgressionResults;
	}

	public int getGold() {
		return gold;
	}

	public Map<Item, Integer> getCollectedItems() {
		return new LinkedHashMap<>(collectedItems);
	}
}