package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rpg.characters.Party;
import rpg.classes.paths.ClassPathAnswerDefinition;
import rpg.classes.paths.ClassPathQuestionDefinition;
import rpg.classes.paths.ClassPathQuestionnaireCatalog;
import rpg.narrative.NarrativeTextProcessor;

// Presents every questionnaire question sequentially without navigation.
public final class ClassPathQuestionnaireMenu {

	private final ConsoleInput consoleInput;

	public ClassPathQuestionnaireMenu(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public List<ClassPathAnswerDefinition> open(ClassPathQuestionnaireCatalog questionnaireCatalog, Party party) {
		Objects.requireNonNull(questionnaireCatalog, "Class path questionnaire catalog cannot be null.");
		Objects.requireNonNull(party, "Party cannot be null.");

		List<ResolvedQuestion> resolvedQuestions = resolveQuestions(questionnaireCatalog, party);

		if (resolvedQuestions.isEmpty()) {
			throw new IllegalStateException("Class path questionnaire contains no questions.");
		}

		List<ClassPathAnswerDefinition> selectedAnswers = new ArrayList<>();

		for (int index = 0; index < resolvedQuestions.size(); index++) {
			selectedAnswers.add(selectAnswer(resolvedQuestions.get(index), index + 1, resolvedQuestions.size()));
		}

		return List.copyOf(selectedAnswers);
	}

	private List<ResolvedQuestion> resolveQuestions(ClassPathQuestionnaireCatalog questionnaireCatalog, Party party) {
		List<ResolvedQuestion> resolvedQuestions = new ArrayList<>();

		for (ClassPathQuestionDefinition question : questionnaireCatalog.getAll()) {
			List<ResolvedAnswer> resolvedAnswers = new ArrayList<>();

			for (ClassPathAnswerDefinition answer : question.getAnswers()) {
				resolvedAnswers
						.add(new ResolvedAnswer(answer, NarrativeTextProcessor.resolve(answer.getText(), party)));
			}

			resolvedQuestions.add(
					new ResolvedQuestion(
							NarrativeTextProcessor.resolve(question.getText(), party),
							List.copyOf(resolvedAnswers)
					)
			);
		}

		return List.copyOf(resolvedQuestions);
	}

	private ClassPathAnswerDefinition selectAnswer(ResolvedQuestion question, int questionNumber, int questionCount) {
		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();

			ConsoleUI.displayHeading(
					"Julgamento dos Sete Presságios",
					"Pergunta " + questionNumber + "/" + questionCount
			);

			System.out.println();
			ConsoleUI.displayWrappedText(question.text());

			System.out.println();

			for (int index = 0; index < question.answers().size(); index++) {
				ResolvedAnswer answer = question.answers().get(index);

				ConsoleUI.displayWrappedText("[" + (index + 1) + "] " + answer.text());
			}

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex >= 0 && selectedIndex < question.answers().size()) {
					return question.answers().get(selectedIndex).definition();
				}

			} catch (NumberFormatException exception) {
				// Invalid input is handled through the shared feedback below.
			}

			feedbackMessage = "Opção inválida.";
		}
	}

	private record ResolvedQuestion(String text, List<ResolvedAnswer> answers) {
	}

	private record ResolvedAnswer(ClassPathAnswerDefinition definition, String text) {
	}
}
