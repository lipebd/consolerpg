package rpg.ui;

import java.util.List;
import java.util.Objects;

import rpg.classes.paths.ClassPathRules;
import rpg.classes.paths.ClassPathTransitionFailure;
import rpg.items.ItemCatalog;

// Presents expected failures that prevent the class-path ceremony.
public final class ClassPathTransitionFailurePresenter {

    private final ConsoleInput consoleInput;
    private final ItemCatalog itemCatalog;

    public ClassPathTransitionFailurePresenter(
            ConsoleInput consoleInput,
            ItemCatalog itemCatalog) {
        this.consoleInput =
                Objects.requireNonNull(
                        consoleInput,
                        "Console input cannot be null.");

        this.itemCatalog =
                Objects.requireNonNull(
                        itemCatalog,
                        "Item catalog cannot be null.");
    }

    public void display(List<ClassPathTransitionFailure> failures) {
        Objects.requireNonNull(
                failures,
                "Class path transition failures cannot be null.");

        if (failures.isEmpty()) {
            return;
        }

        ConsoleUI.clearScreen();

        System.out.println("CERIMÔNIA BLOQUEADA");
        System.out.println();

        for (ClassPathTransitionFailure failure : failures) {
            System.out.println("- " + describe(failure));
        }

        System.out.println();
        System.out.print("[Enter] Continuar");
        consoleInput.readLine();
    }

    private String describe(ClassPathTransitionFailure failure) {
        return switch (failure.getFailureType()) {
            case ALREADY_COMPLETED ->
                    "A cerimônia de mudança de classe já foi concluída.";

            case INELIGIBLE_CURRENT_CLASS ->
                    "A classe atual não pode realizar esta cerimônia.";

            case LEVEL_TOO_LOW ->
                    "É necessário alcançar o nível "
                            + ClassPathRules.CLASS_CHANGE_LEVEL
                            + " para realizar a cerimônia.";

            case ITEM_CAPACITY_EXCEEDED -> {
                String itemName =
                        itemCatalog
                                .getById(failure.getItemId().orElseThrow())
                                .getName();

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
}