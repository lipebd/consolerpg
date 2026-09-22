package rpg.narrative;

import java.util.Optional;

// Stores one immutable utterance inside a linear dialogue.
public final class DialogueLineDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String dialogueId;
    private final int line;
    private final DialogueSpeakerType speakerType;
    private final Optional<String> speakerId;
    private final String text;

    public DialogueLineDefinition(
            String dialogueId,
            int line,
            DialogueSpeakerType speakerType,
            Optional<String> speakerId,
            String text) {
        this.dialogueId = validateId(dialogueId, "Dialogue");

        if (line <= 0) {
            throw new IllegalArgumentException("Dialogue line number must be positive.");
        }

        if (speakerType == null) {
            throw new IllegalArgumentException("Dialogue speaker type cannot be null.");
        }

        if (speakerId == null) {
            throw new IllegalArgumentException("Dialogue speaker ID cannot be null.");
        }

        this.line = line;
        this.speakerType = speakerType;
        this.speakerId = validateSpeakerId(speakerType, speakerId);
        this.text = NarrativeTextProcessor.decodeTemplate(text, "Dialogue text");
    }

    private static Optional<String> validateSpeakerId(
            DialogueSpeakerType speakerType, Optional<String> speakerId) {
        switch (speakerType) {
            case NPC:
            case ENEMY:
            case RECRUIT:
                if (speakerId.isEmpty()) {
                    throw new IllegalArgumentException(
                            speakerType + " dialogue speaker requires an ID.");
                }

                return Optional.of(validateId(speakerId.orElseThrow(), "Dialogue speaker"));

            case LEADER:
            case NARRATOR:
                if (speakerId.isPresent()) {
                    throw new IllegalArgumentException(
                            speakerType + " dialogue speaker cannot define an ID.");
                }

                return Optional.empty();
        }

        throw new IllegalStateException("Unsupported dialogue speaker type: " + speakerType);
    }

    private static String validateId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
        }

        String normalizedValue = value.trim();

        if (!normalizedValue.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    fieldName
                            + " ID must use lowercase letters, numbers and single underscores: "
                            + normalizedValue);
        }

        return normalizedValue;
    }

    public String getDialogueId() {
        return dialogueId;
    }

    public int getLine() {
        return line;
    }

    public DialogueSpeakerType getSpeakerType() {
        return speakerType;
    }

    public Optional<String> getSpeakerId() {
        return speakerId;
    }

    public String getText() {
        return text;
    }
}
