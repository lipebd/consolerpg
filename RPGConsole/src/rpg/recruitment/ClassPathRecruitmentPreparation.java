package rpg.recruitment;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import rpg.classes.paths.ClassPathTransitionFailure;
import rpg.quests.QuestCompletionFailure;

// Stores either a composed transaction plan or expected failures.
public final class ClassPathRecruitmentPreparation {

	private final Optional<ClassPathRecruitmentPlan> plan;
	private final List<ClassPathTransitionFailure> classPathFailures;
	private final List<QuestCompletionFailure> questCompletionFailures;

	private ClassPathRecruitmentPreparation(
			Optional<ClassPathRecruitmentPlan> plan,
			List<ClassPathTransitionFailure> classPathFailures,
			List<QuestCompletionFailure> questCompletionFailures
	) {
		if (plan == null) {
			throw new IllegalArgumentException(
					"Class path recruitment plan cannot be null."
			);
		}

		this.plan = plan;

		this.classPathFailures =
				List.copyOf(
						Objects.requireNonNull(
								classPathFailures,
								"Class path failures cannot be null."));

		this.questCompletionFailures =
				List.copyOf(
						Objects.requireNonNull(
								questCompletionFailures,
								"Quest completion failures cannot be null."));

		boolean hasFailures =
				!this.classPathFailures.isEmpty()
						|| !this.questCompletionFailures.isEmpty();

		if (plan.isPresent() == hasFailures) {
			throw new IllegalArgumentException(
					"Class path recruitment preparation must contain "
							+ "either one plan or failures."
			);
		}
	}

	public static ClassPathRecruitmentPreparation ready(
			ClassPathRecruitmentPlan plan
	) {
		return new ClassPathRecruitmentPreparation(
				Optional.of(
						Objects.requireNonNull(
								plan,
								"Class path recruitment plan cannot be null.")),
				List.of(),
				List.of());
	}

	public static ClassPathRecruitmentPreparation blocked(
			List<ClassPathTransitionFailure> classPathFailures,
			List<QuestCompletionFailure> questCompletionFailures
	) {
		if ((classPathFailures == null
					|| classPathFailures.isEmpty())
				&& (questCompletionFailures == null
					|| questCompletionFailures.isEmpty())) {
			throw new IllegalArgumentException(
					"Blocked class path recruitment must contain failures."
			);
		}

		return new ClassPathRecruitmentPreparation(
				Optional.empty(),
				classPathFailures,
				questCompletionFailures);
	}

	public boolean isReady() {
		return plan.isPresent();
	}

	public ClassPathRecruitmentPlan getPlan() {
		return plan.orElseThrow(
				() ->
						new IllegalStateException(
								"Blocked class path recruitment has no plan."));
	}

	public List<ClassPathTransitionFailure> getClassPathFailures() {
		return classPathFailures;
	}

	public List<QuestCompletionFailure> getQuestCompletionFailures() {
		return questCompletionFailures;
	}
}