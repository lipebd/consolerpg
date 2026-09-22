package rpg.ui;

import java.util.List;
import java.util.Objects;

import rpg.items.ItemCatalog;
import rpg.quests.QuestCatalog;
import rpg.quests.QuestCompletionFailure;
import rpg.quests.QuestDefinition;
import rpg.quests.QuestProgressResult;
import rpg.quests.QuestProgressUpdate;
import rpg.quests.QuestStepDefinition;
import rpg.quests.rewards.QuestRewardCatalog;
import rpg.quests.rewards.QuestRewardDefinition;
import rpg.quests.QuestProgressUpdateType;

// Presents grouped quest progress, effects, failures, and rewards.
public final class QuestProgressPresenter {

	private final QuestRewardSummaryFormatter rewardSummaryFormatter;
    private final ConsoleInput consoleInput;
    private final QuestCatalog questCatalog;
    private final QuestRewardCatalog rewardCatalog;
    private final ItemCatalog itemCatalog;

    public QuestProgressPresenter(
            ConsoleInput consoleInput,
            QuestCatalog questCatalog,
            QuestRewardCatalog rewardCatalog,
            ItemCatalog itemCatalog) {
        this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");

        this.questCatalog = Objects.requireNonNull(questCatalog, "Quest catalog cannot be null.");

        this.rewardCatalog =
                Objects.requireNonNull(rewardCatalog, "Quest reward catalog cannot be null.");

        this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");

        this.rewardSummaryFormatter = new QuestRewardSummaryFormatter(this.itemCatalog);
    }

    public void display(QuestProgressResult result) {
        Objects.requireNonNull(result, "Quest progress result cannot be null.");

        if (!result.hasUpdates()
                && !result.hasEffectDisplayTexts()
                && !result.hasCompletionFailures()) {
            return;
        }

        ConsoleUI.clearScreen();

        if (containsQuestCompletion(result)) {
            System.out.println("QUEST CONCLUÍDA");
        } else {
            System.out.println("PROGRESSO DE QUEST");
        }

        System.out.println();

        displayUpdates(result.getUpdates());
        displayEffects(result.getEffectDisplayTexts());
        displayFailures(result.getCompletionFailures());
        displayCompletedQuestRewards(result.getUpdates());

        System.out.print("[Enter] Continuar");
        consoleInput.readLine();
    }

    private void displayUpdates(List<QuestProgressUpdate> updates) {
        for (QuestProgressUpdate update : updates) {
            QuestDefinition questDefinition = questCatalog.getById(update.getQuestId());

            switch (update.getUpdateType()) {
                case OBJECTIVE_PROGRESS ->
                        System.out.println(
                                questDefinition.getTitle()
                                        + ": "
                                        + update.getCurrentQuantity()
                                        + "/"
                                        + update.getRequiredQuantity()
                                        + ".");

                case OBJECTIVE_COMPLETED ->
                        System.out.println(questDefinition.getTitle() + ": objetivo concluído.");

                case STEP_COMPLETED -> {
                    QuestStepDefinition stepDefinition = getStep(questDefinition, update.getStep());

                    System.out.println(
                            questDefinition.getTitle()
                                    + ": etapa concluída — "
                                    + stepDefinition.getTitle()
                                    + ".");
                }

                case QUEST_COMPLETED ->
                        System.out.println("Quest concluída: " + questDefinition.getTitle() + ".");
            }
        }

        if (!updates.isEmpty()) {
            System.out.println();
        }
    }

    private static void displayEffects(
            List<String> effectDisplayTexts) {
        if (effectDisplayTexts.isEmpty()) {
            return;
        }

        for (String displayText : effectDisplayTexts) {
            System.out.println("- " + displayText);
        }

        System.out.println();
    }

    private void displayFailures(List<QuestCompletionFailure> failures) {
        if (failures.isEmpty()) {
            return;
        }

        System.out.println("Não foi possível concluir:");

        for (QuestCompletionFailure failure : failures) {
            System.out.println("- " + describeFailure(failure));
        }

        System.out.println();
    }

    private String describeFailure(QuestCompletionFailure failure) {
        return switch (failure.getFailureType()) {
            case MISSING_DELIVERY_ITEM -> {
                String itemName =
                        itemCatalog.getById(failure.getTargetId().orElseThrow()).getName();

                yield itemName
                        + ": "
                        + failure.getAvailableQuantity()
                        + "/"
                        + failure.getRequiredQuantity()
                        + ".";
            }

            case GOLD_CAPACITY_EXCEEDED ->
                    "Espaço insuficiente para receber "
                            + failure.getRequiredQuantity()
                            + " GOLD. Capacidade disponível: "
                            + failure.getAvailableQuantity()
                            + ".";

            case ITEM_CAPACITY_EXCEEDED -> {
                String itemName =
                        itemCatalog.getById(failure.getTargetId().orElseThrow()).getName();

                yield "Espaço insuficiente para receber "
                        + failure.getRequiredQuantity()
                        + "x "
                        + itemName
                        + ". Capacidade disponível: "
                        + failure.getAvailableQuantity()
                        + ".";
            }
        };
    }

    private void displayCompletedQuestRewards(List<QuestProgressUpdate> updates) {
        for (QuestProgressUpdate update : updates) {
            if (update.getUpdateType() != QuestProgressUpdateType.QUEST_COMPLETED) {
                continue;
            }

            QuestDefinition questDefinition = questCatalog.getById(update.getQuestId());

            List<QuestRewardDefinition> rewards =
                    rewardCatalog.getByQuestId(questDefinition.getId());

            System.out.println("Recompensas — " + questDefinition.getTitle() + ":");

            List<String> rewardSummaries = rewardSummaryFormatter.format(rewards);

            if (rewardSummaries.isEmpty()) {
                System.out.println("- Nenhuma recompensa material.");
            } else {
                for (String rewardSummary : rewardSummaries) {
                    System.out.println("- " + rewardSummary);
                }
            }

            System.out.println();
        }
    }

    private static boolean containsQuestCompletion(QuestProgressResult result) {
        for (QuestProgressUpdate update : result.getUpdates()) {
            if (update.getUpdateType() == QuestProgressUpdateType.QUEST_COMPLETED) {
                return true;
            }
        }

        return false;
    }

    public void displayQuestStarted(QuestDefinition questDefinition, QuestProgressResult result) {
        Objects.requireNonNull(questDefinition, "Quest definition cannot be null.");

        Objects.requireNonNull(result, "Quest progress result cannot be null.");

        ConsoleUI.clearScreen();

        System.out.println("QUEST INICIADA");
        System.out.println();
        System.out.println(questDefinition.getTitle());
        System.out.println();

        ConsoleUI.displayWrappedText(questDefinition.getDescription());

        System.out.println();

        displayEffects(result.getEffectDisplayTexts());

        displayFailures(result.getCompletionFailures());

        System.out.print("[Enter] Continuar");
        consoleInput.readLine();
    }

    public void displayCompletionFailures(List<QuestCompletionFailure> failures) {
        Objects.requireNonNull(failures, "Quest completion failures cannot be null.");

        if (failures.isEmpty()) {
            return;
        }

        ConsoleUI.clearScreen();

        System.out.println("QUEST BLOQUEADA");
        System.out.println();

        displayFailures(failures);

        System.out.print("[Enter] Continuar");
        consoleInput.readLine();
    }

    private static QuestStepDefinition getStep(QuestDefinition questDefinition, int step) {
        return questDefinition.getSteps().get(step - 1);
    }

    public void displayBattleProgress(QuestProgressResult result) {
        Objects.requireNonNull(result, "Quest progress result cannot be null.");

        if (!result.hasUpdates()
                && !result.hasEffectDisplayTexts()
                && !result.hasCompletionFailures()) {
            return;
        }

        boolean requiresDedicatedScreen =
                containsQuestCompletion(result)
                        || result.hasEffectDisplayTexts()
                        || result.hasCompletionFailures();

        if (requiresDedicatedScreen) {
            display(result);
            return;
        }

        System.out.println();

        displayUpdates(result.getUpdates());
    }
}
