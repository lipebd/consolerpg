package rpg.quests;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.flags.CampaignFlags;
import rpg.items.Inventory;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.quests.effects.QuestEffectCatalog;
import rpg.quests.effects.QuestEffectDefinition;
import rpg.quests.effects.QuestEffectTrigger;
import rpg.quests.effects.QuestEffectType;
import rpg.quests.effects.QuestTransportState;
import rpg.quests.rewards.QuestRewardCatalog;
import rpg.quests.rewards.QuestRewardDefinition;
import rpg.quests.rewards.QuestRewardType;
import rpg.world.travel.TravelProgress;

// Applies confirmed gameplay events and atomic quest transitions.
public final class QuestProgressService {

	private final QuestTransportState questTransportState;
    private final QuestProgress questProgress;
    private final QuestRewardCatalog rewardCatalog;
    private final QuestEffectCatalog effectCatalog;
    private final Party party;
    private final ItemCatalog itemCatalog;
    private final CampaignFlags campaignFlags;
    private final TravelProgress travelProgress;

    public QuestProgressService(
            QuestProgress questProgress,
            QuestRewardCatalog rewardCatalog,
            QuestEffectCatalog effectCatalog,
            Party party,
            ItemCatalog itemCatalog,
            CampaignFlags campaignFlags,
            TravelProgress travelProgress,
            QuestTransportState questTransportState) {
        if (questProgress == null) {
            throw new IllegalArgumentException("Quest progress cannot be null.");
        }

        if (rewardCatalog == null) {
            throw new IllegalArgumentException("Quest reward catalog cannot be null.");
        }

        if (effectCatalog == null) {
            throw new IllegalArgumentException("Quest effect catalog cannot be null.");
        }

        if (party == null) {
            throw new IllegalArgumentException("Party cannot be null.");
        }

        if (itemCatalog == null) {
            throw new IllegalArgumentException("Item catalog cannot be null.");
        }

        if (campaignFlags == null) {
            throw new IllegalArgumentException("Campaign flags cannot be null.");
        }

        if (travelProgress == null) {
            throw new IllegalArgumentException("Travel progress cannot be null.");
        }

		if (questTransportState == null) {
			throw new IllegalArgumentException("Quest transport state cannot be null.");
		}

        this.questTransportState = questTransportState;this.questProgress = questProgress;
        this.rewardCatalog = rewardCatalog;
        this.effectCatalog = effectCatalog;
        this.party = party;
        this.itemCatalog = itemCatalog;
        this.campaignFlags = campaignFlags;
        this.travelProgress = travelProgress;
    }

    public QuestProgressResult startQuest(String questId) {
        ProgressAccumulator accumulator = new ProgressAccumulator();

        questProgress.activateQuest(questId);

        applyEffects(
                effectCatalog.getByTrigger(
                        questId,
                        0,
                        QuestEffectTrigger.QUEST_START),
                accumulator);

        return accumulator.createResult();
    }

    public QuestProgressResult processEvents(List<QuestProgressEvent> events) {
        List<QuestProgressEvent> validatedEvents = validateEvents(events);
        ProgressAccumulator accumulator = new ProgressAccumulator();

        processEventQueue(validatedEvents, accumulator);

        return accumulator.createResult();
    }

	public List<QuestCompletionFailure>
	validateNpcInteractionCompletion(
			String questId,
			String npcId
			) {
		return validateNpcInteractionCompletion(
				questId,
				npcId,
				Map.of());
	}

	public List<QuestCompletionFailure>
	validateNpcInteractionCompletion(
			String questId,
			String npcId,
			Map<Item, Integer> pendingInventoryAdditions
			) {
        if (npcId == null || npcId.isBlank()) {
			throw new IllegalArgumentException(
					"NPC ID cannot be empty."
					);
        }

		Map<Item, Integer> validatedPendingAdditions =
				validatePendingInventoryAdditions(
						pendingInventoryAdditions);

        QuestStepDefinition currentStep =
                questProgress
                        .getCurrentStep(questId)
                        .orElseThrow(
						() ->
						new IllegalStateException(
								"Quest is not active: "
										+ questId));

        String normalizedNpcId = npcId.trim();

        Map<Item, Integer> deliveredItems;

        if (containsDeliveryObjective(currentStep)) {
			validateDeliveryInteraction(
					questId,
					normalizedNpcId,
					currentStep);

			deliveredItems =
					collectDeliveredItems(
							questId,
							currentStep);
        } else {
			validateTalkInteraction(
					questId,
					normalizedNpcId,
					currentStep);

            deliveredItems = Map.of();
        }

		return List.copyOf(
				validateCompletionPlan(
						questId,
						deliveredItems,
						validatedPendingAdditions));
    }

    public QuestProgressResult completeNpcInteraction(String questId, String npcId) {
        if (npcId == null || npcId.isBlank()) {
            throw new IllegalArgumentException("NPC ID cannot be empty.");
        }

        QuestStepDefinition currentStep =
                questProgress
                        .getCurrentStep(questId)
                        .orElseThrow(
                                () -> new IllegalStateException("Quest is not active: " + questId));

        String normalizedNpcId = npcId.trim();

        if (containsDeliveryObjective(currentStep)) {
            return completeDelivery(questId, normalizedNpcId);
        }

        validateTalkInteraction(questId, normalizedNpcId, currentStep);

        if (questProgress.isCurrentStepComplete(questId)) {
            return retryCompletedStep(questId);
        }

        QuestProgressEvent event =
                new QuestProgressEvent(
                        QuestProgressEventType.NPC_TALKED, Optional.of(normalizedNpcId), 1);

        return processEvents(List.of(event));
    }

    public QuestProgressResult completeDelivery(String questId, String npcId) {
        if (npcId == null || npcId.isBlank()) {
            throw new IllegalArgumentException("NPC ID cannot be empty.");
        }

        QuestStepDefinition currentStep =
                questProgress
                        .getCurrentStep(questId)
                        .orElseThrow(
                                () -> new IllegalStateException("Quest is not active: " + questId));

        validateDeliveryInteraction(questId, npcId.trim(), currentStep);

        Map<Item, Integer> deliveredItems = collectDeliveredItems(questId, currentStep);

        ProgressAccumulator accumulator = new ProgressAccumulator();

		List<QuestCompletionFailure> failures = validateCompletionPlan(questId, deliveredItems, Map.of());

        if (!failures.isEmpty()) {
            accumulator.completionFailures.addAll(failures);
            return accumulator.createResult();
        }

        removeDeliveredItems(deliveredItems);

        completeDeliveryObjectives(questId, currentStep, accumulator);

        List<QuestProgressEvent> generatedEvents = applyCompletedStep(questId, accumulator);

        processEventQueue(generatedEvents, accumulator);

        return accumulator.createResult();
    }

    public boolean canRetryQuestCompletion(String questId) {
        Optional<QuestStepDefinition> currentStep =
                questProgress.getCurrentStep(questId);

        if (currentStep.isEmpty()) {
            return false;
        }

        if (!questProgress.isCurrentStepComplete(questId)) {
            return false;
        }

        for (QuestObjectiveDefinition objective
                : currentStep.orElseThrow().getObjectives()) {
            if (objective.getObjectiveType()
                    == QuestObjectiveType.TALK_TO_NPC) {
                return false;
            }

            if (objective.getObjectiveType()
                    == QuestObjectiveType.DELIVER_ITEM) {
                return false;
            }
        }

        return true;
    }

    public QuestProgressResult retryQuestCompletion(String questId) {
        if (!canRetryQuestCompletion(questId)) {
            throw new IllegalStateException(
                    "Quest is not eligible for manual completion retry: " + questId);
        }

        return retryCompletedStep(questId);
    }

    public QuestProgressResult retryCompletedStep(String questId) {
        QuestStepDefinition currentStep =
                questProgress
                        .getCurrentStep(questId)
                        .orElseThrow(
                                () -> new IllegalStateException("Quest is not active: " + questId));

        if (containsDeliveryObjective(currentStep)) {
            throw new IllegalStateException(
                    "Delivery quest steps must be completed through their NPC interaction: "
                            + questId);
        }

        if (!questProgress.isCurrentStepComplete(questId)) {
            throw new IllegalStateException(
                    "Quest step is not ready for another completion attempt: " + questId);
        }

        ProgressAccumulator accumulator = new ProgressAccumulator();

		List<QuestCompletionFailure> failures = validateCompletionPlan(questId, Map.of(), Map.of());

        if (!failures.isEmpty()) {
            accumulator.completionFailures.addAll(failures);
            return accumulator.createResult();
        }

        List<QuestProgressEvent> generatedEvents = applyCompletedStep(questId, accumulator);

        processEventQueue(generatedEvents, accumulator);

        return accumulator.createResult();
    }

    private void processEventQueue(
            List<QuestProgressEvent> initialEvents, ProgressAccumulator accumulator) {
        Deque<QuestProgressEvent> pendingEvents = new ArrayDeque<>(initialEvents);

        while (!pendingEvents.isEmpty()) {
            QuestProgressEvent event = pendingEvents.removeFirst();

            List<QuestProgressEvent> generatedEvents = processEvent(event, accumulator);

            for (int index = generatedEvents.size() - 1; index >= 0; index--) {
                pendingEvents.addFirst(generatedEvents.get(index));
            }
        }
    }

    private List<QuestProgressEvent> processEvent(
            QuestProgressEvent event, ProgressAccumulator accumulator) {
        List<QuestProgressEvent> generatedEvents = new ArrayList<>();

        List<QuestDefinition> activeQuests = questProgress.getActiveQuests();

        for (QuestDefinition questDefinition : activeQuests) {
            String questId = questDefinition.getId();

            QuestStepDefinition currentStep =
                    questProgress
                            .getCurrentStep(questId)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Active quest is missing its current step: "
                                                            + questId));

            if (event.getEventType() == QuestProgressEventType.NPC_TALKED
                    && containsDeliveryObjective(currentStep)) {
                continue;
            }

            Optional<QuestObjectiveDefinition> matchingObjective =
                    findMatchingObjective(currentStep, event);

            if (matchingObjective.isEmpty()) {
                continue;
            }

            QuestObjectiveDefinition objective = matchingObjective.orElseThrow();

            int previousProgress = questProgress.getObjectiveProgress(questId, objective.getId());

            if (previousProgress >= objective.getRequiredQuantity()) {
                continue;
            }

            int resultingProgress =
                    questProgress.increaseObjectiveProgress(
                            questId, objective.getId(), event.getQuantity());

            if (resultingProgress >= objective.getRequiredQuantity()) {
                accumulator.updates.add(
                        QuestProgressUpdate.objectiveCompleted(
                                questId,
                                currentStep.getStep(),
                                objective.getId(),
                                objective.getRequiredQuantity()));
            } else {
                accumulator.updates.add(
                        QuestProgressUpdate.objectiveProgress(
                                questId,
                                currentStep.getStep(),
                                objective.getId(),
                                resultingProgress,
                                objective.getRequiredQuantity()));
            }

            if (!questProgress.isCurrentStepComplete(questId)) {
                continue;
            }

			List<QuestCompletionFailure> failures = validateCompletionPlan(questId, Map.of(), Map.of());

            if (!failures.isEmpty()) {
                accumulator.completionFailures.addAll(failures);
                continue;
            }

            generatedEvents.addAll(applyCompletedStep(questId, accumulator));
        }

        return generatedEvents;
    }

    private List<QuestCompletionFailure> validateCompletionPlan(
            String questId,
            Map<Item, Integer> deliveredItems,
            Map<Item, Integer> pendingInventoryAdditions) {
        List<QuestCompletionFailure> failures = new ArrayList<>();

        Inventory inventory = party.getInventory();

        for (Map.Entry<Item, Integer> delivery : deliveredItems.entrySet()) {
            int ownedQuantity = inventory.getQuantity(delivery.getKey());

            if (ownedQuantity < delivery.getValue()) {
                failures.add(
                        QuestCompletionFailure.missingDeliveryItem(
                                questId,
                                delivery.getKey().getId(),
                                delivery.getValue(),
                                ownedQuantity));
            }
        }

        if (!failures.isEmpty()) {
            return failures;
        }

        Map<Item, Integer> completionAdditions = new LinkedHashMap<>(pendingInventoryAdditions);

        for (Map.Entry<Item, Integer> addition : collectGrantedItems(questId).entrySet()) {
            completionAdditions.merge(addition.getKey(), addition.getValue(), Math::addExact);
        }

        for (Map.Entry<Item, Integer> addition : completionAdditions.entrySet()) {
            Item item = addition.getKey();

            int ownedQuantity = inventory.getQuantity(item);

            int deliveredQuantity = deliveredItems.getOrDefault(item, 0);

            int quantityBeforeAddition = ownedQuantity - deliveredQuantity;

            int remainingCapacity = Inventory.MAXIMUM_QUANTITY_PER_ITEM - quantityBeforeAddition;

            if (addition.getValue() > remainingCapacity) {
                failures.add(
                        QuestCompletionFailure.itemCapacityExceeded(
                                questId, item.getId(), addition.getValue(), remainingCapacity));
            }
        }

        if (!failures.isEmpty() || !questProgress.isCurrentStepFinal(questId)) {
            return failures;
        }

        for (QuestRewardDefinition reward : rewardCatalog.getByQuestId(questId)) {
            switch (reward.getRewardType()) {
                case GOLD -> {
                    int remainingCapacity = party.getRemainingGoldCapacity();

                    if (reward.getQuantity() > remainingCapacity) {
                        failures.add(
                                QuestCompletionFailure.goldCapacityExceeded(
                                        questId, reward.getQuantity(), remainingCapacity));
                    }
                }

                case ITEM -> {
                    Item item = itemCatalog.getById(reward.getRewardId().orElseThrow());

                    int ownedQuantity = inventory.getQuantity(item);

                    int deliveredQuantity = deliveredItems.getOrDefault(item, 0);

                    int pendingAddition = completionAdditions.getOrDefault(item, 0);

                    int quantityBeforeReward =
                            Math.addExact(ownedQuantity - deliveredQuantity, pendingAddition);

                    int remainingCapacity =
                            Inventory.MAXIMUM_QUANTITY_PER_ITEM - quantityBeforeReward;

                    if (reward.getQuantity() > remainingCapacity) {
                        failures.add(
                                QuestCompletionFailure.itemCapacityExceeded(
                                        questId,
                                        item.getId(),
                                        reward.getQuantity(),
                                        remainingCapacity));
                    }
                }

                case PARTY_EXP -> {
                    // Character progression always accepts its supported portion.
                }
            }
        }

        return failures;
    }

	private static Map<Item, Integer>
	validatePendingInventoryAdditions(
			Map<Item, Integer> pendingInventoryAdditions
			) {
		if (pendingInventoryAdditions == null) {
			throw new IllegalArgumentException(
					"Pending inventory additions cannot be null."
					);
		}

		for (Map.Entry<Item, Integer> addition
				: pendingInventoryAdditions.entrySet()) {
			if (addition.getKey() == null) {
				throw new IllegalArgumentException(
						"Pending inventory additions cannot contain a null item."
						);
			}

			if (addition.getValue() == null
					|| addition.getValue() <= 0) {
				throw new IllegalArgumentException(
						"Pending inventory addition quantity must be positive."
						);
			}
		}

		return Map.copyOf(pendingInventoryAdditions);
	}

    private List<QuestProgressEvent> applyCompletedStep(
            String questId, ProgressAccumulator accumulator) {
        QuestStepDefinition completedStep = questProgress.getCurrentStep(questId).orElseThrow();

        boolean finalStep = questProgress.isCurrentStepFinal(questId);

        List<QuestProgressEvent> generatedEvents = new ArrayList<>();

        generatedEvents.addAll(
                applyEffects(
                        effectCatalog.getByTrigger(
                                questId,
                                completedStep.getStep(),
                                QuestEffectTrigger.STEP_COMPLETE),
                        accumulator));

        if (finalStep) {
        	generatedEvents.addAll(applyMaterialRewards(questId));

        	generatedEvents.addAll(
        	        applyEffects(
        	                effectCatalog.getByTrigger(
        	                        questId,
        	                        0,
        	                        QuestEffectTrigger.QUEST_COMPLETE),
        	                accumulator));
        }

        boolean questCompleted = questProgress.advanceCompletedStep(questId);

        if (questCompleted != finalStep) {
            throw new IllegalStateException(
                    "Quest completion result disagrees with the completion plan: " + questId);
        }

        accumulator.updates.add(
                QuestProgressUpdate.stepCompleted(questId, completedStep.getStep()));

        if (questCompleted) {
            accumulator.updates.add(
                    QuestProgressUpdate.questCompleted(questId, completedStep.getStep()));
        }

        return generatedEvents;
    }

    private List<QuestProgressEvent> applyMaterialRewards(String questId) {
        List<QuestRewardDefinition> rewards = rewardCatalog.getByQuestId(questId);

        List<QuestProgressEvent> generatedEvents = new ArrayList<>();

        // Potentially rejecting mutations happen before EXP.
        for (QuestRewardDefinition reward : rewards) {
            switch (reward.getRewardType()) {
                case GOLD -> {
                    if (!party.addGold(reward.getQuantity())) {
                        throw new IllegalStateException(
                                "Validated quest GOLD reward could not be added: " + questId);
                    }
                }

                case ITEM -> {
                    Item item = itemCatalog.getById(reward.getRewardId().orElseThrow());

                    if (!party.getInventory().addItem(item, reward.getQuantity())) {
                        throw new IllegalStateException(
                                "Validated quest item reward could not be added: "
                                        + questId
                                        + " -> "
                                        + item.getId());
                    }

                    generatedEvents.add(
                            new QuestProgressEvent(
                                    QuestProgressEventType.ITEM_OBTAINED,
                                    Optional.of(item.getId()),
                                    reward.getQuantity()));
                }

                case PARTY_EXP -> {
                    // Applied only after every capacity-limited reward.
                }
            }
        }

        for (QuestRewardDefinition reward : rewards) {
            if (reward.getRewardType() != QuestRewardType.PARTY_EXP) {
                continue;
            }

            for (PlayableCharacter member : party.getMembers()) {
                member.gainExperience(reward.getQuantity());
            }
        }

        return generatedEvents;
    }

    private List<QuestProgressEvent> applyEffects(
            List<QuestEffectDefinition> effects,
            ProgressAccumulator accumulator) {
        List<QuestProgressEvent> generatedEvents =
                new ArrayList<>();

        for (QuestEffectDefinition effect : effects) {
            boolean stateChanged = applyEffect(effect);

            if (!stateChanged) {
                continue;
            }

            effect.getDisplayText()
                    .ifPresent(accumulator.effectDisplayTexts::add);

            if (effect.getEffectType()
                    == QuestEffectType.GRANT_ITEM) {
                generatedEvents.add(
                        new QuestProgressEvent(
                                QuestProgressEventType.ITEM_OBTAINED,
                                Optional.of(effect.getTargetId()),
                                effect.getQuantity().orElseThrow()));
            }
        }

        return generatedEvents;
    }

	private boolean applyEffect(QuestEffectDefinition effect) {
		return switch (effect.getEffectType()) {

		case SET_FLAG -> campaignFlags.set(effect.getTargetId());

		case UNLOCK_ROUTE -> {
			boolean alreadyUnlocked = travelProgress.isUnlocked(effect.getTargetId());

			if (!alreadyUnlocked) {
				travelProgress.unlock(effect.getTargetId());
			}

			yield !alreadyUnlocked;
		}

		case GRANT_ITEM -> {
			Item item = itemCatalog.getById(effect.getTargetId());
			int quantity = effect.getQuantity().orElseThrow();

			if (!party.getInventory().addItem(item, quantity)) {
				throw new IllegalStateException(
						"Validated quest item effect could not be applied: " + effect.getQuestId() + " -> "
								+ item.getId()
				);
			}

			yield true;
		}

		case TRANSPORT_PARTY -> {
			questTransportState.request(effect.getTargetId());
			yield true;
		}
		};
	}

    private void validateTalkInteraction(
            String questId, String npcId, QuestStepDefinition currentStep) {
        boolean matchingTalkObjective = false;

        for (QuestObjectiveDefinition objective : currentStep.getObjectives()) {
            if (objective.getObjectiveType() == QuestObjectiveType.TALK_TO_NPC) {
                matchingTalkObjective = objective.getTargetId().orElseThrow().equals(npcId);

                continue;
            }

            int currentProgress = questProgress.getObjectiveProgress(questId, objective.getId());

            if (currentProgress < objective.getRequiredQuantity()) {
                throw new IllegalStateException(
                        "Quest NPC interaction cannot proceed before the other objectives are complete: "
                                + questId);
            }
        }

        if (!matchingTalkObjective) {
            throw new IllegalStateException(
                    "NPC does not complete the current quest interaction: " + npcId);
        }
    }

    private void validateDeliveryInteraction(
            String questId, String npcId, QuestStepDefinition currentStep) {
        boolean matchingTalkObjective = false;
        boolean deliveryObjectiveFound = false;

        for (QuestObjectiveDefinition objective : currentStep.getObjectives()) {
            if (objective.getObjectiveType() == QuestObjectiveType.TALK_TO_NPC) {
                matchingTalkObjective = objective.getTargetId().orElseThrow().equals(npcId);

                continue;
            }

            if (objective.getObjectiveType() == QuestObjectiveType.DELIVER_ITEM) {
                deliveryObjectiveFound = true;
                continue;
            }

            int currentProgress = questProgress.getObjectiveProgress(questId, objective.getId());

            if (currentProgress < objective.getRequiredQuantity()) {
                throw new IllegalStateException(
                        "Quest delivery cannot proceed before the other "
                                + "objectives are complete: "
                                + questId);
            }
        }

        if (!deliveryObjectiveFound) {
            throw new IllegalStateException(
                    "Current quest step does not contain a delivery objective: " + questId);
        }

        if (!matchingTalkObjective) {
            throw new IllegalStateException(
                    "NPC does not receive the current quest delivery: " + npcId);
        }
    }

    private Map<Item, Integer> collectDeliveredItems(
            String questId, QuestStepDefinition currentStep) {
        Map<Item, Integer> deliveredItems = new LinkedHashMap<>();

        for (QuestObjectiveDefinition objective : currentStep.getObjectives()) {
            if (objective.getObjectiveType() != QuestObjectiveType.DELIVER_ITEM) {
                continue;
            }

            int currentProgress = questProgress.getObjectiveProgress(questId, objective.getId());

            if (currentProgress != 0) {
                throw new IllegalStateException(
                        "Delivery objective contains unexpected prior progress: "
                                + objective.getId());
            }

            Item item = itemCatalog.getById(objective.getTargetId().orElseThrow());

            deliveredItems.put(item, objective.getRequiredQuantity());
        }

        return deliveredItems;
    }

    private void removeDeliveredItems(Map<Item, Integer> deliveredItems) {
        Inventory inventory = party.getInventory();

        for (Map.Entry<Item, Integer> delivery : deliveredItems.entrySet()) {
            if (!inventory.removeItem(delivery.getKey(), delivery.getValue())) {
                throw new IllegalStateException(
                        "Validated quest delivery could not remove item: "
                                + delivery.getKey().getId());
            }
        }
    }

    private void completeDeliveryObjectives(
            String questId, QuestStepDefinition currentStep, ProgressAccumulator accumulator) {
        for (QuestObjectiveDefinition objective : currentStep.getObjectives()) {
            if (objective.getObjectiveType() != QuestObjectiveType.TALK_TO_NPC
                    && objective.getObjectiveType() != QuestObjectiveType.DELIVER_ITEM) {
                continue;
            }

            int resultingProgress =
                    questProgress.increaseObjectiveProgress(
                            questId, objective.getId(), objective.getRequiredQuantity());

            if (resultingProgress != objective.getRequiredQuantity()) {
                throw new IllegalStateException(
                        "Quest delivery objective did not reach completion: " + objective.getId());
            }

            accumulator.updates.add(
                    QuestProgressUpdate.objectiveCompleted(
                            questId,
                            currentStep.getStep(),
                            objective.getId(),
                            objective.getRequiredQuantity()));
        }

        if (!questProgress.isCurrentStepComplete(questId)) {
            throw new IllegalStateException(
                    "Quest delivery did not complete its current step: " + questId);
        }
    }

    private Optional<QuestObjectiveDefinition> findMatchingObjective(
            QuestStepDefinition currentStep,
            QuestProgressEvent event) {
        QuestObjectiveType expectedObjectiveType =
                event.getEventType().getObjectiveType();

        for (QuestObjectiveDefinition objective
                : currentStep.getObjectives()) {
            if (objective.getObjectiveType()
                    != expectedObjectiveType) {
                continue;
            }

            if (objective.getTargetId().isEmpty()
                    || objective.getTargetId().equals(event.getTargetId())) {
                return Optional.of(objective);
            }
        }

        return Optional.empty();
    }

    private boolean containsDeliveryObjective(QuestStepDefinition currentStep) {
        for (QuestObjectiveDefinition objective : currentStep.getObjectives()) {
            if (objective.getObjectiveType() == QuestObjectiveType.DELIVER_ITEM) {
                return true;
            }
        }

        return false;
    }

    private List<QuestProgressEvent> validateEvents(List<QuestProgressEvent> events) {
        if (events == null) {
            throw new IllegalArgumentException("Quest progress events cannot be null.");
        }

        for (QuestProgressEvent event : events) {
            if (event == null) {
                throw new IllegalArgumentException(
                        "Quest progress events cannot contain null entries.");
            }
        }

        return List.copyOf(events);
    }

    private static final class ProgressAccumulator {

        private final List<QuestProgressUpdate> updates;
        private final List<String> effectDisplayTexts;
        private final List<QuestCompletionFailure> completionFailures;

        private ProgressAccumulator() {
            updates = new ArrayList<>();
            effectDisplayTexts = new ArrayList<>();
            completionFailures = new ArrayList<>();
        }

        private QuestProgressResult createResult() {
            return new QuestProgressResult(updates, effectDisplayTexts, completionFailures);
        }
    }

    private Map<Item, Integer> collectGrantedItems(
            String questId) {
        QuestStepDefinition currentStep =
                questProgress
                        .getCurrentStep(questId)
                        .orElseThrow();

        Map<Item, Integer> grantedItems =
                new LinkedHashMap<>();

        collectGrantedItems(
                effectCatalog.getByTrigger(
                        questId,
                        currentStep.getStep(),
                        QuestEffectTrigger.STEP_COMPLETE),
                grantedItems);

        if (questProgress.isCurrentStepFinal(questId)) {
            collectGrantedItems(
                    effectCatalog.getByTrigger(
                            questId,
                            0,
                            QuestEffectTrigger.QUEST_COMPLETE),
                    grantedItems);
        }

        return grantedItems;
    }

    private void collectGrantedItems(
            List<QuestEffectDefinition> effects,
            Map<Item, Integer> grantedItems) {
        for (QuestEffectDefinition effect : effects) {
            if (effect.getEffectType()
                    != QuestEffectType.GRANT_ITEM) {
                continue;
            }

            Item item = itemCatalog.getById(effect.getTargetId());

            grantedItems.merge(
                    item,
                    effect.getQuantity().orElseThrow(),
                    Math::addExact);
        }
    }
}
