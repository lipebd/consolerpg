package rpg.recruitment;

import rpg.characters.CharacterNameRules;

// Stores the persistent campaign identity of one recruitment candidate.
public final class RecruitmentCandidateState {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String candidateId;
    private final String name;
    private boolean recruited;

    public RecruitmentCandidateState(String candidateId, String name) {
        this.candidateId = validateCandidateId(candidateId);
        this.name = CharacterNameRules.requireValid(name);
        this.recruited = false;
    }

    private static String validateCandidateId(String candidateId) {
        if (candidateId == null || candidateId.isBlank()) {
            throw new IllegalArgumentException("Recruitment candidate ID cannot be empty.");
        }

        String trimmedCandidateId = candidateId.trim();

        if (!trimmedCandidateId.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    "Recruitment candidate ID must use lowercase letters, numbers and single underscores: "
                            + trimmedCandidateId);
        }

        return trimmedCandidateId;
    }

    void markRecruited() {
        if (recruited) {
            throw new IllegalStateException(
                    "Recruitment candidate is already recruited: " + candidateId);
        }

        recruited = true;
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
