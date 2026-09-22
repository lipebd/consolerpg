package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import rpg.classes.PlayableClassDefinition;

// Lets the player resolve a two-way or three-way questionnaire tie.
public final class ClassPathTieMenu {
	
	private static final long NARRATIVE_PAUSE_MILLISECONDS = 2000;

    private final ConsoleInput consoleInput;

    public ClassPathTieMenu(ConsoleInput consoleInput) {
        this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
    }

    public Optional<PlayableClassDefinition> select(
            List<PlayableClassDefinition> tiedClasses) {
        Objects.requireNonNull(
                tiedClasses, "Tied playable classes cannot be null.");

        List<PlayableClassDefinition> classes =
                List.copyOf(tiedClasses);

        if (classes.size() < 2 || classes.size() > 3) {
            throw new IllegalArgumentException(
                    "Class path tie must contain two or three classes.");
        }

        displayTie(classes);

        while (true) {
            System.out.print("> ");

            String choice = consoleInput.readTrimmedLine();

            if (choice.equals("0")) {
                return Optional.empty();
            }

            try {
                int selectedIndex = Integer.parseInt(choice) - 1;

                if (selectedIndex >= 0
                        && selectedIndex < classes.size()) {
                    return Optional.of(classes.get(selectedIndex));
                }

            } catch (NumberFormatException exception) {
                // Invalid input is handled through the shared feedback below.
            }

            System.out.println("Opção inválida.");
        }
    }

    private void displayTie(List<PlayableClassDefinition> classes) {
        ConsoleUI.clearScreen();

        System.out.println("[AS MARCAS]");
        System.out.println();

        if (classes.size() == 2) {
            System.out.println("Duas marcas permanecem acesas.");
        } else {
            System.out.println("Três marcas resistem às chamas.");
        }

        ConsoleUI.pause(NARRATIVE_PAUSE_MILLISECONDS);

        System.out.println();
        ConsoleUI.displayWrappedText(
                "Seis vozes atravessam a sombra em perfeita harmonia.");

        ConsoleUI.pause(NARRATIVE_PAUSE_MILLISECONDS);

        System.out.println();
        ConsoleUI.displayWrappedText(
                "Em seguida, perguntam em uma única voz:");

        ConsoleUI.pause(NARRATIVE_PAUSE_MILLISECONDS);

        System.out.println();
        ConsoleUI.displayWrappedText(
                "“Qual destas faces deseja carregar para além desta sombra?”");

        ConsoleUI.pause(NARRATIVE_PAUSE_MILLISECONDS);

        System.out.println();

        for (int index = 0; index < classes.size(); index++) {
            PlayableClassDefinition classDefinition = classes.get(index);

            System.out.println(
                    "["
                            + (index + 1)
                            + "] ["
                            + classDefinition.getSymbol()
                            + "] "
                            + classDefinition.getName());
        }

        System.out.println();
        System.out.println(
                "[0] Nenhuma delas. Forjarei meu próprio caminho");

        System.out.println();
    }

}
