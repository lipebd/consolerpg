package rpg.ui;

import java.util.Objects;

import rpg.classes.PlayableClassDefinition;

// Formats playable-class information for console interfaces.
public final class PlayableClassDetailFormatter {

    private PlayableClassDetailFormatter() {}

    public static String formatPresentation(PlayableClassDefinition classDefinition) {
        Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.");

        return formatSummary(classDefinition)
                + "\n\nDescrição:\n"
                + classDefinition.getDescription();
    }

    public static String formatSpecializations(PlayableClassDefinition classDefinition) {
        Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.");

        return "["
                + classDefinition.getPrimaryAttribute()
                + " ↑↑ | "
                + classDefinition.getSecondaryAttribute()
                + " ↑]";
    }

    public static String formatSummary(PlayableClassDefinition classDefinition) {
        Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.");

        return "["
                + classDefinition.getSymbol()
                + "] "
                + classDefinition.getName()
                + "\nArmadura: "
                + classDefinition.getArmorWeight().getDisplayName()
                + "\n"
                + formatSpecializations(classDefinition);
    }
}
