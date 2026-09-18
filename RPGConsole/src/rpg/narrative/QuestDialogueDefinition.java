package rpg.narrative;

// Associates one quest interaction context with a dialogue.
public final class QuestDialogueDefinition {

    private final String questId;
    private final int step;
    private final QuestDialogueContext context;
    private final String dialogueId;

    public QuestDialogueDefinition(
            String questId, int step, QuestDialogueContext context, String dialogueId) {
        this.questId = requireId(questId, "Quest");
        this.context = requireContext(context);
        this.step = validateStep(step, context);
        this.dialogueId = requireId(dialogueId, "Dialogue");
    }

    private static QuestDialogueContext requireContext(QuestDialogueContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Quest dialogue context cannot be null.");
        }

        return context;
    }

    private static int validateStep(int step, QuestDialogueContext context) {
        switch (context) {
            case OFFER, ACCEPT, DECLINE -> {
                if (step != 0) {
                    throw new IllegalArgumentException(
                            context + " quest dialogue must use step 0.");
                }
            }

            case PENDING, COMPLETE -> {
                if (step <= 0) {
                    throw new IllegalArgumentException(
                            context + " quest dialogue must use a positive step number.");
                }
            }
        }

        return step;
    }

    private static String requireId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " ID cannot be empty.");
        }

        return value.trim();
    }

    public String getQuestId() {
        return questId;
    }

    public int getStep() {
        return step;
    }

    public QuestDialogueContext getContext() {
        return context;
    }

    public String getDialogueId() {
        return dialogueId;
    }
}
