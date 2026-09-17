package rpg.requirements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Defines a group of requirements that must all be satisfied.
public final class RequirementSetDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String id;
    private final List<RequirementDefinition> requirements;

    public RequirementSetDefinition(String id, List<RequirementDefinition> requirements) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Requirement set ID cannot be empty.");
        }

        String normalizedId = id.trim();

        if (!normalizedId.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    "Requirement set ID must use lowercase letters, numbers and single underscores: "
                            + normalizedId);
        }

        if (requirements == null || requirements.isEmpty()) {
            throw new IllegalArgumentException(
                    "Requirement set must contain at least one requirement: " + id);
        }

        List<RequirementDefinition> orderedRequirements = new ArrayList<>(requirements);

        if (orderedRequirements.contains(null)) {
            throw new IllegalArgumentException(
                    "Requirement set cannot contain null requirements: " + id);
        }

        orderedRequirements.sort(Comparator.comparingInt(RequirementDefinition::getOrder));

        validateOrder(id, orderedRequirements);
        validateCombinations(id, orderedRequirements);

        this.id = normalizedId;
        this.requirements = List.copyOf(orderedRequirements);
    }

    public String getId() {
        return id;
    }

    public List<RequirementDefinition> getRequirements() {
        return requirements;
    }

    private static void validateOrder(String setId, List<RequirementDefinition> requirements) {

        for (int index = 0; index < requirements.size(); index++) {
            int expectedOrder = index + 1;
            int actualOrder = requirements.get(index).getOrder();

            if (actualOrder != expectedOrder) {
                throw new IllegalArgumentException(
                        "Requirement orders must be consecutive from 1 in set "
                                + setId
                                + ". Expected "
                                + expectedOrder
                                + " but found "
                                + actualOrder
                                + ".");
            }
        }
    }

    private static void validateCombinations(
            String setId, List<RequirementDefinition> requirements) {

        boolean leaderLevelFound = false;
        Set<String> itemIds = new HashSet<>();
        Set<String> questIds = new HashSet<>();
        Set<String> flagIds = new HashSet<>();

        for (RequirementDefinition requirement : requirements) {
            switch (requirement.getType()) {
                case LEADER_LEVEL_AT_LEAST -> {
                    if (leaderLevelFound) {
                        throw duplicateRequirement(setId, "leader level");
                    }

                    leaderLevelFound = true;
                }

                case ITEM_QUANTITY_AT_LEAST -> {
                    String itemId = getRequiredTarget(requirement);

                    if (!itemIds.add(itemId)) {
                        throw duplicateRequirement(setId, "item " + itemId);
                    }
                }

                case QUEST_NOT_STARTED, QUEST_ACTIVE, QUEST_COMPLETED -> {
                    String questId = getRequiredTarget(requirement);

                    if (!questIds.add(questId)) {
                        throw duplicateRequirement(setId, "quest " + questId);
                    }
                }

                case FLAG_SET, FLAG_UNSET -> {
                    String flagId = getRequiredTarget(requirement);

                    if (!flagIds.add(flagId)) {
                        throw duplicateRequirement(setId, "flag " + flagId);
                    }
                }
            }
        }
    }

    private static String getRequiredTarget(RequirementDefinition requirement) {

        return requirement
                .getTargetId()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Requirement target was not validated for type: "
                                                + requirement.getType()));
    }

    private static IllegalArgumentException duplicateRequirement(String setId, String subject) {

        return new IllegalArgumentException(
                "Requirement set "
                        + setId
                        + " contains duplicated or conflicting requirements for "
                        + subject
                        + ".");
    }
}
