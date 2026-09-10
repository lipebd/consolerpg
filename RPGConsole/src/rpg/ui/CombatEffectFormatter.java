package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import rpg.characters.CharacterAttribute;
import rpg.characters.GameCharacter;

// Formats active attribute effects for battle interfaces.
public final class CombatEffectFormatter {

	private static final int ATTRIBUTES_PER_LINE = 3;

	private CombatEffectFormatter() {
	}

	public static List<String> formatActiveEffectLines(GameCharacter character) {
		Objects.requireNonNull(character, "Game character cannot be null.");

		List<String> attributeSummaries = new ArrayList<>();

		for (CharacterAttribute attribute : CharacterAttribute.values()) {

			String summary = formatAttributeSummary(character, attribute);

			if (!summary.isEmpty()) {
				attributeSummaries.add(summary);
			}
		}

		List<String> lines = new ArrayList<>();

		for (int startIndex = 0; startIndex < attributeSummaries.size(); startIndex += ATTRIBUTES_PER_LINE) {
			int endIndex = Math.min(startIndex + ATTRIBUTES_PER_LINE, attributeSummaries.size());

			lines.add(String.join(" | ", attributeSummaries.subList(startIndex, endIndex)));
		}

		if (character.hasDistortedAttributes()) {
			lines.add("Distorção Ancestral: ATK ⇄ MAG | DEF ⇄ RES");
		}

		return List.copyOf(lines);
	}

	private static String formatAttributeSummary(GameCharacter character, CharacterAttribute attribute) {
		int positiveStages = character.getPositiveAttributeStages(attribute);

		int negativeStages = character.getNegativeAttributeStages(attribute);

		if (positiveStages == 0 && negativeStages == 0) {
			return "";
		}

		if (attribute == CharacterAttribute.VEL) {
			return formatSpeedSummary(positiveStages, negativeStages);
		}

		StringJoiner states = new StringJoiner(" ");

		if (positiveStages > 0) {
			states.add(formatTimedState('↑', positiveStages, character.getPositiveAttributeRemainingTurns(attribute)));
		}

		if (negativeStages > 0) {
			states.add(formatTimedState('↓', negativeStages, character.getNegativeAttributeRemainingTurns(attribute)));
		}

		return attribute.name() + " " + states;
	}

	private static String formatSpeedSummary(int positiveStages, int negativeStages) {
		StringJoiner states = new StringJoiner(" ");

		if (positiveStages > 0) {
			states.add(repeatArrow('↑', positiveStages));
		}

		if (negativeStages > 0) {
			states.add(repeatArrow('↓', negativeStages));
		}

		return CharacterAttribute.VEL.name() + " " + states + " (próxima rodada)";
	}

	private static String formatTimedState(char arrow, int stages, int remainingTurns) {
		return repeatArrow(arrow, stages) + " (" + remainingTurns + ")";
	}

	private static String repeatArrow(char arrow, int quantity) {
		return String.valueOf(arrow).repeat(quantity);
	}
}