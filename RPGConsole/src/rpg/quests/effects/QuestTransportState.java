package rpg.quests.effects;

import java.util.Optional;

// Stores one immediate scenario transport requested by quest progression.
public final class QuestTransportState {

	private String pendingDestinationScenarioId;

	public QuestTransportState() {
		pendingDestinationScenarioId = null;
	}

	public void request(String destinationScenarioId) {
		if (destinationScenarioId == null || destinationScenarioId.isBlank()) {
			throw new IllegalArgumentException("Quest transport destination scenario ID cannot be empty.");
		}

		if (pendingDestinationScenarioId != null) {
			throw new IllegalStateException(
					"Another quest transport is already pending: " + pendingDestinationScenarioId
			);
		}

		pendingDestinationScenarioId = destinationScenarioId.trim();
	}

	public Optional<String> consume() {
		if (pendingDestinationScenarioId == null) {
			return Optional.empty();
		}

		String destinationScenarioId = pendingDestinationScenarioId;

		pendingDestinationScenarioId = null;

		return Optional.of(destinationScenarioId);
	}

	public boolean hasPendingTransport() {
		return pendingDestinationScenarioId != null;
	}
}