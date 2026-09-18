package rpg.ui;

import java.util.List;
import java.util.Objects;

import rpg.quests.QuestProgressEvent;
import rpg.quests.QuestProgressResult;
import rpg.quests.QuestProgressService;

// Coordinates confirmed quest events with their player-facing feedback.
public final class QuestProgressCoordinator {

    private final QuestProgressService questProgressService;
    private final QuestProgressPresenter questProgressPresenter;

    public QuestProgressCoordinator(
            QuestProgressService questProgressService,
            QuestProgressPresenter questProgressPresenter) {
        this.questProgressService =
                Objects.requireNonNull(
                        questProgressService, "Quest progress service cannot be null.");

        this.questProgressPresenter =
                Objects.requireNonNull(
                        questProgressPresenter, "Quest progress presenter cannot be null.");
    }

    public void processAndDisplay(List<QuestProgressEvent> events) {
        Objects.requireNonNull(events, "Quest progress events cannot be null.");

        QuestProgressResult result = questProgressService.processEvents(events);

        questProgressPresenter.display(result);
    }

    public boolean canRetryQuestCompletion(String questId) {
        return questProgressService.canRetryQuestCompletion(questId);
    }

    public void retryQuestCompletionAndDisplay(String questId) {
        QuestProgressResult result = questProgressService.retryQuestCompletion(questId);

        questProgressPresenter.display(result);
    }

    public void processBattleEventsAndDisplay(
            List<QuestProgressEvent> events) {
        QuestProgressResult result =
                questProgressService.processEvents(events);

        questProgressPresenter.displayBattleProgress(result);
    }
}
