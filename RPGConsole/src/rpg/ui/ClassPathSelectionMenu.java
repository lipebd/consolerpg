package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassDefinition;
import rpg.classes.paths.ClassPathCatalog;
import rpg.classes.paths.ClassPathDefinition;

// Allows selection among every final playable-class path.
public final class ClassPathSelectionMenu {

    private final ConsoleInput consoleInput;
    private final List<PlayableClassDefinition> selectableClasses;

    public ClassPathSelectionMenu(
            ConsoleInput consoleInput,
            ClassPathCatalog classPathCatalog,
            PlayableClassCatalog playableClassCatalog) {
        this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");

        Objects.requireNonNull(classPathCatalog, "Class path catalog cannot be null.");
        Objects.requireNonNull(playableClassCatalog, "Playable class catalog cannot be null.");

        List<PlayableClassDefinition> classDefinitions = new ArrayList<>();

        for (ClassPathDefinition pathDefinition : classPathCatalog.getAll()) {
            classDefinitions.add(playableClassCatalog.getById(pathDefinition.getClassId()));
        }

        if (classDefinitions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Class path selection requires at least one final class.");
        }

        this.selectableClasses = List.copyOf(classDefinitions);
    }

    public PlayableClassDefinition select() {
        String feedbackMessage = null;

        while (true) {
            ConsoleUI.clearScreen();

            System.out.println("[SETE CAMINHOS]");
            System.out.println();
            System.out.println("Escolha um caminho para conhecer:");

            System.out.println();

            for (int index = 0; index < selectableClasses.size(); index++) {
                PlayableClassDefinition classDefinition = selectableClasses.get(index);

                System.out.println(
                        "["
                                + (index + 1)
                                + "] ["
                                + classDefinition.getSymbol()
                                + "] "
                                + classDefinition.getName());
            }

            if (feedbackMessage != null) {
                System.out.println();
                System.out.println(feedbackMessage);
            }

            System.out.println();
            System.out.print("> ");

            String choice = consoleInput.readTrimmedLine();

            try {
                int selectedIndex = Integer.parseInt(choice) - 1;

                if (selectedIndex >= 0 && selectedIndex < selectableClasses.size()) {
                    return selectableClasses.get(selectedIndex);
                }

            } catch (NumberFormatException exception) {
                // Invalid input is handled through the shared feedback below.
            }

            feedbackMessage = "Opção inválida.";
        }
    }
}
