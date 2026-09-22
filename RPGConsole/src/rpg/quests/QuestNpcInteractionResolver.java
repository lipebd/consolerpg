package rpg.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import rpg.characters.Party;
import rpg.flags.CampaignFlags;
import rpg.requirements.RequirementCatalog;
import rpg.requirements.RequirementEvaluator;
import rpg.requirements.RequirementSetDefinition;

// Resolves the quest interactions currently available through one NPC.
public final class QuestNpcInteractionResolver {

    private final QuestCatalog questCatalog;
    private final QuestProgress questProgress;
    private final RequirementCatalog requirementCatalog;
    private final RequirementEvaluator requirementEvaluator;
    private final Party party;
    private final CampaignFlags campaignFlags;

    public QuestNpcInteractionResolver(
            QuestCatalog questCatalog,
            QuestProgress questProgress,
            RequirementCatalog requirementCatalog,
            RequirementEvaluator requirementEvaluator,
            Party party,
            CampaignFlags campaignFlags) {
        if (questCatalog == null) {
            throw new IllegalArgumentException("Quest catalog cannot be null.");
        }

        if (questProgress == null) {
            throw new IllegalArgumentException("Quest progress cannot be null.");
        }

        if (requirementCatalog == null) {
            throw new IllegalArgumentException("Requirement catalog cannot be null.");
        }

        if (requirementEvaluator == null) {
            throw new IllegalArgumentException("Requirement evaluator cannot be null.");
        }

        if (party == null) {
            throw new IllegalArgumentException("Party cannot be null.");
        }

        if (campaignFlags == null) {
            throw new IllegalArgumentException("Campaign flags cannot be null.");
        }

        this.questCatalog = questCatalog;
        this.questProgress = questProgress;
        this.requirementCatalog = requirementCatalog;
        this.requirementEvaluator = requirementEvaluator;
        this.party = party;
        this.campaignFlags = campaignFlags;
    }

    public List<QuestDefinition> getAvailableQuests(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            throw new IllegalArgumentException("NPC ID cannot be empty.");
        }

        String normalizedNpcId = npcId.trim();
        List<QuestDefinition> availableQuests = new ArrayList<>();

        for (QuestDefinition questDefinition : questCatalog.getAll()) {
            QuestStatus status = questProgress.getStatus(questDefinition.getId());

            switch (status) {
                case NOT_STARTED -> {
                    if (isAvailableOffer(questDefinition, normalizedNpcId)) {
                        availableQuests.add(questDefinition);
                    }
                }

                case ACTIVE -> {
                    if (isAvailableActiveInteraction(questDefinition, normalizedNpcId)) {
                        availableQuests.add(questDefinition);
                    }
                }

                case COMPLETED -> {
                    // Completed quests no longer create NPC interactions.
                }
            }
        }

        return List.copyOf(availableQuests);
    }

    private boolean isAvailableOffer(QuestDefinition questDefinition, String npcId) {
        if (!questDefinition.getStartingNpcId().equals(npcId)) {
            return false;
        }

        return isRequirementSatisfied(questDefinition.getAvailabilityRequirementSetId());
    }

    private boolean isAvailableActiveInteraction(QuestDefinition questDefinition, String npcId) {
        QuestStepDefinition currentStep =
                questProgress
                        .getCurrentStep(questDefinition.getId())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Active quest is missing its current step: "
                                                        + questDefinition.getId()));

        boolean matchingTalkObjective = false;

        for (QuestObjectiveDefinition objective : currentStep.getObjectives()) {
            if (objective.getObjectiveType() == QuestObjectiveType.TALK_TO_NPC) {
                matchingTalkObjective = objective.getTargetId().orElseThrow().equals(npcId);

                continue;
            }

            if (objective.getObjectiveType() == QuestObjectiveType.DELIVER_ITEM) {
                continue;
            }

            int currentProgress =
                    questProgress.getObjectiveProgress(questDefinition.getId(), objective.getId());

            if (currentProgress < objective.getRequiredQuantity()) {
                return false;
            }
        }

        return matchingTalkObjective;
    }

    private boolean isRequirementSatisfied(Optional<String> requirementSetId) {
        if (requirementSetId.isEmpty()) {
            return true;
        }

        RequirementSetDefinition requirementSet =
                requirementCatalog.getById(requirementSetId.orElseThrow());

        return requirementEvaluator.isSatisfied(
                requirementSet, party, questProgress, campaignFlags);
    }
}
