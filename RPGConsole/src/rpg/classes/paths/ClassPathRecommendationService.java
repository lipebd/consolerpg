package rpg.classes.paths;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Scores questionnaire answers and identifies every highest-scoring class path.
public final class ClassPathRecommendationService {

    private final ClassPathQuestionnaireCatalog questionnaireCatalog;
    private final ClassPathCatalog classPathCatalog;

    public ClassPathRecommendationService(
            ClassPathQuestionnaireCatalog questionnaireCatalog, ClassPathCatalog classPathCatalog) {
        this.questionnaireCatalog =
                Objects.requireNonNull(
                        questionnaireCatalog, "Class path questionnaire catalog cannot be null.");

        this.classPathCatalog =
                Objects.requireNonNull(classPathCatalog, "Class path catalog cannot be null.");
    }

    public ClassPathRecommendationResult evaluate(List<ClassPathAnswerDefinition> selectedAnswers) {
        Objects.requireNonNull(selectedAnswers, "Selected class path answers cannot be null.");

        List<ClassPathQuestionDefinition> questions = questionnaireCatalog.getAll();

        if (questions.isEmpty()) {
            throw new IllegalStateException(
                    "Class path recommendation requires at least one question.");
        }

        if (selectedAnswers.size() != questions.size()) {
            throw new IllegalArgumentException(
                    "Class path recommendation requires exactly one answer per question.");
        }

        Map<String, ClassPathAnswerDefinition> selectedAnswersByQuestionId =
                indexSelectedAnswers(selectedAnswers);

        Map<String, Integer> scoresByClassId = new LinkedHashMap<>();

        for (ClassPathQuestionDefinition question : questions) {
            ClassPathAnswerDefinition selectedAnswer =
                    selectedAnswersByQuestionId.get(question.getId());

            if (selectedAnswer == null) {
                throw new IllegalArgumentException(
                        "Class path question is missing a selected answer: " + question.getId());
            }

            ClassPathAnswerDefinition canonicalAnswer =
                    question.getAnswerByOrder(selectedAnswer.getOrder());

            if (!canonicalAnswer.getClassId().equals(selectedAnswer.getClassId())) {
                throw new IllegalArgumentException(
                        "Selected answer does not match the questionnaire definition for "
                                + question.getId()
                                + ".");
            }

            validateRecommendationTarget(canonicalAnswer.getClassId());

            scoresByClassId.merge(canonicalAnswer.getClassId(), 1, Math::addExact);
        }

        int highestScore =
                scoresByClassId.values().stream().mapToInt(Integer::intValue).max().orElseThrow();

        List<ClassPathDefinition> leadingPaths = new ArrayList<>();

        for (ClassPathDefinition pathDefinition : classPathCatalog.getAll()) {
            if (scoresByClassId.getOrDefault(pathDefinition.getClassId(), 0) == highestScore) {
                leadingPaths.add(pathDefinition);
            }
        }

        return new ClassPathRecommendationResult(leadingPaths);
    }

    private Map<String, ClassPathAnswerDefinition> indexSelectedAnswers(
            List<ClassPathAnswerDefinition> selectedAnswers) {
        Map<String, ClassPathAnswerDefinition> answersByQuestionId = new LinkedHashMap<>();

        for (ClassPathAnswerDefinition selectedAnswer : selectedAnswers) {
            Objects.requireNonNull(selectedAnswer, "Selected class path answer cannot be null.");

            ClassPathAnswerDefinition previousAnswer =
                    answersByQuestionId.putIfAbsent(selectedAnswer.getQuestionId(), selectedAnswer);

            if (previousAnswer != null) {
                throw new IllegalArgumentException(
                        "Multiple answers were selected for class path question: "
                                + selectedAnswer.getQuestionId());
            }
        }

        return answersByQuestionId;
    }

    private void validateRecommendationTarget(String classId) {
        if (classId.equals(ClassPathRules.FINAL_FLAGELO_CLASS_ID)) {
            throw new IllegalArgumentException(
                    "Final Flagelo cannot be recommended by the questionnaire.");
        }

        classPathCatalog.getByClassId(classId);
    }
}
