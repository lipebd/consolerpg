package rpg.classes.paths;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Stores questionnaire questions in their validated presentation order.
public final class ClassPathQuestionnaireCatalog {

    private final Map<String, ClassPathQuestionDefinition> questionsById;

    public ClassPathQuestionnaireCatalog() {
        this.questionsById = new LinkedHashMap<>();
    }

    public void register(ClassPathQuestionDefinition questionDefinition) {
        Objects.requireNonNull(
                questionDefinition, "Class path question definition cannot be null.");

        if (questionsById.containsKey(questionDefinition.getId())) {
            throw new IllegalArgumentException(
                    "Class path question ID is already registered: " + questionDefinition.getId());
        }

        int expectedOrder = questionsById.size() + 1;

        if (questionDefinition.getOrder() != expectedOrder) {
            throw new IllegalArgumentException(
                    "Class path question orders must start at 1 and be consecutive. "
                            + "Expected "
                            + expectedOrder
                            + " but found "
                            + questionDefinition.getOrder()
                            + ".");
        }

        questionsById.put(questionDefinition.getId(), questionDefinition);
    }

    public ClassPathQuestionDefinition getById(String questionId) {
        if (questionId == null || questionId.isBlank()) {
            throw new IllegalArgumentException("Class path question ID cannot be empty.");
        }

        ClassPathQuestionDefinition questionDefinition = questionsById.get(questionId.trim());

        if (questionDefinition == null) {
            throw new IllegalArgumentException(
                    "Class path question not found: " + questionId.trim());
        }

        return questionDefinition;
    }

    public List<ClassPathQuestionDefinition> getAll() {
        return List.copyOf(questionsById.values());
    }

    public boolean isEmpty() {
        return questionsById.isEmpty();
    }
}
