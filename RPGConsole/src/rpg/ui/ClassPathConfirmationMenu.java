package rpg.ui;

import java.util.Objects;

import rpg.classes.PlayableClassDefinition;

// Confirms one final class path without allowing ceremony cancellation.
public final class ClassPathConfirmationMenu {

    private final ConsoleInput consoleInput;

    public ClassPathConfirmationMenu(ConsoleInput consoleInput) {
        this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
    }

    public boolean confirm(PlayableClassDefinition classDefinition) {
        Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.");

        String feedbackMessage = null;

        while (true) {
            ConsoleUI.clearScreen();

            ConsoleUI.displayHeading("Escolha do Caminho");
            System.out.println();

            ConsoleUI.displayWrappedText(
                    PlayableClassDetailFormatter.formatPresentation(classDefinition));

            System.out.println();
            System.out.println("[1] Confirmar");
            System.out.println("[2] Escolher novamente");

            if (feedbackMessage != null) {
                System.out.println();
                ConsoleUI.displayWrappedText(feedbackMessage);
            }

            System.out.println();
            System.out.print("> ");

            switch (consoleInput.readTrimmedLine()) {
                case "1" -> {
                    return true;
                }

                case "2" -> {
                    return false;
                }

                default -> feedbackMessage = "Opção inválida.";
            }
        }
    }
}
