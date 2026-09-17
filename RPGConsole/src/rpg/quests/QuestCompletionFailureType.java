package rpg.quests;

// Identifies a correctable state blocking a quest-step transition.
public enum QuestCompletionFailureType {
    MISSING_DELIVERY_ITEM,
    GOLD_CAPACITY_EXCEEDED,
    ITEM_CAPACITY_EXCEEDED
}