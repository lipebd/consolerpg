package rpg.recruitment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import rpg.characters.Party;

// Stores recruitment associations and provides candidate identity lookup.
public final class QuestRecruitmentCatalog {

    private static final int MAXIMUM_RECRUITMENTS = Party.MAXIMUM_SIZE - Party.MINIMUM_SIZE;

    private final Map<QuestStepKey, QuestRecruitmentDefinition> definitionsByQuestStep;
    private final Map<String, QuestRecruitmentDefinition> definitionsByCandidateId;

    public QuestRecruitmentCatalog() {
        definitionsByQuestStep = new LinkedHashMap<>();
        definitionsByCandidateId = new LinkedHashMap<>();
    }

    public void register(QuestRecruitmentDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Quest recruitment definition cannot be null.");
        }

        QuestStepKey questStepKey = QuestStepKey.from(definition);

        if (definitionsByQuestStep.containsKey(questStepKey)) {
            throw new IllegalArgumentException(
                    "Quest recruitment association is duplicated: "
                            + definition.getQuestId()
                            + ", step "
                            + definition.getStep()
                            + ".");
        }

        if (definitionsByCandidateId.containsKey(definition.getCandidateId())) {
            throw new IllegalArgumentException(
                    "Recruitment candidate ID is duplicated: " + definition.getCandidateId());
        }

        if (definitionsByQuestStep.size() >= MAXIMUM_RECRUITMENTS) {
            throw new IllegalArgumentException(
                    "Quest recruitment catalog cannot contain more than "
                            + MAXIMUM_RECRUITMENTS
                            + " definitions.");
        }

        definitionsByQuestStep.put(questStepKey, definition);
        definitionsByCandidateId.put(definition.getCandidateId(), definition);
    }

    public Optional<QuestRecruitmentDefinition> find(String questId, int step) {
        if (questId == null || questId.isBlank()) {
            throw new IllegalArgumentException("Quest ID cannot be empty.");
        }

        if (step <= 0) {
            throw new IllegalArgumentException("Recruitment quest step must be positive.");
        }

        return Optional.ofNullable(
                definitionsByQuestStep.get(new QuestStepKey(questId.trim(), step)));
    }

    public QuestRecruitmentDefinition getByCandidateId(String candidateId) {
        if (candidateId == null || candidateId.isBlank()) {
            throw new IllegalArgumentException("Recruitment candidate ID cannot be empty.");
        }

        String normalizedCandidateId = candidateId.trim();
        QuestRecruitmentDefinition definition = definitionsByCandidateId.get(normalizedCandidateId);

        if (definition == null) {
            throw new IllegalArgumentException(
                    "Recruitment candidate not found: " + normalizedCandidateId);
        }

        return definition;
    }

    public List<QuestRecruitmentDefinition> getAll() {
        return List.copyOf(definitionsByQuestStep.values());
    }

    private record QuestStepKey(String questId, int step) {

        private static QuestStepKey from(QuestRecruitmentDefinition definition) {
            return new QuestStepKey(definition.getQuestId(), definition.getStep());
        }
    }
}
