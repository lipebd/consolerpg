package rpg.marketplace;

import java.util.Objects;

// Represents sale proceeds kept by the API until the seller claims them.
public record PendingReward(
		String id,
		String transactionId,
		int amount,
		RewardStatus status) {

	public PendingReward {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Reward ID cannot be empty.");
		}
		if (transactionId == null || transactionId.isBlank()) {
			throw new IllegalArgumentException("Transaction ID cannot be empty.");
		}
		if (amount <= 0) {
			throw new IllegalArgumentException("Reward amount must be positive.");
		}
		Objects.requireNonNull(status, "Reward status cannot be null.");
	}
}
