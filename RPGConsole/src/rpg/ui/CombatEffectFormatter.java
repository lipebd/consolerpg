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

	public static String formatCompactEffectSummary(GameCharacter character, int width) {
		Objects.requireNonNull(character, "Game character cannot be null.");

		if (width < 2) {
			throw new IllegalArgumentException("Effect summary width must be at least two.");
		}

		List<String> effects = new ArrayList<>();

		for (CharacterAttribute attribute : CharacterAttribute.values()) {
			int positive = character.getPositiveAttributeStages(attribute);
			int negative = character.getNegativeAttributeStages(attribute);

			if (positive > 0 || negative > 0) {
				effects.add(attribute.name().substring(0, 1) + repeatArrow('↑', positive) + repeatArrow('↓', negative));
			}
		}

		if (character.hasDistortedAttributes()) {
			effects.add("A⇄M D⇄R");
		}

		if (effects.isEmpty()) {
			return "--";
		}

		List<String> visible = new ArrayList<>();

		for (int index = 0; index < effects.size(); index++) {
			int remaining = effects.size() - index - 1;
			String candidate = joinWithRemainder(visible, effects.get(index), remaining);

			if (ConsoleUI.getVisibleLength(candidate) > width) {
				break;
			}

			visible.add(effects.get(index));
		}

		int hidden = effects.size() - visible.size();
		String summary = String.join(" ", visible);

		if (hidden > 0) {
			String remainder = "+" + hidden;
			summary = summary.isEmpty() ? remainder : summary + " " + remainder;
		}

		return summary;
	}

	private static String joinWithRemainder(List<String> visible, String nextEffect, int remaining) {
		String joined = visible.isEmpty() ? nextEffect : String.join(" ", visible) + " " + nextEffect;

		return remaining == 0 ? joined : joined + " +" + remaining;
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

	public static String formatEffectCountSummary(GameCharacter character) {
		Objects.requireNonNull(character, "Game character cannot be null.");

		int bonuses = 0;
		int penalties = 0;

		for (CharacterAttribute attribute : CharacterAttribute.values()) {
			if (character.getPositiveAttributeStages(attribute) > 0) {
				bonuses++;
			}
			if (character.getNegativeAttributeStages(attribute) > 0) {
				penalties++;
			}
		}

		List<String> parts = new ArrayList<>();

		if (bonuses > 0) {
			parts.add(bonuses + " bônus");
		}
		if (penalties > 0) {
			parts.add(penalties + (penalties == 1 ? " penalidade" : " penalidades"));
		}

		String summary = String.join(", ", parts);

		if (character.hasDistortedAttributes()) {
			return summary.isEmpty() ? "Distorção" : "Distorção e " + summary;
		}

		return summary.isEmpty() ? "Sem efeitos" : summary;
	}

	public static List<String> formatStatusEffectLines(GameCharacter character) {
		Objects.requireNonNull(character, "Game character cannot be null.");

		String firstLine = String.join(
				" | ",
				formatStatusAttribute(character, CharacterAttribute.ATK),
				formatStatusAttribute(character, CharacterAttribute.DEF),
				formatStatusAttribute(character, CharacterAttribute.MAG)
		);

		String secondLine = String.join(
				" | ",
				formatStatusAttribute(character, CharacterAttribute.RES),
				formatStatusAttribute(character, CharacterAttribute.VEL),
				formatStatusAttribute(character, CharacterAttribute.LCK)
		);

		return List.of(firstLine, secondLine);
	}

	private static String formatStatusAttribute(GameCharacter character, CharacterAttribute attribute) {
		String abbreviation = attribute.name().substring(0, 1);
		int positive = character.getPositiveAttributeStages(attribute);
		int negative = character.getNegativeAttributeStages(attribute);

		if (positive == 0 && negative == 0) {
			return abbreviation + "--";
		}

		StringBuilder summary = new StringBuilder(abbreviation);

		if (positive > 0) {
			summary.append(repeatArrow('↑', positive));

			if (attribute != CharacterAttribute.VEL) {
				summary.append('(').append(character.getPositiveAttributeRemainingTurns(attribute)).append(')');
			}
		}

		if (negative > 0) {
			summary.append(repeatArrow('↓', negative));

			if (attribute != CharacterAttribute.VEL) {
				summary.append('(').append(character.getNegativeAttributeRemainingTurns(attribute)).append(')');
			}
		}

		return summary.toString();
	}
}