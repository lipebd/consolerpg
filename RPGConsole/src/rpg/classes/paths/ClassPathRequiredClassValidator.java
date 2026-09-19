package rpg.classes.paths;

import java.util.Objects;

import rpg.characters.CharacterStats;
import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassDefinition;

// Validates the two canonical playable classes required by class-path progression.
public final class ClassPathRequiredClassValidator {

    private ClassPathRequiredClassValidator() {}

    public static void validate(PlayableClassCatalog playableClassCatalog) {
        Objects.requireNonNull(playableClassCatalog, "Playable class catalog cannot be null.");

        PlayableClassDefinition beginnerDefinition =
                getRequiredClass(
                        playableClassCatalog, ClassPathRules.BEGINNER_CLASS_ID, "beginner Flagelo");

        PlayableClassDefinition finalDefinition =
                getRequiredClass(
                        playableClassCatalog,
                        ClassPathRules.FINAL_FLAGELO_CLASS_ID,
                        "final Flagelo");

        validateSharedPresentation(beginnerDefinition, finalDefinition);
        validateSharedProgressionRules(beginnerDefinition, finalDefinition);
    }

    private static PlayableClassDefinition getRequiredClass(
            PlayableClassCatalog playableClassCatalog, String classId, String classRole) {
        try {
            return playableClassCatalog.getById(classId);

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Required " + classRole + " playable class is missing: " + classId, exception);
        }
    }

    private static void validateSharedPresentation(
            PlayableClassDefinition beginnerDefinition, PlayableClassDefinition finalDefinition) {
        if (!beginnerDefinition.getName().equals(finalDefinition.getName())) {
            throw incompatibleDefinitions(
                    "the same player-facing name", beginnerDefinition, finalDefinition);
        }

        if (!beginnerDefinition.getSymbol().equals(finalDefinition.getSymbol())) {
            throw incompatibleDefinitions(
                    "the same player-facing symbol", beginnerDefinition, finalDefinition);
        }
    }

    private static void validateSharedProgressionRules(
            PlayableClassDefinition beginnerDefinition, PlayableClassDefinition finalDefinition) {
        if (!haveSameBaseStats(beginnerDefinition.getBaseStats(), finalDefinition.getBaseStats())) {
            throw incompatibleDefinitions(
                    "the same base statistics", beginnerDefinition, finalDefinition);
        }

        if (beginnerDefinition.getPrimaryAttribute() != finalDefinition.getPrimaryAttribute()
                || beginnerDefinition.getSecondaryAttribute()
                        != finalDefinition.getSecondaryAttribute()) {
            throw incompatibleDefinitions(
                    "the same primary and secondary attributes",
                    beginnerDefinition,
                    finalDefinition);
        }

        if (beginnerDefinition.getArmorWeight() != finalDefinition.getArmorWeight()) {
            throw incompatibleDefinitions(
                    "the same armor weight", beginnerDefinition, finalDefinition);
        }
    }

    private static boolean haveSameBaseStats(
            CharacterStats firstStats, CharacterStats secondStats) {
        return firstStats.getMaxHp() == secondStats.getMaxHp()
                && firstStats.getMaxMp() == secondStats.getMaxMp()
                && firstStats.getMaxSp() == secondStats.getMaxSp()
                && firstStats.getAttack() == secondStats.getAttack()
                && firstStats.getDefense() == secondStats.getDefense()
                && firstStats.getMagic() == secondStats.getMagic()
                && firstStats.getResistance() == secondStats.getResistance()
                && firstStats.getSpeed() == secondStats.getSpeed()
                && firstStats.getLuck() == secondStats.getLuck();
    }

    private static IllegalArgumentException incompatibleDefinitions(
            String requiredSimilarity,
            PlayableClassDefinition beginnerDefinition,
            PlayableClassDefinition finalDefinition) {
        return new IllegalArgumentException(
                "Required playable classes "
                        + beginnerDefinition.getId()
                        + " and "
                        + finalDefinition.getId()
                        + " must have "
                        + requiredSimilarity
                        + ".");
    }
}
