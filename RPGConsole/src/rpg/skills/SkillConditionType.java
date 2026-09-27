package rpg.skills;

// Defines an optional condition for executing a skill component.
public enum SkillConditionType {

    NONE,
    SELF_HP_LESS_THAN_PERCENT,
    SELF_HP_GREATER_THAN_PERCENT,
    TARGET_HP_LESS_THAN_PERCENT,
    TARGET_HP_GREATER_THAN_PERCENT,
    SELF_GREATER,
    SELF_LOWER
}