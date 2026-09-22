package rpg.classes.paths;

import java.util.List;
import java.util.Objects;

import rpg.narrative.NarrativeTextProcessor;

// Stores one immutable questionnaire question and its ordered answers.
public final class ClassPathQuestionDefinition {

    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";
    private static final int MINIMUM_ANSWER_COUNT = 2;

    private final String id;
    private final int order;
    private final String text;
    private final List<ClassPathAnswerDefinition> answers;

    public ClassPathQuestionDefinition(
            String id, int order, String text, List<ClassPathAnswerDefinition> answers) {
        this.id = requireId(id);

        if (order < 1) {
            throw new IllegalArgumentException(
                    "Class path question order must be greater than 0: " + this.id);
        }

        this.order = order;
        this.text =
                NarrativeTextProcessor.decodeTemplate(
                        text, "Class path question text for " + this.id);
        this.answers =
                List.copyOf(
                        Objects.requireNonNull(
                                answers, "Class path question answers cannot be null."));

        validateAnswers();
    }

    private void validateAnswers() {
        if (answers.size() < MINIMUM_ANSWER_COUNT) {
            throw new IllegalArgumentException(
                    "Class path question must contain at least "
                            + MINIMUM_ANSWER_COUNT
                            + " answers: "
                            + id);
        }

        for (int index = 0; index < answers.size(); index++) {
            ClassPathAnswerDefinition answer =
                    Objects.requireNonNull(answers.get(index), "Class path answer cannot be null.");

            if (!answer.getQuestionId().equals(id)) {
                throw new IllegalArgumentException(
                        "Class path answer belongs to a different question: "
                                + answer.getQuestionId()
                                + " for "
                                + id);
            }

            int expectedOrder = index + 1;

            if (answer.getOrder() != expectedOrder) {
                throw new IllegalArgumentException(
                        "Class path answer orders must start at 1 and be consecutive for "
                                + id
                                + ". Expected "
                                + expectedOrder
                                + " but found "
                                + answer.getOrder()
                                + ".");
            }
        }
    }

    private static String requireId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Class path question ID cannot be empty.");
        }

        String normalizedValue = value.trim();

        if (!normalizedValue.matches(ID_PATTERN)) {
            throw new IllegalArgumentException(
                    "Class path question ID must use lowercase letters, numbers "
                            + "and single underscores: "
                            + normalizedValue);
        }

        return normalizedValue;
    }

    public ClassPathAnswerDefinition getAnswerByOrder(int answerOrder) {
        if (answerOrder < 1 || answerOrder > answers.size()) {
            throw new IllegalArgumentException(
                    "Class path answer order is unavailable for " + id + ": " + answerOrder);
        }

        return answers.get(answerOrder - 1);
    }

    public String getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public String getText() {
        return text;
    }

    public List<ClassPathAnswerDefinition> getAnswers() {
        return answers;
    }
}
