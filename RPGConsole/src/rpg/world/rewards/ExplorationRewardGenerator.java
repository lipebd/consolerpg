package rpg.world.rewards;

import java.util.List;
import java.util.Objects;
import java.util.Random;

// Selects rewards using the weights of an exploration pool.
public final class ExplorationRewardGenerator {

	private final ExplorationRewardCatalog rewardCatalog;
	private final Random random;

	public ExplorationRewardGenerator(ExplorationRewardCatalog rewardCatalog, Random random) {
		this.rewardCatalog = Objects.requireNonNull(rewardCatalog, "Exploration reward catalog cannot be null.");

		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");
	}

	public ExplorationReward generate(String scenarioId, boolean specialArea) {
		List<ExplorationRewardEntry> availableEntries = rewardCatalog.getAvailableEntries(scenarioId, specialArea);

		if (availableEntries.isEmpty()) {
			throw new IllegalStateException("No exploration rewards are available for scenario: " + scenarioId);
		}

		ExplorationRewardEntry selectedEntry = selectWeightedEntry(availableEntries);

		int quantity = generateQuantity(selectedEntry);

		return new ExplorationReward(selectedEntry, quantity);
	}

	private ExplorationRewardEntry selectWeightedEntry(List<ExplorationRewardEntry> entries) {
		int totalWeight = 0;

		for (ExplorationRewardEntry entry : entries) {
			totalWeight += entry.getWeight();
		}

		int roll = random.nextInt(totalWeight);
		int accumulatedWeight = 0;

		for (ExplorationRewardEntry entry : entries) {
			accumulatedWeight += entry.getWeight();

			if (roll < accumulatedWeight) {
				return entry;
			}
		}

		throw new IllegalStateException("Exploration reward selection failed.");
	}

	private int generateQuantity(ExplorationRewardEntry entry) {
		int quantityRange = entry.getMaxQuantity() - entry.getMinQuantity() + 1;

		return random.nextInt(quantityRange) + entry.getMinQuantity();
	}
}