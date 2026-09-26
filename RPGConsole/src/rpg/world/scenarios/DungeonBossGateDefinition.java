package rpg.world.scenarios;

// Stores one persistent narrative gate applied to a dungeon boss.
public final class DungeonBossGateDefinition {

	private final String dungeonId;
	private final String availabilityRequirementSetId;
	private final String defaultBlockedDialogueId;
	private final String blockedDestinationScenarioId;

	public DungeonBossGateDefinition(
			String dungeonId,
			String availabilityRequirementSetId,
			String defaultBlockedDialogueId,
			String blockedDestinationScenarioId
	) {
		this.dungeonId = requireId(dungeonId, "Dungeon");
		this.availabilityRequirementSetId = requireId(availabilityRequirementSetId, "Availability requirement set");
		this.defaultBlockedDialogueId = requireId(defaultBlockedDialogueId, "Default blocked dialogue");
		this.blockedDestinationScenarioId = requireId(blockedDestinationScenarioId, "Blocked destination scenario");

		if (this.dungeonId.equals(this.blockedDestinationScenarioId)) {
			throw new IllegalArgumentException(
					"Dungeon boss gate cannot transport the party to the same dungeon: " + this.dungeonId
			);
		}
	}

	private static String requireId(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
		}

		return value.trim();
	}

	public String getDungeonId() {
		return dungeonId;
	}

	public String getAvailabilityRequirementSetId() {
		return availabilityRequirementSetId;
	}

	public String getDefaultBlockedDialogueId() {
		return defaultBlockedDialogueId;
	}

	public String getBlockedDestinationScenarioId() {
		return blockedDestinationScenarioId;
	}
}