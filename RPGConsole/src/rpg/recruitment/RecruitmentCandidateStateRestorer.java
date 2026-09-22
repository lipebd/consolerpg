package rpg.recruitment;

import rpg.characters.CharacterNameRules;

// Reconstructs validated candidate state without introducing or recruiting through gameplay.
public final class RecruitmentCandidateStateRestorer {

	private final RecruitmentCandidateStateCatalog candidateStateCatalog;
	private boolean finished;

	public RecruitmentCandidateStateRestorer(
			QuestRecruitmentCatalog recruitmentCatalog
	) {
		this.candidateStateCatalog =
				new RecruitmentCandidateStateCatalog(
						recruitmentCatalog
				);

		this.finished = false;
	}

	public void restoreCandidate(
			String candidateId,
			String name,
			boolean recruited
	) {
		requireOpen();

		String validatedName =
				CharacterNameRules.requireValid(name);

		if (!validatedName.equals(name)) {
			throw new IllegalArgumentException(
					"Recruitment candidate name must already be normalized: "
							+ name
			);
		}

		RecruitmentCandidateState candidateState =
				new RecruitmentCandidateState(
						candidateId,
						validatedName
				);

		if (recruited) {
			candidateState.markRecruited();
		}

		candidateStateCatalog.register(candidateState);
	}

	public RecruitmentCandidateStateCatalog finish() {
		requireOpen();

		finished = true;
		return candidateStateCatalog;
	}

	private void requireOpen() {
		if (finished) {
			throw new IllegalStateException(
					"Recruitment candidate restoration is already finished."
			);
		}
	}
}