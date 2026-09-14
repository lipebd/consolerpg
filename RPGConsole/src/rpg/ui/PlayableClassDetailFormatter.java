package rpg.ui;

import java.util.Objects;

import rpg.classes.PlayableClassDefinition;

// Formats playable-class information for console interfaces.
public final class PlayableClassDetailFormatter {

	private PlayableClassDetailFormatter() {
	}

	public static String formatSpecializations(PlayableClassDefinition classDefinition) {
		Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.");

		return "[" + classDefinition.getPrimaryAttribute() + " ↑↑ | "
				+ classDefinition.getSecondaryAttribute() + " ↑]";
	}
}