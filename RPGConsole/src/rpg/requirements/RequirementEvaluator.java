package rpg.requirements;

import rpg.characters.Party;
import rpg.flags.CampaignFlags;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.quests.QuestProgress;
import rpg.quests.QuestStatus;

// Evaluates requirement sets against the current campaign state.
public final class RequirementEvaluator {

    private final ItemCatalog itemCatalog;

    public RequirementEvaluator(ItemCatalog itemCatalog) {
        if (itemCatalog == null) {
            throw new IllegalArgumentException("Item catalog cannot be null.");
        }

        this.itemCatalog = itemCatalog;
    }

    public boolean isSatisfied(
            RequirementSetDefinition requirementSet,
            Party party,
            QuestProgress questProgress,
            CampaignFlags campaignFlags) {

        if (requirementSet == null) {
            throw new IllegalArgumentException("Requirement set cannot be null.");
        }

        if (party == null) {
            throw new IllegalArgumentException("Party cannot be null.");
        }

        if (questProgress == null) {
            throw new IllegalArgumentException("Quest progress cannot be null.");
        }

        if (campaignFlags == null) {
            throw new IllegalArgumentException("Campaign flags cannot be null.");
        }

        for (RequirementDefinition requirement : requirementSet.getRequirements()) {

            if (!isSatisfied(requirement, party, questProgress, campaignFlags)) {

                return false;
            }
        }

        return true;
    }

    private boolean isSatisfied(
            RequirementDefinition requirement,
            Party party,
            QuestProgress questProgress,
            CampaignFlags campaignFlags) {

        return switch (requirement.getType()) {
            case LEADER_LEVEL_AT_LEAST ->
                    party.getLeader().getLevel() >= getRequiredValue(requirement);

            case ITEM_QUANTITY_AT_LEAST -> hasRequiredItemQuantity(requirement, party);

            case QUEST_NOT_STARTED ->
                    hasQuestStatus(requirement, questProgress, QuestStatus.NOT_STARTED);

            case QUEST_ACTIVE -> hasQuestStatus(requirement, questProgress, QuestStatus.ACTIVE);

            case QUEST_COMPLETED ->
                    hasQuestStatus(requirement, questProgress, QuestStatus.COMPLETED);

            case FLAG_SET -> campaignFlags.isSet(getRequiredTarget(requirement));

            case FLAG_UNSET -> !campaignFlags.isSet(getRequiredTarget(requirement));
        };
    }

    private boolean hasRequiredItemQuantity(RequirementDefinition requirement, Party party) {

        Item item = itemCatalog.getById(getRequiredTarget(requirement));

        int ownedQuantity = party.getInventory().getQuantity(item);

        return ownedQuantity >= getRequiredValue(requirement);
    }

    private boolean hasQuestStatus(
            RequirementDefinition requirement,
            QuestProgress questProgress,
            QuestStatus requiredStatus) {

        QuestStatus currentStatus = questProgress.getStatus(getRequiredTarget(requirement));

        return currentStatus == requiredStatus;
    }

    private String getRequiredTarget(RequirementDefinition requirement) {

        return requirement
                .getTargetId()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Requirement target was not validated for type: "
                                                + requirement.getType()));
    }

    private int getRequiredValue(RequirementDefinition requirement) {

        return requirement
                .getRequiredValue()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Requirement value was not validated for type: "
                                                + requirement.getType()));
    }
}
