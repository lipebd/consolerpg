package rpg.requirements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rpg.quests.QuestCatalog;
import rpg.quests.QuestDefinition;

// Validates requirement-set references and quest-availability dependencies.
public final class QuestAvailabilityValidator {

    private QuestAvailabilityValidator() {}

    public static void validate(QuestCatalog questCatalog, RequirementCatalog requirementCatalog) {

        if (questCatalog == null) {
            throw new IllegalArgumentException("Quest catalog cannot be null.");
        }

        if (requirementCatalog == null) {
            throw new IllegalArgumentException("Requirement catalog cannot be null.");
        }

        Map<String, Set<String>> dependenciesByQuestId =
                collectDependencies(questCatalog, requirementCatalog);

        validateAcyclicDependencies(dependenciesByQuestId);
    }

    private static Map<String, Set<String>> collectDependencies(
            QuestCatalog questCatalog, RequirementCatalog requirementCatalog) {

        Map<String, Set<String>> dependenciesByQuestId = new LinkedHashMap<>();

        for (QuestDefinition quest : questCatalog.getAll()) {
            Set<String> dependencies = new LinkedHashSet<>();

            quest.getAvailabilityRequirementSetId()
                    .ifPresent(
                            requirementSetId ->
                                    collectQuestDependencies(
                                            quest,
                                            requirementSetId,
                                            questCatalog,
                                            requirementCatalog,
                                            dependencies));

            dependenciesByQuestId.put(quest.getId(), dependencies);
        }

        return dependenciesByQuestId;
    }

    private static void collectQuestDependencies(
            QuestDefinition quest,
            String requirementSetId,
            QuestCatalog questCatalog,
            RequirementCatalog requirementCatalog,
            Set<String> dependencies) {

        RequirementSetDefinition requirementSet;

        try {
            requirementSet = requirementCatalog.getById(requirementSetId);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Quest "
                            + quest.getId()
                            + " references unknown availability requirement set: "
                            + requirementSetId,
                    exception);
        }

        for (RequirementDefinition requirement : requirementSet.getRequirements()) {

            if (!isQuestRequirement(requirement.getType())) {
                continue;
            }

            String dependencyQuestId =
                    requirement
                            .getTargetId()
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Quest requirement target was not validated: "
                                                            + requirement.getType()));

            try {
                questCatalog.getById(dependencyQuestId);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Quest "
                                + quest.getId()
                                + " availability references unknown quest: "
                                + dependencyQuestId,
                        exception);
            }

            if (quest.getId().equals(dependencyQuestId)) {
                throw new IllegalArgumentException(
                        "Quest availability cannot depend on its own status: " + quest.getId());
            }

            dependencies.add(dependencyQuestId);
        }
    }

    private static boolean isQuestRequirement(RequirementType type) {

        return type == RequirementType.QUEST_NOT_STARTED
                || type == RequirementType.QUEST_ACTIVE
                || type == RequirementType.QUEST_COMPLETED;
    }

    private static void validateAcyclicDependencies(
            Map<String, Set<String>> dependenciesByQuestId) {

        Map<String, VisitState> statesByQuestId = new LinkedHashMap<>();

        List<String> currentPath = new ArrayList<>();

        for (String questId : dependenciesByQuestId.keySet()) {
            if (!statesByQuestId.containsKey(questId)) {
                visit(questId, dependenciesByQuestId, statesByQuestId, currentPath);
            }
        }
    }

    private static void visit(
            String questId,
            Map<String, Set<String>> dependenciesByQuestId,
            Map<String, VisitState> statesByQuestId,
            List<String> currentPath) {

        statesByQuestId.put(questId, VisitState.VISITING);

        currentPath.add(questId);

        for (String dependencyQuestId : dependenciesByQuestId.get(questId)) {

            VisitState dependencyState = statesByQuestId.get(dependencyQuestId);

            if (dependencyState == VisitState.VISITING) {
                throw cycleException(dependencyQuestId, currentPath);
            }

            if (dependencyState != VisitState.VISITED) {
                visit(dependencyQuestId, dependenciesByQuestId, statesByQuestId, currentPath);
            }
        }

        currentPath.remove(currentPath.size() - 1);

        statesByQuestId.put(questId, VisitState.VISITED);
    }

    private static IllegalArgumentException cycleException(
            String repeatedQuestId, List<String> currentPath) {

        int cycleStart = currentPath.indexOf(repeatedQuestId);

        List<String> cycle = new ArrayList<>(currentPath.subList(cycleStart, currentPath.size()));

        cycle.add(repeatedQuestId);

        return new IllegalArgumentException(
                "Quest availability dependency cycle detected: " + String.join(" -> ", cycle));
    }

    private enum VisitState {
        VISITING,
        VISITED
    }
}
