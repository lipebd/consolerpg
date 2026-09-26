package rpg.classes.paths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Loads complete class-path questionnaire questions and answers.
public final class ClassPathQuestionnaireLoader {

    private static final int QUESTION_FIELD_COUNT = 3;
    private static final int ANSWER_FIELD_COUNT = 4;
    private static final String ID_PATTERN = "[a-z0-9]+(?:_[a-z0-9]+)*";

    public ClassPathQuestionnaireCatalog load(
            Path questionFilePath, Path answerFilePath, ClassPathCatalog classPathCatalog)
            throws IOException {
        Objects.requireNonNull(questionFilePath, "Class path question file path cannot be null.");
        Objects.requireNonNull(answerFilePath, "Class path answer file path cannot be null.");
        Objects.requireNonNull(classPathCatalog, "Class path catalog cannot be null.");

        Map<String, QuestionDraft> questionDrafts = loadQuestionDrafts(questionFilePath);

        Map<String, List<AnswerDraft>> answerDraftsByQuestionId =
                loadAnswerDrafts(answerFilePath, questionDrafts, classPathCatalog);

        return buildCatalog(
                questionFilePath, answerFilePath, questionDrafts, answerDraftsByQuestionId);
    }

    private static Map<String, QuestionDraft> loadQuestionDrafts(Path questionFilePath)
            throws IOException {
        Map<String, QuestionDraft> questionDrafts = new LinkedHashMap<>();

        List<String> lines = Files.readAllLines(questionFilePath, StandardCharsets.UTF_8);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            try {
                QuestionDraft draft = parseQuestionDraft(line, index + 1);

                QuestionDraft previousDraft = questionDrafts.putIfAbsent(draft.id, draft);

                if (previousDraft != null) {
                    throw new IllegalArgumentException(
                            "Class path question ID is duplicated: " + draft.id);
                }

            } catch (IllegalArgumentException exception) {
                throw invalidDefinition("question", questionFilePath, index + 1, line, exception);
            }
        }

        return questionDrafts;
    }

    private static QuestionDraft parseQuestionDraft(String line, int physicalLine) {
        String[] fields = line.split(";", -1);

        if (fields.length != QUESTION_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected "
                            + QUESTION_FIELD_COUNT
                            + " fields but found "
                            + fields.length
                            + ".");
        }

        return new QuestionDraft(
                requireId(fields[0], "Class path question ID"),
                parsePositiveInteger(fields[1], "class path question order"),
                fields[2],
                physicalLine,
                line);
    }

    private static Map<String, List<AnswerDraft>> loadAnswerDrafts(
            Path answerFilePath,
            Map<String, QuestionDraft> questionDrafts,
            ClassPathCatalog classPathCatalog)
            throws IOException {
        Map<String, List<AnswerDraft>> answerDraftsByQuestionId = new LinkedHashMap<>();

        List<String> lines = Files.readAllLines(answerFilePath, StandardCharsets.UTF_8);

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            try {
                AnswerDraft draft =
                        parseAnswerDraft(line, index + 1, questionDrafts, classPathCatalog);

                answerDraftsByQuestionId
                        .computeIfAbsent(
                                draft.definition.getQuestionId(), ignored -> new ArrayList<>())
                        .add(draft);

            } catch (IllegalArgumentException exception) {
                throw invalidDefinition("answer", answerFilePath, index + 1, line, exception);
            }
        }

        return answerDraftsByQuestionId;
    }

    private static AnswerDraft parseAnswerDraft(
            String line,
            int physicalLine,
            Map<String, QuestionDraft> questionDrafts,
            ClassPathCatalog classPathCatalog) {
        String[] fields = line.split(";", -1);

        if (fields.length != ANSWER_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + ANSWER_FIELD_COUNT + " fields but found " + fields.length + ".");
        }

        ClassPathAnswerDefinition definition =
                new ClassPathAnswerDefinition(
                        fields[0],
                        parsePositiveInteger(fields[1], "class path answer order"),
                        fields[2],
                        fields[3]);

        if (!questionDrafts.containsKey(definition.getQuestionId())) {
            throw new IllegalArgumentException(
                    "Class path answer references an unknown question: "
                            + definition.getQuestionId());
        }

        if (definition.getClassId().equals(ClassPathRules.FINAL_FLAGELO_CLASS_ID)) {
            throw new IllegalArgumentException(
                    "Final Flagelo cannot be recommended by the questionnaire: "
                            + definition.getClassId());
        }

        classPathCatalog.getByClassId(definition.getClassId());

        return new AnswerDraft(definition, physicalLine, line);
    }

    private static ClassPathQuestionnaireCatalog buildCatalog(
            Path questionFilePath,
            Path answerFilePath,
            Map<String, QuestionDraft> questionDrafts,
            Map<String, List<AnswerDraft>> answerDraftsByQuestionId) {
        List<QuestionDraft> orderedQuestionDrafts = new ArrayList<>(questionDrafts.values());

        orderedQuestionDrafts.sort(Comparator.comparingInt(draft -> draft.order));

        validateQuestionOrder(questionFilePath, orderedQuestionDrafts);

        ClassPathQuestionnaireCatalog questionnaireCatalog = new ClassPathQuestionnaireCatalog();

        for (QuestionDraft questionDraft : orderedQuestionDrafts) {
            List<AnswerDraft> answerDrafts =
                    new ArrayList<>(
                            answerDraftsByQuestionId.getOrDefault(questionDraft.id, List.of()));

            answerDrafts.sort(Comparator.comparingInt(draft -> draft.definition.getOrder()));

            validateAnswerOrder(answerFilePath, questionDraft.id, answerDrafts);

            List<ClassPathAnswerDefinition> answerDefinitions =
                    answerDrafts.stream().map(draft -> draft.definition).toList();

            try {
                questionnaireCatalog.register(
                        new ClassPathQuestionDefinition(
                                questionDraft.id,
                                questionDraft.order,
                                questionDraft.text,
                                answerDefinitions));

            } catch (IllegalArgumentException exception) {
                throw invalidDefinition(
                        "question",
                        questionFilePath,
                        questionDraft.physicalLine,
                        questionDraft.sourceLine,
                        exception);
            }
        }

        return questionnaireCatalog;
    }

    private static void validateQuestionOrder(
            Path questionFilePath, List<QuestionDraft> orderedQuestionDrafts) {
        for (int index = 0; index < orderedQuestionDrafts.size(); index++) {
            QuestionDraft draft = orderedQuestionDrafts.get(index);
            int expectedOrder = index + 1;

            if (draft.order != expectedOrder) {
                throw invalidDefinition(
                        "question",
                        questionFilePath,
                        draft.physicalLine,
                        draft.sourceLine,
                        new IllegalArgumentException(
                                "Class path question orders must start at 1 and be consecutive. Expected "
                                        + expectedOrder
                                        + " but found "
                                        + draft.order
                                        + "."));
            }
        }
    }

    private static void validateAnswerOrder(
            Path answerFilePath, String questionId, List<AnswerDraft> orderedAnswerDrafts) {
        for (int index = 0; index < orderedAnswerDrafts.size(); index++) {
            AnswerDraft draft = orderedAnswerDrafts.get(index);
            int expectedOrder = index + 1;

            if (draft.definition.getOrder() != expectedOrder) {
                throw invalidDefinition(
                        "answer",
                        answerFilePath,
                        draft.physicalLine,
                        draft.sourceLine,
                        new IllegalArgumentException(
                                "Class path answer orders must start at 1 and be consecutive for "
                                        + questionId
                                        + ". Expected "
                                        + expectedOrder
                                        + " but found "
                                        + draft.definition.getOrder()
                                        + "."));
            }
        }
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

    private static int parsePositiveInteger(String value, String fieldName) {
        String normalizedValue = value.trim();

        try {
            int parsedValue = Integer.parseInt(normalizedValue);

            if (parsedValue < 1) {
                throw new IllegalArgumentException(fieldName + " must be greater than 0.");
            }

            return parsedValue;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid " + fieldName + ": " + normalizedValue, exception);
        }
    }

    private static IllegalArgumentException invalidDefinition(
            String definitionType,
            Path filePath,
            int lineNumber,
            String line,
            IllegalArgumentException cause) {
        return new IllegalArgumentException(
                "Invalid class path "
                        + definitionType
                        + " definition in "
                        + filePath
                        + " at line "
                        + lineNumber
                        + ": "
                        + line
                        + ". "
                        + cause.getMessage(),
                cause);
    }

    private static final class QuestionDraft {

        private final String id;
        private final int order;
        private final String text;
        private final int physicalLine;
        private final String sourceLine;

        private QuestionDraft(
                String id, int order, String text, int physicalLine, String sourceLine) {
            this.id = id;
            this.order = order;
            this.text = text;
            this.physicalLine = physicalLine;
            this.sourceLine = sourceLine;
        }
    }

    private static final class AnswerDraft {

        private final ClassPathAnswerDefinition definition;
        private final int physicalLine;
        private final String sourceLine;

        private AnswerDraft(
                ClassPathAnswerDefinition definition, int physicalLine, String sourceLine) {
            this.definition = definition;
            this.physicalLine = physicalLine;
            this.sourceLine = sourceLine;
        }
    }
}
