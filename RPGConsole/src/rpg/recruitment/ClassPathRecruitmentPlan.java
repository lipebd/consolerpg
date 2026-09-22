package rpg.recruitment;

import java.util.Objects;

import rpg.classes.paths.ClassPathTransitionPlan;

// Stores the composed class-path and ally-recruitment transaction.
public final class ClassPathRecruitmentPlan {

	private final String npcId;
	private final ClassPathTransitionPlan classPathPlan;
	private final AllyRecruitmentPlan allyRecruitmentPlan;

	ClassPathRecruitmentPlan(
			String npcId,
			ClassPathTransitionPlan classPathPlan,
			AllyRecruitmentPlan allyRecruitmentPlan
	) {
		if (npcId == null || npcId.isBlank()) {
			throw new IllegalArgumentException(
					"Recruitment NPC ID cannot be empty."
			);
		}

		this.npcId = npcId.trim();

		this.classPathPlan =
				Objects.requireNonNull(
						classPathPlan,
						"Class path transition plan cannot be null.");

		this.allyRecruitmentPlan =
				Objects.requireNonNull(
						allyRecruitmentPlan,
						"Ally recruitment plan cannot be null.");

		validateClassDifference();
	}

	private void validateClassDifference() {
		String protagonistTargetClassId =
				classPathPlan
						.getTargetClassDefinition()
						.getId();

		String allyClassId =
				allyRecruitmentPlan
						.getAlly()
						.getPlayableClassDefinition()
						.getId();

		if (protagonistTargetClassId.equals(allyClassId)) {
			throw new IllegalArgumentException(
					"Protagonist and first ally cannot use the same final class."
			);
		}
	}

	public String getQuestId() {
		return allyRecruitmentPlan
				.getRecruitmentDefinition()
				.getQuestId();
	}

	public int getQuestStep() {
		return allyRecruitmentPlan
				.getRecruitmentDefinition()
				.getStep();
	}

	public String getNpcId() {
		return npcId;
	}

	public ClassPathTransitionPlan getClassPathPlan() {
		return classPathPlan;
	}

	public AllyRecruitmentPlan getAllyRecruitmentPlan() {
		return allyRecruitmentPlan;
	}
}