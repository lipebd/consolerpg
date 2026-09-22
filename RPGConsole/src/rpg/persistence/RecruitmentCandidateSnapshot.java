package rpg.persistence;

import rpg.characters.CharacterNameRules;

// Stores one introduced recruitment candidate in a campaign snapshot.
public final class RecruitmentCandidateSnapshot {

	private final String candidateId;
	private final String name;
	private final boolean recruited;

	public RecruitmentCandidateSnapshot(
			String candidateId,
			String name,
			boolean recruited
	) {
		if (candidateId == null || candidateId.isBlank()) {
			throw new IllegalArgumentException(
					"Recruitment candidate ID cannot be null or blank."
			);
		}

		String validatedName = CharacterNameRules.requireValid(name);

		if (!validatedName.equals(name)) {
			throw new IllegalArgumentException(
					"Recruitment candidate name must already be normalized: "
							+ name
			);
		}

		this.candidateId = candidateId;
		this.name = name;
		this.recruited = recruited;
	}

	public String getCandidateId() {
		return candidateId;
	}

	public String getName() {
		return name;
	}

	public boolean isRecruited() {
		return recruited;
	}
}