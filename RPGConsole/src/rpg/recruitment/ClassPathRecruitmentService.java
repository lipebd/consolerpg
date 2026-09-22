package rpg.recruitment;

import java.util.List;
import java.util.Objects;

import rpg.characters.Party;
import rpg.classes.paths.ClassPathTransitionPlan;
import rpg.classes.paths.ClassPathTransitionPreparation;
import rpg.classes.paths.ClassPathTransitionService;
import rpg.quests.QuestCompletionFailure;
import rpg.quests.QuestProgress;
import rpg.quests.QuestProgressResult;
import rpg.quests.QuestProgressService;
import rpg.quests.QuestStepDefinition;

// Composes the class-path ceremony, quest completion and first recruitment.
public final class ClassPathRecruitmentService {

	private final Party party;
	private final QuestProgress questProgress;
	private final QuestRecruitmentCatalog recruitmentCatalog;
	private final ClassPathTransitionService classPathTransitionService;
	private final AllyRecruitmentService allyRecruitmentService;
	private final QuestProgressService questProgressService;

	public ClassPathRecruitmentService(
			Party party,
			QuestProgress questProgress,
			QuestRecruitmentCatalog recruitmentCatalog,
			ClassPathTransitionService classPathTransitionService,
			AllyRecruitmentService allyRecruitmentService,
			QuestProgressService questProgressService
	) {
		this.party =
				Objects.requireNonNull(
						party,
						"Party cannot be null.");

		this.questProgress =
				Objects.requireNonNull(
						questProgress,
						"Quest progress cannot be null.");

		this.recruitmentCatalog =
				Objects.requireNonNull(
						recruitmentCatalog,
						"Quest recruitment catalog cannot be null.");

		this.classPathTransitionService =
				Objects.requireNonNull(
						classPathTransitionService,
						"Class path transition service cannot be null.");

		this.allyRecruitmentService =
				Objects.requireNonNull(
						allyRecruitmentService,
						"Ally recruitment service cannot be null.");

		this.questProgressService =
				Objects.requireNonNull(
						questProgressService,
						"Quest progress service cannot be null.");
	}

	public boolean hasRecruitmentForCurrentStep(String questId) {
	    return questProgress
	            .getCurrentStep(questId)
	            .map(
	                    currentStep ->
	                            recruitmentCatalog
	                                    .find(questId, currentStep.getStep())
	                                    .isPresent())
	            .orElse(false);
	}

	public ClassPathRecruitmentPreparation prepare(
			String questId,
			String npcId,
			String protagonistTargetClassId
	) {
		validateFirstAllyPosition();

		QuestRecruitmentDefinition recruitmentDefinition =
				getCurrentRecruitmentDefinition(questId);

		ClassPathTransitionPreparation classPathPreparation =
				classPathTransitionService.prepare(
						party,
						protagonistTargetClassId);

		if (!classPathPreparation.isReady()) {
			return ClassPathRecruitmentPreparation.blocked(
					classPathPreparation.getFailures(),
					List.of());
		}

		ClassPathTransitionPlan classPathPlan =
				classPathPreparation.getPlan();

		List<QuestCompletionFailure> questFailures =
				questProgressService
						.validateNpcInteractionCompletion(
								questId,
								npcId,
								classPathPlan
										.getInventoryAdditions());

		if (!questFailures.isEmpty()) {
			return ClassPathRecruitmentPreparation.blocked(
					List.of(),
					questFailures);
		}

		AllyRecruitmentPlan allyRecruitmentPlan =
				allyRecruitmentService.prepare(
						recruitmentDefinition
								.getCandidateId(),
						List.of(
								classPathPlan
										.getTargetClassDefinition()
										.getId()));

		ClassPathRecruitmentPlan plan =
				new ClassPathRecruitmentPlan(
						npcId,
						classPathPlan,
						allyRecruitmentPlan);

		return ClassPathRecruitmentPreparation.ready(plan);
	}

	public void validate(ClassPathRecruitmentPlan plan) {
		Objects.requireNonNull(
				plan,
				"Class path recruitment plan cannot be null.");

		validateFirstAllyPosition();

		QuestRecruitmentDefinition currentDefinition =
				getCurrentRecruitmentDefinition(
						plan.getQuestId());

		if (currentDefinition
				!= plan
						.getAllyRecruitmentPlan()
						.getRecruitmentDefinition()) {
			throw new IllegalStateException(
					"Quest recruitment definition changed after preparation."
			);
		}

		classPathTransitionService.validate(
				plan.getClassPathPlan());

		allyRecruitmentService.validate(
				plan.getAllyRecruitmentPlan());

		String protagonistTargetClassId =
				plan
						.getClassPathPlan()
						.getTargetClassDefinition()
						.getId();

		String allyClassId =
				plan
						.getAllyRecruitmentPlan()
						.getAlly()
						.getPlayableClassDefinition()
						.getId();

		if (protagonistTargetClassId.equals(allyClassId)) {
			throw new IllegalStateException(
					"Protagonist and first ally class conflict after preparation."
			);
		}

		List<QuestCompletionFailure> questFailures =
				questProgressService
						.validateNpcInteractionCompletion(
								plan.getQuestId(),
								plan.getNpcId(),
								plan
										.getClassPathPlan()
										.getInventoryAdditions());

		if (!questFailures.isEmpty()) {
			throw new IllegalStateException(
					"Quest completion became blocked after recruitment preparation."
			);
		}
	}

	public QuestProgressResult apply(
			ClassPathRecruitmentPlan plan
	) {
		validate(plan);

		classPathTransitionService.apply(
				plan.getClassPathPlan());

		QuestProgressResult questResult =
				questProgressService
						.completeNpcInteraction(
								plan.getQuestId(),
								plan.getNpcId());

		if (questResult.hasCompletionFailures()) {
			throw new IllegalStateException(
					"Validated quest completion failed during class path recruitment."
			);
		}

		allyRecruitmentService.apply(
				plan.getAllyRecruitmentPlan());

		return questResult;
	}

	private QuestRecruitmentDefinition
			getCurrentRecruitmentDefinition(
					String questId
			) {
		QuestStepDefinition currentStep =
				questProgress
						.getCurrentStep(questId)
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Quest is not active: "
														+ questId));

		return recruitmentCatalog
				.find(
						questId,
						currentStep.getStep())
				.orElseThrow(
						() ->
								new IllegalStateException(
										"Current quest step has no recruitment definition: "
												+ questId
												+ ", step "
												+ currentStep.getStep()));
	}

	private void validateFirstAllyPosition() {
		if (party.size() != Party.MINIMUM_SIZE) {
			throw new IllegalStateException(
					"Class path recruitment requires a party containing only the leader."
			);
		}
	}
}