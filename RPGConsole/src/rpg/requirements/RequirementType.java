package rpg.requirements;

// Identifies a condition that must be satisfied by the current campaign state.
public enum RequirementType {

    LEADER_LEVEL_AT_LEAST,
    ITEM_QUANTITY_AT_LEAST,
    QUEST_NOT_STARTED,
    QUEST_ACTIVE,
    QUEST_COMPLETED,
    FLAG_SET,
    FLAG_UNSET
}