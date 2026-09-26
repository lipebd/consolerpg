package rpg.narrative;

import java.util.List;

// Stores one complete immutable linear dialogue.
public final class DialogueDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String id;
    private final List<DialogueLineDefinition> lines;

    public DialogueDefinition(String id, List<DialogueLineDefinition> lines) {
        this.id = validateId(id);
        validateLines(this.id, lines);
        this.lines = List.copyOf(lines);
    }

    private static void validateLines(String dialogueId, List<DialogueLineDefinition> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "Dialogue must contain at least one line: " + dialogueId);
        }

        for (int index = 0; index < lines.size(); index++) {
            DialogueLineDefinition lineDefinition = lines.get(index);

            if (lineDefinition == null) {
                throw new IllegalArgumentException("Dialogue lines cannot contain null entries.");
            }

            if (!lineDefinition.getDialogueId().equals(dialogueId)) {
                throw new IllegalArgumentException(
                        "Dialogue line belongs to a different dialogue: "
                                + lineDefinition.getDialogueId());
            }

            int expectedLine = index + 1;

            if (lineDefinition.getLine() != expectedLine) {
                throw new IllegalArgumentException(
                        "Dialogue lines must start at 1 and be consecutive. Expected line "
                                + expectedLine
                                + " but found "
                                + lineDefinition.getLine()
                                + ".");
            }
        }
    }

    private static String validateId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Dialogue ID cannot be empty.");
        }

        String normalizedValue = value.trim();

        if (!normalizedValue.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    "Dialogue ID must use lowercase letters, numbers and single underscores: "
                            + normalizedValue);
        }

        return normalizedValue;
    }

    public String getId() {
        return id;
    }

    public List<DialogueLineDefinition> getLines() {
        return lines;
    }
}
