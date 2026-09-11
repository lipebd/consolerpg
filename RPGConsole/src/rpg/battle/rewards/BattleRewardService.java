package rpg.battle.rewards;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import rpg.characters.PlayableCharacter;
import rpg.characters.Enemy;
import rpg.characters.Party;
import rpg.items.DropEntry;
import rpg.items.Item;
import rpg.world.scenarios.CombatScenario;
import rpg.characters.CharacterStats;
import rpg.skills.SkillDefinition;

// Calculates and grants rewards after a battle victory.
public final class BattleRewardService {

	private final Random random;

	private final RewardCalculator rewardCalculator;

	public BattleRewardService(Random random) {
		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");

		this.rewardCalculator = new RewardCalculator();
	}

	// Overload for a battle against a single enemy.
	public BattleRewards grantRewards(Party party, Enemy defeatedEnemy, CombatScenario combatScenario) {
		Objects.requireNonNull(defeatedEnemy, "Defeated enemy cannot be null.");

		return grantRewards(party, List.of(defeatedEnemy), combatScenario);
	}

	public BattleRewards grantRewards(Party party, List<Enemy> defeatedEnemies, CombatScenario combatScenario) {
		Objects.requireNonNull(party, "Party cannot be null.");

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

			int enemyExperienceReward = Math
					.toIntExact(Math.round(combatScenario.getBaseExperienceReward() * rankMultiplier));

			int enemyGoldReward = Math.toIntExact(Math.round(combatScenario.getBaseGoldReward() * rankMultiplier));

			experienceReward = Math.addExact(experienceReward, enemyExperienceReward);

			goldReward = Math.addExact(goldReward, enemyGoldReward);

		}

		BattleRewardModifiers rewardModifiers = createRewardModifiers(party);

		int globalExperienceReward = rewardCalculator
				.calculateModifiedReward(experienceReward, rewardModifiers.getTemporaryExperienceModifierPercent());

		int modifiedGoldReward = rewardCalculator
				.calculateModifiedReward(goldReward, rewardModifiers.getTotalGoldModifierPercent());

		List<CharacterProgressionResult> progressionResults = grantExperienceToParty(
				party,
				experienceReward,
				rewardModifiers
		);

		party.addGold(modifiedGoldReward);

		Map<Item, Integer> collectedItems = collectEnemyDrops(defeatedEnemies);

		addCollectedItemsToInventory(party, collectedItems);

		party.getRewardModifierState().consumeRewardedVictory();

		return new BattleRewards(
				progressionResults,
				globalExperienceReward,
				modifiedGoldReward,
				collectedItems,
				rewardModifiers
		);
	}

	private BattleRewardModifiers createRewardModifiers(Party party) {
		PartyRewardModifierState modifierState = party.getRewardModifierState();

		int experienceVictoriesRemaining = Math.max(0, modifierState.getExperienceVictoriesRemaining() - 1);

		int goldVictoriesRemaining = Math.max(0, modifierState.getGoldVictoriesRemaining() - 1);

		Map<PlayableCharacter, Integer> individualExperienceModifierPercents = new LinkedHashMap<>();

		int equipmentGoldModifierPercent = party.getEquipmentGoldModifierPercent();

		for (PlayableCharacter member : party.getMembers()) {
			int individualExperienceModifierPercent = member.getEquipment().getExperienceModifierPercent();

			if (individualExperienceModifierPercent > 0) {
				individualExperienceModifierPercents.put(member, individualExperienceModifierPercent);
			}

		}

		return new BattleRewardModifiers(
				modifierState.getExperienceModifierPercent(),
				experienceVictoriesRemaining,
				modifierState.getGoldModifierPercent(),
				goldVictoriesRemaining,
				equipmentGoldModifierPercent,
				individualExperienceModifierPercents
		);
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

	private void addCollectedItemsToInventory(Party party, Map<Item, Integer> collectedItems) {
		for (Map.Entry<Item, Integer> entry : collectedItems.entrySet()) {

			party.getInventory().addItem(entry.getKey(), entry.getValue());
		}
	}

	private List<CharacterProgressionResult>
			grantExperienceToParty(Party party, int baseExperienceReward, BattleRewardModifiers rewardModifiers) {
		List<CharacterProgressionResult> results = new ArrayList<>();

		for (PlayableCharacter member : party.getMembers()) {
			int experienceReward = rewardCalculator.calculateModifiedReward(
					baseExperienceReward,
					rewardModifiers.getTotalExperienceModifierPercent(member)
			);

			int previousLevel = member.getLevel();

			CharacterStats previousNaturalStats = member.getNaturalStats();

			member.gainExperience(experienceReward);

			int currentLevel = member.getLevel();

			CharacterStats currentNaturalStats = member.getNaturalStats();

			List<SkillDefinition> newlyUnlockedSkillDefinitions = member.getUnlockedSkillDefinitions().stream()
					.filter(skillDefinition -> skillDefinition.getAcquisitionLevel() > previousLevel)
					.filter(skillDefinition -> skillDefinition.getAcquisitionLevel() <= currentLevel).toList();

			boolean resourcesRestored = false;

			if (currentLevel > previousLevel) {
				resourcesRestored = member.restoreAllResources();
			}

			results.add(
					new CharacterProgressionResult(
							member,
							experienceReward,
							previousLevel,
							currentLevel,
							resourcesRestored,
							previousNaturalStats,
							currentNaturalStats,
							newlyUnlockedSkillDefinitions
					)
			);
		}

		return results;
	}
}