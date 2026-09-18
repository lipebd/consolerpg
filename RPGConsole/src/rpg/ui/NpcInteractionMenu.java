package rpg.ui;

import java.util.List;
import java.util.Objects;

import rpg.narrative.NpcDefinition;
import rpg.narrative.NarrativeLocationDefinition;

// Displays the currently available interactions with one NPC.
public final class NpcInteractionMenu {

    private final ConsoleInput consoleInput;

    public NpcInteractionMenu(ConsoleInput consoleInput) {
        this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
    }

    public Selection selectAction(
            String cityName,
            NarrativeLocationDefinition locationDefinition,
            NpcDefinition npcDefinition,
            List<String> questTitles
    ) {
        if (cityName == null || cityName.isBlank()) {
            throw new IllegalArgumentException("City name cannot be empty.");
        }

        Objects.requireNonNull(
                locationDefinition, "Narrative location definition cannot be null.");
        Objects.requireNonNull(npcDefinition, "NPC definition cannot be null.");
        Objects.requireNonNull(questTitles, "Quest titles cannot be null.");

        while (true) {
            ConsoleUI.clearScreen();
            System.out.println("[" + cityName.trim() + "]");
            System.out.println("[" + locationDefinition.getName() + "]");
            System.out.println();
            System.out.println(npcDefinition.getName());
            System.out.println();
            ConsoleUI.displayWrappedText(
                    npcDefinition.getDescription());
            System.out.println();

            for (int index = 0; index < questTitles.size(); index++) {
                System.out.println("[" + (index + 1) + "] " + questTitles.get(index));
            }

            int talkOption = questTitles.size() + 1;

            System.out.println("[" + talkOption + "] Conversar");

            System.out.println();
            System.out.println("[0] Voltar");
            System.out.print("> ");

            String choice = consoleInput.readTrimmedLine();

            if (choice.equals("0")) {
                return Selection.back();
            }

            try {
                int selectedOption = Integer.parseInt(choice);

                if (selectedOption >= 1 && selectedOption <= questTitles.size()) {
                    return Selection.quest(selectedOption - 1);
                }

                if (selectedOption == talkOption) {
                    return Selection.talk();
                }

                System.out.println("Interação inválida.");
            } catch (NumberFormatException exception) {
                System.out.println("Digite o número de uma interação.");
            }
        }
    }

    public enum Action {
        QUEST,
        TALK,
        BACK
    }

    public record Selection(Action action, int questIndex) {
        public Selection {
            Objects.requireNonNull(action, "NPC interaction action cannot be null.");

            if (action == Action.QUEST && questIndex < 0) {
                throw new IllegalArgumentException("Quest interaction index cannot be negative.");
            }

            if (action != Action.QUEST && questIndex != -1) {
                throw new IllegalArgumentException(
                        "Non-quest interaction cannot define a quest index.");
            }
        }

        public static Selection quest(int questIndex) {
            return new Selection(Action.QUEST, questIndex);
        }

        public static Selection talk() {
            return new Selection(Action.TALK, -1);
        }

        public static Selection back() {
            return new Selection(Action.BACK, -1);
        }
    }
}
