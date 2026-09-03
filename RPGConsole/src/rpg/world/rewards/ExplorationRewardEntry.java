package rpg.world.rewards;

import java.util.Objects;

import rpg.world.exploration.ExplorationAreaRestriction;

// Defines one weighted reward entry available during exploration.
public final class ExplorationRewardEntry {

	private final ExplorationRewardType type;
	private final String rewardId;
	private final int minQuantity;
	private final int maxQuantity;
	private final int weight;
	private final ExplorationAreaRestriction areaRestriction;

	public ExplorationRewardEntry(ExplorationRewardType type, String rewardId, int minQuantity, int maxQuantity,
			int weight, ExplorationAreaRestriction areaRestriction) {
		this.type = Objects.requireNonNull(type, "Reward type cannot be null.");

		String normalizedRewardId = rewardId == null ? "" : rewardId.trim();

		if (type == ExplorationRewardType.ITEM && normalizedRewardId.isEmpty()) {
			throw new IllegalArgumentException("Item reward ID cannot be empty.");
		}

		if (type == ExplorationRewardType.GOLD && !normalizedRewardId.isEmpty()) {
			throw new IllegalArgumentException("Gold rewards cannot have a reward ID.");
		}

		if (minQuantity < 1) {
			throw new IllegalArgumentException("Minimum reward quantity must be at least one.");
		}

		if (maxQuantity < minQuantity) {
			throw new IllegalArgumentException(
					"Maximum reward quantity cannot be lower than the minimum quantity.");
		}

		if (weight <= 0) {
			throw new IllegalArgumentException("Reward weight must be greater than zero.");
		}

		this.rewardId = normalizedRewardId;
		this.minQuantity = minQuantity;
		this.maxQuantity = maxQuantity;
		this.weight = weight;
		this.areaRestriction = Objects.requireNonNull(areaRestriction, "Area restriction cannot be null.");
	}

	public boolean isAvailableIn(boolean specialArea) {
		return areaRestriction.allows(specialArea);
	}

	public ExplorationRewardType getType() {
		return type;
	}

	public String getRewardId() {
		return rewardId;
	}

	public int getMinQuantity() {
		return minQuantity;
	}

	public int getMaxQuantity() {
		return maxQuantity;
	}

	public int getWeight() {
		return weight;
	}

	public ExplorationAreaRestriction getAreaRestriction() {
		return areaRestriction;
	}
}