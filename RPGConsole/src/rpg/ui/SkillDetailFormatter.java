package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rpg.skills.SkillDefinition;
import rpg.skills.SkillEffectDefinition;
import rpg.skills.SkillEffectType;
import rpg.skills.SkillExecutionPhase;
import rpg.skills.SkillTargetType;

// Formats technical skill information for console interfaces.
public final class SkillDetailFormatter {

	private SkillDetailFormatter() {
	}

	public static String formatType(SkillDefinition skill) {
		SkillEffectDefinition mainEffect = getMainEffect(skill);

		return switch (mainEffect.getEffectType()) {
		case DAMAGE -> switch (mainEffect.getDamageType()) {
		case PHYSICAL -> "Físico";
		case MAGICAL -> "Mágico";
		};

		case RECOIL_DAMAGE -> "Recuo";

		case HP_RECOVERY -> "Cura";

		case MP_RECOVERY, SP_RECOVERY -> "Recuperação";

		case REVIVE -> "Ressurreição";

		case ATTRIBUTE_STAGE -> mainEffect.getStages() > 0 ? "Fortalecimento" : "Enfraquecimento";

		case PURIFICATION -> "Purificação";

		case DISSIPATION -> "Dissipação";

		case SPECIAL_STATE -> "Status";

		case DAMAGE_POWER_MODIFIER ->
			throw new IllegalStateException("A damage-power modifier cannot be " + "the main skill effect.");
		};
	}

	public static String formatPower(SkillDefinition skill) {
		Integer power = getMainEffect(skill).getPower();

		return power == null ? "--" : power.toString();
	}

	public static String formatTarget(SkillDefinition skill) {
		SkillTargetType targetType = getMainEffect(skill).getTargetType();

		return switch (targetType) {
		case SELF -> "O usuário";

		case SINGLE_ALLY -> "Um aliado";

		case ALL_ALLIES -> "Todos os aliados";

		case ALL_BUT_SELF -> "Todos os outros aliados";

		case RANDOM_ALLY -> "Um aliado aleatório";

		case SINGLE_ENEMY -> "Um inimigo";

		case ALL_ENEMIES -> "Todos os inimigos";

		case RANDOM_ENEMY -> "Um inimigo aleatório";
		};
	}

	public static String formatCost(SkillDefinition skill) {
		Objects.requireNonNull(skill, "Skill definition cannot be null.");

		List<String> costs = new ArrayList<>();

		if (skill.getMpCost() > 0) {
			costs.add(skill.getMpCost() + " MP");
		}

		if (skill.getSpCost() > 0) {
			costs.add(skill.getSpCost() + " SP");
		}

		if (costs.isEmpty()) {
			return "Nenhum";
		}

		return String.join(" | ", costs);
	}

	private static SkillEffectDefinition getMainEffect(SkillDefinition skill) {
		Objects.requireNonNull(skill, "Skill definition cannot be null.");

		return skill.getEffectsByPhase(SkillExecutionPhase.PRIMARY).stream()
				.filter(effect -> effect.getEffectType() != SkillEffectType.DAMAGE_POWER_MODIFIER).findFirst()
				.orElseGet(
						() -> skill.getEffects().stream()
								.filter(effect -> effect.getEffectType() != SkillEffectType.DAMAGE_POWER_MODIFIER)
								.findFirst()
								.orElseThrow(() -> new IllegalStateException("Skill has no " + "displayable effect."))
				);
	}

	public static String formatDetails(SkillDefinition skill) {
		Objects.requireNonNull(skill, "Skill definition cannot be null.");

		String lineSeparator = System.lineSeparator();

		return skill.getName() + " [Nv. " + skill.getAcquisitionLevel() + "]" + lineSeparator + lineSeparator + "Tipo: "
				+ formatType(skill) + lineSeparator + "Poder: " + formatPower(skill) + lineSeparator + "Alvo: "
				+ formatTarget(skill) + lineSeparator + "Custo: " + formatCost(skill) + lineSeparator + lineSeparator
				+ "Descrição:" + lineSeparator + skill.getDescription();
	}

}