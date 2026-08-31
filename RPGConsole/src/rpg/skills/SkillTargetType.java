package rpg.skills;

// Defines the possible targets of a skill effect.
public enum SkillTargetType {

    NONE,
    SELF,
    SINGLE_ALLY,
    ALL_ALLIES,
    ALL_BUT_SELF,
    RANDOM_ALLY,
    SINGLE_ENEMY,
    ALL_ENEMIES,
    RANDOM_ENEMY
}