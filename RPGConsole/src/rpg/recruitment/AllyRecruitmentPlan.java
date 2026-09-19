package rpg.recruitment;

import java.util.List;
import java.util.Objects;

import rpg.characters.CharacterNameRules;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;

// Stores one fully prepared ally recruitment plan.
public final class AllyRecruitmentPlan {

	private final Party party;
	private final List<PlayableCharacter> existingPartyMembers;
	private final QuestRecruitmentDefinition recruitmentDefinition;
	private final RecruitmentCandidateState candidateState;
	private final PlayableCharacter ally;

	AllyRecruitmentPlan(
			Party party,
			QuestRecruitmentDefinition recruitmentDefinition,
			RecruitmentCandidateState candidateState,
			PlayableCharacter ally
	) {
		this.party =
				Objects.requireNonNull(
						party,
						"Party cannot be null.");

		this.existingPartyMembers =
				List.copyOf(party.getMembers());

		this.recruitmentDefinition =
				Objects.requireNonNull(
						recruitmentDefinition,
						"Quest recruitment definition cannot be null.");

		this.candidateState =
				Objects.requireNonNull(
						candidateState,
						"Recruitment candidate state cannot be null.");

		this.ally =
				Objects.requireNonNull(
						ally,
						"Recruitment ally cannot be null.");

		validateCoherence();
	}

	private void validateCoherence() {
		if (!recruitmentDefinition
				.getCandidateId()
				.equals(candidateState.getCandidateId())) {
			throw new IllegalArgumentException(
					"Recruitment definition and candidate state must use the same ID."
			);
		}

		if (!CharacterNameRules.areEquivalent(
				candidateState.getName(),
				ally.getName())) {
			throw new IllegalArgumentException(
					"Recruitment candidate and ally must use the same name."
			);
		}

		if (recruitmentDefinition.getAllyLevel()
				!= ally.getLevel()) {
			throw new IllegalArgumentException(
					"Recruitment definition and ally must use the same level."
			);
		}
	}

	Party getParty() {
		return party;
	}

	List<PlayableCharacter> getExistingPartyMembers() {
		return existingPartyMembers;
	}

	public QuestRecruitmentDefinition getRecruitmentDefinition() {
		return recruitmentDefinition;
	}

	public RecruitmentCandidateState getCandidateState() {
		return candidateState;
	}

	public PlayableCharacter getAlly() {
		return ally;
	}
}