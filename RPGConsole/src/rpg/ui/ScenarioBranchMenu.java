package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import rpg.world.branches.ScenarioBranchDefinition;

// Displays exploration branches and returns the selected branch.
public final class ScenarioBranchMenu {

        private final ConsoleInput consoleInput;

        public ScenarioBranchMenu(ConsoleInput consoleInput) {
                this.consoleInput = Objects.requireNonNull(
                                consoleInput,
                                "Console input cannot be null."
                );
        }

        public Optional<ScenarioBranchDefinition> open(
                        List<ScenarioBranchDefinition> branches) {
                Objects.requireNonNull(
                                branches,
                                "Scenario branches cannot be null."
                );

                if (branches.isEmpty()) {
                        System.out.println(
                                        "\nNão há outros caminhos disponíveis."
                        );

                        return Optional.empty();
                }

                while (true) {
                        System.out.println(
                                        "\nEscolha o caminho que deseja seguir:"
                        );

                        for (int index = 0; index < branches.size(); index++) {
                                ScenarioBranchDefinition branch =
                                                branches.get(index);

                                System.out.println(
                                                "[" + (index + 1) + "] "
                                                                + branch.getDisplayName()
                                );

                                System.out.println(
                                                "    "
                                                                + branch.getDescription()
                                                                                .replace("\n", "\n    ")
                                );
                        }

                        System.out.println("[0] Voltar");
                        System.out.print("> ");

                        String choice = consoleInput.readLine();

                        if (choice.equals("0")) {
                                return Optional.empty();
                        }

                        try {
                                int selectedIndex =
                                                Integer.parseInt(choice) - 1;

                                if (selectedIndex < 0
                                                || selectedIndex >= branches.size()) {
                                        System.out.println("Caminho inválido.");
                                        continue;
                                }

                                return Optional.of(
                                                branches.get(selectedIndex)
                                );

                        } catch (NumberFormatException exception) {
                                System.out.println(
                                                "Digite o número de um caminho."
                                );
                        }
                }
        }
}