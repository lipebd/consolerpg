package rpg.recruitment;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import rpg.characters.CharacterNameRules;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.classes.PlayableClassDefinition;
import rpg.classes.paths.ClassPathRules;

// Prepares, validates and applies recruitment for any ally.
public final class AllyRecruitmentService {

	private final Party party;
	private final QuestRecruitmentCatalog recruitmentCatalog;
	private final RecruitmentCandidateStateCatalog candidateStateCatalog;
	private final AllyClassSelector allyClassSelector;
	private final RecruitmentAllyFactory allyFactory;

	public AllyRecruitmentService(
			Party party,
			QuestRecruitmentCatalog recruitmentCatalog,
			RecruitmentCandidateStateCatalog candidateStateCatalog,
			AllyClassSelector allyClassSelector,
			RecruitmentAllyFactory allyFactory
	) {
		this.party =
				Objects.requireNonNull(
						party,
						"Party cannot be null.");

		this.recruitmentCatalog =
				Objects.requireNonNull(
						recruitmentCatalog,
						"Quest recruitment catalog cannot be null.");

		this.candidateStateCatalog =
				Objects.requireNonNull(
						candidateStateCatalog,
						"Recruitment candidate state catalog cannot be null.");

		this.allyClassSelector =
				Objects.requireNonNull(
						allyClassSelector,
						"Ally class selector cannot be null.");

		this.allyFactory =
				Objects.requireNonNull(
						allyFactory,
						"Recruitment ally factory cannot be null.");
	}

	public AllyRecruitmentPlan prepare(String candidateId) {
		return prepare(candidateId, List.of());
	}

	public AllyRecruitmentPlan prepare(
			String candidateId,
			Collection<String> additionalExcludedClassIds
	) {
		QuestRecruitmentDefinition recruitmentDefinition =
				recruitmentCatalog.getByCandidateId(candidateId);

		RecruitmentCandidateState candidateState =
				candidateStateCatalog.getByCandidateId(
						recruitmentDefinition.getCandidateId());

		if (candidateState.isRecruited()) {
			throw new IllegalStateException(
					"Recruitment candidate is already recruited: "
							+ candidateState.getCandidateId()
			);
		}

		if (!party.hasAvailableSlot()) {
			throw new IllegalStateException(
					"Party has no available slot for recruitment."
			);
		}

		PlayableClassDefinition allyClass =
				allyClassSelector.selectFor(
						party,
						additionalExcludedClassIds);

		PlayableCharacter ally =
				allyFactory.create(
						candidateState,
						allyClass.getId(),
						recruitmentDefinition.getAllyLevel());

		if (!party.canAddMember(ally)) {
			throw new IllegalStateException(
					"Prepared ally cannot be added to the party: "
							+ ally.getName()
			);
		}

		return new AllyRecruitmentPlan(
				party,
				recruitmentDefinition,
				candidateState,
				ally);
	}

	public void validate(AllyRecruitmentPlan plan) {
		Objects.requireNonNull(
				plan,
				"Ally recruitment plan cannot be null.");

		if (plan.getParty() != party) {
			throw new IllegalStateException(
					"Recruitment plan belongs to another party."
			);
		}

		if (!party.getMembers().equals(
				plan.getExistingPartyMembers())) {
			throw new IllegalStateException(
					"Party composition changed after recruitment preparation."
			);
		}

		QuestRecruitmentDefinition currentDefinition =
				recruitmentCatalog.getByCandidateId(
						plan
								.getRecruitmentDefinition()
								.getCandidateId());

		if (currentDefinition
				!= plan.getRecruitmentDefinition()) {
			throw new IllegalStateException(
					"Quest recruitment definition changed after preparation."
			);
		}

		RecruitmentCandidateState currentCandidateState =
				candidateStateCatalog.getByCandidateId(
						currentDefinition.getCandidateId());

		if (currentCandidateState
				!= plan.getCandidateState()) {
			throw new IllegalStateException(
					"Recruitment candidate state changed after preparation."
			);
		}

		if (currentCandidateState.isRecruited()) {
			throw new IllegalStateException(
					"Recruitment candidate is already recruited: "
							+ currentCandidateState.getCandidateId()
			);
		}

		PlayableCharacter ally = plan.getAlly();

		if (!CharacterNameRules.areEquivalent(
				ally.getName(),
				currentCandidateState.getName())) {
			throw new IllegalStateException(
					"Prepared ally name no longer matches the candidate."
			);
		}

		if (ally.getLevel()
				!= currentDefinition.getAllyLevel()) {
			throw new IllegalStateException(
					"Prepared ally level no longer matches the recruitment definition."
			);
		}

		String allyClassId =
				ally.getPlayableClassDefinition().getId();

		if (allyClassId.equals(
					ClassPathRules.BEGINNER_CLASS_ID)
				|| allyClassId.equals(
						ClassPathRules.FINAL_FLAGELO_CLASS_ID)) {
			throw new IllegalStateException(
					"Prepared ally class is not eligible: "
							+ allyClassId
			);
		}

		for (PlayableCharacter member : party.getMembers()) {
			if (member
					.getPlayableClassDefinition()
					.getId()
					.equals(allyClassId)) {
				throw new IllegalStateException(
						"Prepared ally class is already present in the party: "
								+ allyClassId
				);
			}
		}

		if (!party.canAddMember(ally)) {
			throw new IllegalStateException(
					"Prepared ally can no longer be added to the party: "
							+ ally.getName()
			);
		}
	}

	public void apply(AllyRecruitmentPlan plan) {
		validate(plan);

		if (!party.addMember(plan.getAlly())) {
			throw new IllegalStateException(
					"Validated ally could not be added to the party."
			);
		}

		plan.getCandidateState().markRecruited();
	}
}