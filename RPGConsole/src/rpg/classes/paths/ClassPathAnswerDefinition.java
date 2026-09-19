package rpg.classes.paths;

import rpg.narrative.NarrativeTextProcessor;

// Stores one immutable questionnaire answer and its recommended class path.
public final class ClassPathAnswerDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    private final String questionId;
    private final int order;
    private final String text;
    private final String classId;

    public ClassPathAnswerDefinition(String questionId, int order, String text, String classId) {
        this.questionId = requireId(questionId, "Question ID");

        if (order < 1) {
            throw new IllegalArgumentException(
                    "Class path answer order must be greater than 0: " + this.questionId);
        }

        this.order = order;
        this.text =
                NarrativeTextProcessor.decodeTemplate(
                        text, "Class path answer text for " + this.questionId);
        this.classId = requireId(classId, "Answer class ID");
    }

    private static String requireId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }

        String normalizedValue = value.trim();

        if (!normalizedValue.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    fieldName
                            + " must use lowercase letters, numbers and single underscores: "
                            + normalizedValue);
        }

        return normalizedValue;
    }

    public String getQuestionId() {
        return questionId;
    }

    public int getOrder() {
        return order;
    }

    public String getText() {
        return text;
    }

    public String getClassId() {
        return classId;
    }
}
