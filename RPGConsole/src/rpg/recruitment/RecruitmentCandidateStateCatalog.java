package rpg.recruitment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import rpg.characters.CharacterNameRules;

// Stores recruitment-candidate state for one campaign.
public final class RecruitmentCandidateStateCatalog {

    private final QuestRecruitmentCatalog recruitmentCatalog;
    private final Map<String, RecruitmentCandidateState> statesByCandidateId;

    public RecruitmentCandidateStateCatalog(QuestRecruitmentCatalog recruitmentCatalog) {
        if (recruitmentCatalog == null) {
            throw new IllegalArgumentException("Quest recruitment catalog cannot be null.");
        }

        this.recruitmentCatalog = recruitmentCatalog;
        this.statesByCandidateId = new LinkedHashMap<>();
    }

    void register(RecruitmentCandidateState candidateState) {
        if (candidateState == null) {
            throw new IllegalArgumentException("Recruitment candidate state cannot be null.");
        }

        String candidateId = candidateState.getCandidateId();

        recruitmentCatalog.getByCandidateId(candidateId);

        if (statesByCandidateId.containsKey(candidateId)) {
            throw new IllegalArgumentException(
                    "Recruitment candidate state is already registered: " + candidateId);
        }

        boolean nameAlreadyUsed =
                statesByCandidateId.values().stream()
                        .anyMatch(
                                existingState ->
                                        CharacterNameRules.areEquivalent(
                                                existingState.getName(), candidateState.getName()));

        if (nameAlreadyUsed) {
            throw new IllegalArgumentException(
                    "Recruitment candidate name is already in use: " + candidateState.getName());
        }

        statesByCandidateId.put(candidateId, candidateState);
    }

    public Optional<RecruitmentCandidateState> findByCandidateId(String candidateId) {
        QuestRecruitmentDefinition definition = recruitmentCatalog.getByCandidateId(candidateId);

        return Optional.ofNullable(statesByCandidateId.get(definition.getCandidateId()));
    }

    public RecruitmentCandidateState getByCandidateId(String candidateId) {
        QuestRecruitmentDefinition definition = recruitmentCatalog.getByCandidateId(candidateId);

        RecruitmentCandidateState candidateState =
                statesByCandidateId.get(definition.getCandidateId());

        if (candidateState == null) {
            throw new IllegalArgumentException(
                    "Recruitment candidate state not found: " + definition.getCandidateId());
        }

        return candidateState;
    }

    public List<RecruitmentCandidateState> getAll() {
        return List.copyOf(statesByCandidateId.values());
    }
}
