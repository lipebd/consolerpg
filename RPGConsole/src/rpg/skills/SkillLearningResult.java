package rpg.skills;

// Describes the outcome of one skill-learning attempt.
public enum SkillLearningResult {

        LEARNED,
        REPLACED,
        ALREADY_LEARNED,
        INCOMPATIBLE_CLASS,
        LEVEL_TOO_LOW,
        SCROLL_LIMIT_REACHED
}