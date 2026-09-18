package rpg.narrative;

import rpg.quests.QuestCatalog;
import rpg.quests.QuestDefinition;
import rpg.quests.QuestObjectiveDefinition;
import rpg.quests.QuestObjectiveType;
import rpg.quests.QuestStepDefinition;

// Validates narrative references that cross independently loaded catalogs.
public final class NarrativeReferenceValidator {

    private NarrativeReferenceValidator() {}

    public static void validate(
            NpcCatalog npcCatalog, DialogueCatalog dialogueCatalog, QuestCatalog questCatalog) {
        if (npcCatalog == null) {
            throw new IllegalArgumentException("NPC catalog cannot be null.");
        }

        if (dialogueCatalog == null) {
            throw new IllegalArgumentException("Dialogue catalog cannot be null.");
        }

        if (questCatalog == null) {
            throw new IllegalArgumentException("Quest catalog cannot be null.");
        }

        validateNpcDefaultDialogues(npcCatalog, dialogueCatalog);

        validateQuestNpcReferences(questCatalog, npcCatalog);
    }

    private static void validateNpcDefaultDialogues(
            NpcCatalog npcCatalog, DialogueCatalog dialogueCatalog) {
        for (NpcDefinition npcDefinition : npcCatalog.getAll()) {
            try {
                dialogueCatalog.getById(npcDefinition.getDefaultDialogueId());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "NPC "
                                + npcDefinition.getId()
                                + " references unknown default dialogue: "
                                + npcDefinition.getDefaultDialogueId(),
                        exception);
            }
        }
    }

    private static void validateQuestNpcReferences(
            QuestCatalog questCatalog, NpcCatalog npcCatalog) {
        for (QuestDefinition questDefinition : questCatalog.getAll()) {
            validateStartingNpc(questDefinition, npcCatalog);

            validateObjectiveNpcs(questDefinition, npcCatalog);
        }
    }

    private static void validateStartingNpc(
            QuestDefinition questDefinition, NpcCatalog npcCatalog) {
        try {
            npcCatalog.getById(questDefinition.getStartingNpcId());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Quest "
                            + questDefinition.getId()
                            + " references unknown starting NPC: "
                            + questDefinition.getStartingNpcId(),
                    exception);
        }
    }

    private static void validateObjectiveNpcs(
            QuestDefinition questDefinition, NpcCatalog npcCatalog) {
        for (QuestStepDefinition stepDefinition : questDefinition.getSteps()) {
            for (QuestObjectiveDefinition objectiveDefinition : stepDefinition.getObjectives()) {
                if (objectiveDefinition.getObjectiveType() != QuestObjectiveType.TALK_TO_NPC) {
                    continue;
                }

                String npcId = objectiveDefinition.getTargetId().orElseThrow();

                try {
                    npcCatalog.getById(npcId);
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException(
                            "Quest "
                                    + questDefinition.getId()
                                    + ", step "
                                    + stepDefinition.getStep()
                                    + ", objective "
                                    + objectiveDefinition.getId()
                                    + " references unknown NPC: "
                                    + npcId,
                            exception);
                }
            }
        }
    }
}
