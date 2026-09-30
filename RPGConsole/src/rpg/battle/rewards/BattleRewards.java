package rpg.battle.rewards;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rpg.items.Item;

// Stores the rewards obtained after winning a battle.
public final class BattleRewards {

	private final BattleRewardModifiers rewardModifiers;
	private final List<CharacterProgressionResult> characterProgressionResults;

	private final int discardedGold;
	private final Map<Item, Integer> discardedItems;
	private final int globalExperience;
	private final int gold;
	private final Map<Item, Integer> collectedItems;

	public BattleRewards(
			List<CharacterProgressionResult> characterProgressionResults,
			int globalExperience,
			int gold,
			int discardedGold,
			Map<Item, Integer> collectedItems,
			Map<Item, Integer> discardedItems,
			BattleRewardModifiers rewardModifiers
	) {
		if (globalExperience < 0) {
			throw new IllegalArgumentException("Global experience reward cannot be negative.");
		}

		if (gold < 0) {
			throw new IllegalArgumentException("Gold reward cannot be negative.");
		}

		if (discardedGold < 0) {
			throw new IllegalArgumentException("Discarded GOLD reward cannot be negative.");
		}

		this.characterProgressionResults = List.copyOf(
				Objects.requireNonNull(characterProgressionResults, "Character progression results cannot be null.")
		);

		this.globalExperience = globalExperience;

		this.gold = gold;

		this.discardedGold = discardedGold;

		this.collectedItems = new LinkedHashMap<>(
				Objects.requireNonNull(collectedItems, "Collected items cannot be null.")
		);

		this.discardedItems = new LinkedHashMap<>(
				Objects.requireNonNull(discardedItems, "Discarded items cannot be null.")
		);

		this.rewardModifiers = Objects.requireNonNull(rewardModifiers, "Battle reward modifiers cannot be null.");
	}

	public List<CharacterProgressionResult> getCharacterProgressionResults() {

		return characterProgressionResults;
	}

	public BattleRewardModifiers getRewardModifiers() {
		return rewardModifiers;
	}

	public int getDiscardedGold() {
		return discardedGold;
	}

	public Map<Item, Integer> getDiscardedItems() {
		return new LinkedHashMap<>(discardedItems);
	}

	public int getGlobalExperience() {
		return globalExperience;
	}

	public int getGold() {
		return gold;
	}

	public Map<Item, Integer> getCollectedItems() {
		return new LinkedHashMap<>(collectedItems);
	}
}