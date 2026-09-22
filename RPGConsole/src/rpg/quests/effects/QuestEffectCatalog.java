package rpg.quests.effects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores permanent quest effects in their declared trigger order.
public final class QuestEffectCatalog {

    private final Map<TriggerLocation, List<QuestEffectDefinition>> effectsByTrigger;
    private final List<QuestEffectDefinition> allEffects;

    public QuestEffectCatalog() {
        effectsByTrigger = new LinkedHashMap<>();
        allEffects = new ArrayList<>();
    }

    public void register(QuestEffectDefinition effectDefinition) {
        if (effectDefinition == null) {
            throw new IllegalArgumentException("Quest effect definition cannot be null.");
        }

        TriggerLocation triggerLocation =
                new TriggerLocation(
                        effectDefinition.getQuestId(),
                        effectDefinition.getStep(),
                        effectDefinition.getTrigger());

        List<QuestEffectDefinition> triggerEffects =
                effectsByTrigger.computeIfAbsent(triggerLocation, ignored -> new ArrayList<>());

        int expectedOrder = triggerEffects.size() + 1;

        if (effectDefinition.getOrder() != expectedOrder) {
            throw new IllegalArgumentException(
                    "Quest effect order must start at 1 and be consecutive for "
                            + describe(triggerLocation)
                            + ". Expected order "
                            + expectedOrder
                            + " but found "
                            + effectDefinition.getOrder()
                            + ".");
        }

        for (QuestEffectDefinition existingEffect : triggerEffects) {
			if (existingEffect.getEffectType() == QuestEffectType.TRANSPORT_PARTY
					&& effectDefinition.getEffectType() == QuestEffectType.TRANSPORT_PARTY) {
				throw new IllegalArgumentException(
						"Quest trigger cannot define more than one " + "TRANSPORT_PARTY effect for "
								+ describe(triggerLocation) + "."
				);
			}

            if (existingEffect.getEffectType() == effectDefinition.getEffectType()
                    && existingEffect.getTargetId().equals(effectDefinition.getTargetId())) {
                throw new IllegalArgumentException(
                        "Quest effect type and target are duplicated for "
                                + describe(triggerLocation)
                                + ": "
                                + effectDefinition.getEffectType()
                                + ":"
                                + effectDefinition.getTargetId());
            }
        }

        triggerEffects.add(effectDefinition);
        allEffects.add(effectDefinition);
    }

    public List<QuestEffectDefinition> getByTrigger(
            String questId, int step, QuestEffectTrigger trigger) {
        if (questId == null || questId.isBlank()) {
            throw new IllegalArgumentException("Quest ID cannot be empty.");
        }

        if (trigger == null) {
            throw new IllegalArgumentException("Quest effect trigger cannot be null.");
        }

        TriggerLocation triggerLocation = new TriggerLocation(questId, step, trigger);

        return List.copyOf(effectsByTrigger.getOrDefault(triggerLocation, List.of()));
    }

    public List<QuestEffectDefinition> getAll() {
        return List.copyOf(allEffects);
    }

    private static String describe(TriggerLocation triggerLocation) {
        return "quest "
                + triggerLocation.questId()
                + ", step "
                + triggerLocation.step()
                + ", trigger "
                + triggerLocation.trigger();
    }

    private record TriggerLocation(String questId, int step, QuestEffectTrigger trigger) {}
}
