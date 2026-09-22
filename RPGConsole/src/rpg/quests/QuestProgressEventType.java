package rpg.quests;

// Identifies a confirmed gameplay event that may progress quests.
public enum QuestProgressEventType {

	NPC_TALKED(
			QuestObjectiveType.TALK_TO_NPC,
			true,
			false),
	ENEMY_DEFEATED(
			QuestObjectiveType.DEFEAT_ENEMY,
			true,
			true),
	ENEMY_ANALYZED(
			QuestObjectiveType.ANALYZE_ENEMY,
			true,
			false),
	BATTLE_WON(
	        QuestObjectiveType.WIN_BATTLE,
	        true,
	        false),
	ITEM_OBTAINED(
			QuestObjectiveType.OBTAIN_ITEM,
			true,
			true),
	SCENARIO_REACHED(
			QuestObjectiveType.REACH_SCENARIO,
			true,
			false),
	DUNGEON_COMPLETED(
			QuestObjectiveType.COMPLETE_DUNGEON,
			true,
			false);

	private final QuestObjectiveType objectiveType;
	private final boolean targetRequired;
	private final boolean variableQuantityAllowed;

	QuestProgressEventType(
			QuestObjectiveType objectiveType,
			boolean targetRequired,
			boolean variableQuantityAllowed) {
		this.objectiveType = objectiveType;
		this.targetRequired = targetRequired;
		this.variableQuantityAllowed = variableQuantityAllowed;
	}

	public QuestObjectiveType getObjectiveType() {
		return objectiveType;
	}

	public boolean isTargetRequired() {
		return targetRequired;
	}

	public boolean isVariableQuantityAllowed() {
		return variableQuantityAllowed;
	}
}