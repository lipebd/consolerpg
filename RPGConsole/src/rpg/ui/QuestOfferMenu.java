package rpg.ui;

import java.util.List;
import java.util.Objects;

import rpg.quests.QuestDefinition;

// Presents the accept-or-decline choice for one offered quest.
public final class QuestOfferMenu {

    private final ConsoleInput consoleInput;

    public QuestOfferMenu(ConsoleInput consoleInput) {
        this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
    }

    public Selection select(List<String> headerLines, QuestDefinition questDefinition) {
        Objects.requireNonNull(headerLines, "Quest offer header lines cannot be null.");

        Objects.requireNonNull(questDefinition, "Quest definition cannot be null.");
        String feedbackMessage = null;
        while (true) {
            ConsoleUI.clearScreen();

            displayHeaders(headerLines);

            System.out.println(questDefinition.getTitle());
            System.out.println();

            ConsoleUI.displayWrappedText(questDefinition.getDescription());

            System.out.println();
            System.out.println("[1] Aceitar");
            System.out.println("[2] Recusar");
            if (feedbackMessage != null) {
                System.out.println();
                ConsoleUI.displayWrappedText(feedbackMessage);
            }

            System.out.print("> ");

            String choice = consoleInput.readTrimmedLine();

            switch (choice) {
                case "1" -> {
                    return Selection.ACCEPT;
                }

                case "2" -> {
                    return Selection.DECLINE;
                }

                default -> feedbackMessage = "Escolha inválida.";
            }
        }
    }

    private static void displayHeaders(List<String> headerLines) {
        for (String headerLine : headerLines) {
            if (headerLine == null || headerLine.isBlank()) {
                throw new IllegalArgumentException("Quest offer header line cannot be empty.");
            }

            System.out.println("[" + headerLine.trim().toUpperCase() + "]");
        }

        if (!headerLines.isEmpty()) {
            System.out.println();
        }
    }

    public enum Selection {
        ACCEPT,
        DECLINE
    }
}
