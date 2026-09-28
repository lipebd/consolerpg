package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import rpg.characters.Party;
import rpg.items.ItemCatalog;
import rpg.narrative.NarrativeTextProcessor;
import rpg.quests.QuestDefinition;
import rpg.quests.QuestObjectiveDefinition;
import rpg.quests.QuestProgress;
import rpg.quests.QuestStatus;
import rpg.quests.QuestStepDefinition;
import rpg.quests.rewards.QuestRewardCatalog;

// Displays active and completed quests without advancing world time.
public final class QuestMenu {

    private final ConsoleInput consoleInput;
    private final Party party;
    private final QuestProgress questProgress;
    private final QuestRewardCatalog rewardCatalog;
    private final QuestRewardSummaryFormatter rewardSummaryFormatter;
    private final QuestProgressCoordinator questProgressCoordinator;
	private final BestiaryMenu bestiaryMenu;

    public QuestMenu(
            ConsoleInput consoleInput,
            Party party,
            QuestProgress questProgress,
            QuestRewardCatalog rewardCatalog,
            ItemCatalog itemCatalog,
            QuestProgressCoordinator questProgressCoordinator,
			BestiaryMenu bestiaryMenu) {
        this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");

        this.party = Objects.requireNonNull(party, "Party cannot be null.");

        this.questProgress =
                Objects.requireNonNull(questProgress, "Quest progress cannot be null.");

        this.rewardCatalog =
                Objects.requireNonNull(rewardCatalog, "Quest reward catalog cannot be null.");

        this.rewardSummaryFormatter =
                new QuestRewardSummaryFormatter(
                        Objects.requireNonNull(itemCatalog, "Item catalog cannot be null."));

        this.questProgressCoordinator =
                Objects.requireNonNull(
                        questProgressCoordinator, "Quest progress coordinator cannot be null.");

		this.bestiaryMenu = Objects.requireNonNull(bestiaryMenu, "Bestiary menu cannot be null.");
    }

    public void open() {
        String feedbackMessage = null;

        while (true) {
            ConsoleUI.clearScreen();

            ConsoleUI.displayHeading("Diário");

            if (feedbackMessage != null) {
                System.out.println();
                ConsoleUI.displayWrappedText(feedbackMessage);
            }

            System.out.println();
            System.out.println("[1] Missões ativas");
            System.out.println("[2] Missões concluídas");
			System.out.println("[3] Bestiário");
            if (feedbackMessage != null) {
                System.out.println();
                ConsoleUI.displayWrappedText(feedbackMessage);
            }
            System.out.println();
            System.out.println("[0] Voltar");
            System.out.print("> ");

            String choice = consoleInput.readTrimmedLine();

            switch (choice) {
                case "1" -> {
                    openActiveQuests();
                    feedbackMessage = null;
                }

                case "2" -> {
                    openCompletedQuests();
                    feedbackMessage = null;
                }

				case "3" -> {
					bestiaryMenu.open();
					feedbackMessage = null;
				}

                case "0" -> {
                    return;
                }

                default -> feedbackMessage = "Opção inválida.";
            }
        }
    }

    private void openActiveQuests() {
        while (true) {
            Optional<QuestDefinition> selectedQuest =
                    selectQuest(
                            "MISSÕES ATIVAS",
                            questProgress.getActiveQuests(),
                            "Nenhuma missão ativa.");

            if (selectedQuest.isEmpty()) {
                return;
            }

            openActiveQuest(selectedQuest.orElseThrow());
        }
    }

    private void openCompletedQuests() {
        while (true) {
            Optional<QuestDefinition> selectedQuest =
                    selectQuest(
                            "MISSÕES CONCLUÍDAS",
                            questProgress.getCompletedQuests(),
                            "Nenhuma missão concluída.");

            if (selectedQuest.isEmpty()) {
                return;
            }

            displayCompletedQuest(selectedQuest.orElseThrow());
        }
    }

	private Optional<QuestDefinition> selectQuest(String heading, List<QuestDefinition> quests, String emptyMessage) {
		Objects.requireNonNull(heading, "Quest-list heading cannot be null.");
		Objects.requireNonNull(quests, "Quest list cannot be null.");
		Objects.requireNonNull(emptyMessage, "Empty quest-list message cannot be null.");
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();

			ConsoleUI.displayHeading(heading);
			System.out.println();

			if (quests.isEmpty()) {
				ConsoleUI.displayWrappedText(emptyMessage);
			} else {
				for (int index = 0; index < quests.size(); index++) {
					System.out.println("[" + (index + 1) + "] " + quests.get(index).getTitle());
				}
			}

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= quests.size()) {
					feedbackMessage = "Missão inválida.";
					continue;
				}

				return Optional.of(quests.get(selectedIndex));
			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número de uma missão.";
			}
		}
	}

	private void openActiveQuest(QuestDefinition questDefinition) {
		String feedbackMessage = null;
		while (questProgress.getStatus(questDefinition.getId()) == QuestStatus.ACTIVE) {
			QuestStepDefinition currentStep = questProgress.getCurrentStep(questDefinition.getId()).orElseThrow(
					() -> new IllegalStateException(
							"Active quest is missing its current step: " + questDefinition.getId()
					)
			);

			String questDescription = NarrativeTextProcessor.resolve(questDefinition.getDescription(), party);

			String stepDescription = NarrativeTextProcessor.resolve(currentStep.getDescription(), party);

			List<String> objectiveTexts = currentStep.getObjectives().stream()
					.map(objective -> NarrativeTextProcessor.resolve(objective.getText(), party)).toList();

			List<String> rewardSummaries = rewardSummaryFormatter
					.format(rewardCatalog.getByQuestId(questDefinition.getId()));

			boolean canRetryCompletion = questProgressCoordinator.canRetryQuestCompletion(questDefinition.getId());

            ConsoleUI.clearScreen();

            ConsoleUI.displayHeading("Missão ativa");
            System.out.println();
            System.out.println(questDefinition.getTitle());
            System.out.println();

            ConsoleUI.displayWrappedText(questDescription);

            System.out.println();
            System.out.println("Etapa " + currentStep.getStep() + " - " + currentStep.getTitle());
            System.out.println();

            ConsoleUI.displayWrappedText(stepDescription);

            System.out.println();
            System.out.println("Objetivos:");

            displayObjectives(questDefinition, currentStep, objectiveTexts);

            System.out.println();
            System.out.println("Recompensas:");

            displayRewards(rewardSummaries);
            System.out.println();

            if (canRetryCompletion) {
                System.out.println("[1] Concluir missão");
            }

            if (feedbackMessage != null) {
                System.out.println();
                ConsoleUI.displayWrappedText(feedbackMessage);
            }

            System.out.println();
            System.out.println("[0] Voltar");
            System.out.print("> ");
            String choice = consoleInput.readTrimmedLine();

            if (choice.equals("0")) {
                return;
            }

            if (choice.equals("1") && canRetryCompletion) {
                feedbackMessage = null;

                questProgressCoordinator.retryQuestCompletionAndDisplay(
                        questDefinition.getId());

                continue;
            }

            feedbackMessage = "Opção inválida.";
        }
    }

    private void displayObjectives(
            QuestDefinition questDefinition,
            QuestStepDefinition currentStep,
            List<String> objectiveTexts) {
        List<QuestObjectiveDefinition> objectives = currentStep.getObjectives();

        for (int index = 0; index < objectives.size(); index++) {
            QuestObjectiveDefinition objective = objectives.get(index);

            int currentQuantity =
                    questProgress.getObjectiveProgress(questDefinition.getId(), objective.getId());

            String objectiveLine =
                    objectiveTexts.get(index)
                            + " ("
                            + currentQuantity
                            + "/"
                            + objective.getRequiredQuantity()
                            + ")";

            ConsoleUI.displayWrappedText(objectiveLine, "- ");
        }
    }

	private static void displayRewards(List<String> rewardSummaries) {
		if (rewardSummaries.isEmpty()) {
			System.out.println("- Nenhuma recompensa material.");
			return;
		}

		for (String rewardSummary : rewardSummaries) {
			ConsoleUI.displayWrappedText(rewardSummary, "- ");
		}
	}

	private void displayCompletedQuest(QuestDefinition questDefinition) {
		String resolvedDescription = NarrativeTextProcessor.resolve(questDefinition.getDescription(), party);

		ConsoleUI.clearScreen();

		ConsoleUI.displayHeading("Missão concluída");
		System.out.println();
		System.out.println(questDefinition.getTitle());
		System.out.println();

		ConsoleUI.displayWrappedText(resolvedDescription);

		System.out.println();
		System.out.print("[Enter] Voltar");

		consoleInput.readLine();
	}
}
