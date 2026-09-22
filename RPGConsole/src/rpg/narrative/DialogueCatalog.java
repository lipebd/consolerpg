package rpg.narrative;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores complete dialogues in stable definition order.
public final class DialogueCatalog {

    private final Map<String, DialogueDefinition> dialoguesById;

    public DialogueCatalog() {
        dialoguesById = new LinkedHashMap<>();
    }

    public void register(DialogueDefinition dialogueDefinition) {
        if (dialogueDefinition == null) {
            throw new IllegalArgumentException("Dialogue definition cannot be null.");
        }

        String dialogueId = dialogueDefinition.getId();

        if (dialoguesById.containsKey(dialogueId)) {
            throw new IllegalArgumentException("Dialogue ID is already registered: " + dialogueId);
        }

        dialoguesById.put(dialogueId, dialogueDefinition);
    }

    public DialogueDefinition getById(String dialogueId) {
        if (dialogueId == null || dialogueId.isBlank()) {
            throw new IllegalArgumentException("Dialogue ID cannot be empty.");
        }

        DialogueDefinition dialogueDefinition = dialoguesById.get(dialogueId);

        if (dialogueDefinition == null) {
            throw new IllegalArgumentException("Dialogue not found: " + dialogueId);
        }

        return dialogueDefinition;
    }

    public List<DialogueDefinition> getAll() {
        return List.copyOf(dialoguesById.values());
    }
}
