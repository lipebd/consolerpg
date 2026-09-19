package rpg.classes.paths;

// Identifies an expected reason why a class-path transition cannot proceed.
public enum ClassPathTransitionFailureType {

    ALREADY_COMPLETED,
    INELIGIBLE_CURRENT_CLASS,
    LEVEL_TOO_LOW,
    ITEM_CAPACITY_EXCEEDED
}