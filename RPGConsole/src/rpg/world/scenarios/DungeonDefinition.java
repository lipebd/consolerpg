package rpg.world.scenarios;

// Stores reusable data specific to dungeons.
public final class DungeonDefinition {

	private final String scenarioId;
	private final String bossEnemyId;
	private final String epicEnemyId;

	public DungeonDefinition(String scenarioId, String bossEnemyId, String epicEnemyId) {
		if (scenarioId == null || scenarioId.isBlank()) {
			throw new IllegalArgumentException("Scenario ID cannot be empty.");
		}

		if (bossEnemyId == null || bossEnemyId.isBlank()) {
			throw new IllegalArgumentException("Boss enemy ID cannot be empty.");
		}

		if (epicEnemyId == null || epicEnemyId.isBlank()) {
			throw new IllegalArgumentException("Epic enemy ID cannot be empty.");
		}

		if (bossEnemyId.equals(epicEnemyId)) {
			throw new IllegalArgumentException("Boss and epic enemy IDs must be different.");
		}

		this.scenarioId = scenarioId;
		this.bossEnemyId = bossEnemyId;
		this.epicEnemyId = epicEnemyId;
	}

	public String getScenarioId() {
		return scenarioId;
	}

	public String getBossEnemyId() {
		return bossEnemyId;
	}

	public String getEpicEnemyId() {
		return epicEnemyId;
	}
}