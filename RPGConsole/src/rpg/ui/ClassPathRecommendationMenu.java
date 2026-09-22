package rpg.ui;

import java.util.Locale;
import java.util.Objects;

import rpg.classes.PlayableClassDefinition;

// Presents one resolved recommendation and allows accepting or rejecting it.
public final class ClassPathRecommendationMenu {

    private static final long NARRATIVE_PAUSE_MILLISECONDS = 2000;

    private final ConsoleInput consoleInput;

    public ClassPathRecommendationMenu(ConsoleInput consoleInput) {
        this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
    }

    public boolean accept(PlayableClassDefinition recommendedClassDefinition) {
        Objects.requireNonNull(
                recommendedClassDefinition,
                "Recommended playable class definition cannot be null.");

        displayRevelation(recommendedClassDefinition);

        String feedbackMessage = null;

        while (true) {
            ConsoleUI.clearScreen();

            System.out.println("[A MARCA ESCOLHIDA]\n");
            ConsoleUI.displayWrappedText("“Este é o seu caminho. Você ousa recusá-lo?”");
            System.out.println();

            ConsoleUI.displayWrappedText(
                    PlayableClassDetailFormatter.formatSummary(recommendedClassDefinition));

            System.out.println();
            System.out.println("[1] Descrição");

            System.out.println();
            System.out.println("[2] Abraçar este destino");
            System.out.println("[0] Recusar o chamado");

            if (feedbackMessage != null) {
                System.out.println();
                System.out.println(feedbackMessage);
            }

            System.out.println();
            System.out.print("> ");

            switch (consoleInput.readTrimmedLine()) {
                case "1" -> {
                    displayDescription(recommendedClassDefinition);
                    feedbackMessage = null;
                }

                case "2" -> {
                    return true;
                }

                case "0" -> {
                    return false;
                }

                default -> feedbackMessage = "Opção inválida.";
            }
        }
    }

    private void displayRevelation(PlayableClassDefinition classDefinition) {
        ConsoleUI.clearScreen();

        System.out.println("[A MARCA ESCOLHIDA]");
        System.out.println();

        System.out.println(
                "As outras marcas se apagam. Apenas uma resiste: ["
                        + classDefinition.getSymbol()
                        + "]");

        ConsoleUI.pause(NARRATIVE_PAUSE_MILLISECONDS);

        System.out.println();
        System.out.println("Os seis mestres então gritam em uníssono:");

        ConsoleUI.pause(NARRATIVE_PAUSE_MILLISECONDS);

        System.out.println();
        System.out.println(classDefinition.getName().toUpperCase(Locale.ROOT) + "!");

        ConsoleUI.pause(NARRATIVE_PAUSE_MILLISECONDS);

        System.out.println();
        ConsoleUI.displayWrappedText(
                "O mestre "
                        + classDefinition.getName()
                        + " se aproxima e, olhando no fundo dos seus olhos, pergunta:");

        ConsoleUI.pause(NARRATIVE_PAUSE_MILLISECONDS*2);

    }

    private void displayDescription(PlayableClassDefinition classDefinition) {
        ConsoleUI.clearScreen();

        System.out.println("[A MARCA ESCOLHIDA]");
        System.out.println();

        ConsoleUI.displayWrappedText(
                PlayableClassDetailFormatter.formatPresentation(classDefinition));

        System.out.println();
        System.out.println("[Enter] Voltar");

        consoleInput.readLine();
    }
}
