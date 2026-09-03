package rpg.world.rewards;

import java.util.Objects;

// Represents one reward selected from an exploration pool.
public final class ExplorationReward {

	private final ExplorationRewardType type;
	private final String rewardId;
	private final int quantity;

	public ExplorationReward(ExplorationRewardEntry sourceEntry, int quantity) {
		Objects.requireNonNull(sourceEntry, "Source reward entry cannot be null.");

		if (quantity < sourceEntry.getMinQuantity() || quantity > sourceEntry.getMaxQuantity()) {
			throw new IllegalArgumentException("Generated reward quantity is outside " + "the configured range.");
		}

		this.type = sourceEntry.getType();
		this.rewardId = sourceEntry.getRewardId();
		this.quantity = quantity;
	}

	public ExplorationRewardType getType() {
		return type;
	}

	public String getRewardId() {
		return rewardId;
	}

	public int getQuantity() {
		return quantity;
	}
}