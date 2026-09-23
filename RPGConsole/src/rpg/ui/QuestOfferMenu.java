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

	public Selection select(String cityName, String locationName, QuestDefinition questDefinition) {
		Objects.requireNonNull(questDefinition, "Quest definition cannot be null.");

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(cityName, locationName);

			System.out.println();
			System.out.println(questDefinition.getTitle());

			System.out.println();
			ConsoleUI.displayWrappedText(questDefinition.getDescription());

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println("[1] Aceitar");
			System.out.println("[2] Recusar");
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

    public enum Selection {
        ACCEPT,
        DECLINE
    }
}
