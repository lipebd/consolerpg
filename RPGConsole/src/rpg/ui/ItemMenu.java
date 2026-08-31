package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Objects;
import java.util.Optional;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.HealingItem;
import rpg.items.Item;

// Controls item selection and usage through the console.
public class ItemMenu {

	private final Party party;
	private final AllyTargetMenu allyTargetMenu;
	private final Scanner scanner;

	public ItemMenu(Party party, Scanner scanner) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");

		this.allyTargetMenu = new AllyTargetMenu(scanner);
	}

	public boolean open() {
		return openMenu(null);
	}

	public boolean open(PlayableCharacter actingCharacter) {
		Objects.requireNonNull(actingCharacter, "Acting character cannot be null.");

		return openMenu(actingCharacter);
	}

	private boolean openMenu(PlayableCharacter actingCharacter) {
		Map<Item, Integer> inventoryContents = party.getInventory().getContents();

		System.out.println("\n[INVENTÁRIO]");
		System.out.println("Ouro: " + party.getGold());

		System.out.println("\nItens:");
		if (inventoryContents.isEmpty()) {
			System.out.println("Seu inventário está vazio.");
			return false;
		}

		List<Map.Entry<Item, Integer>> itemEntries = new ArrayList<>(inventoryContents.entrySet());

		while (true) {

			for (int index = 0; index < itemEntries.size(); index++) {

				Map.Entry<Item, Integer> entry = itemEntries.get(index);

				Item item = entry.getKey();
				int quantity = entry.getValue();

				System.out.print("[" + (index + 1) + "] " + item.getName() + " x" + quantity);

				if (item instanceof HealingItem healingItem) {
					System.out.print(" | Cura: " + healingItem.getHealingAmount() + " HP");
				}

				System.out.println();
			}

			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine();

			if (choice.equals("0")) {
				return false;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= itemEntries.size()) {
					System.out.println("Item inválido.");
					continue;
				}

				Item selectedItem = itemEntries.get(selectedIndex).getKey();

				if (!confirmItemUse(selectedItem)) {
					continue;
				}

				if (selectedItem instanceof HealingItem healingItem) {
					Optional<PlayableCharacter> selectedTarget = allyTargetMenu.selectInjuredTarget(party);;

					if (selectedTarget.isEmpty()) {
						continue;
					}

					PlayableCharacter target = selectedTarget.get();

					if (target.getCurrentHp() == target.getMaxHp()) {
						System.out.println(target.getName() + " já está com o HP cheio.");

						continue;
					}

					int recoveredHp = healingItem.use(target);

					if (recoveredHp <= 0) {
						System.out.println("O item não produziu nenhum efeito.");

						continue;
					}

					party.getInventory().removeItem(selectedItem, 1);

					displayItemUse(actingCharacter, target, selectedItem);

					displayHealing(target, recoveredHp);

					return true;
				}

				System.out.println("Este item não pode ser usado agora.");

			} catch (NumberFormatException exception) {
				System.out.println("Digite o número de um item.");
			}
		}
	}

	private boolean confirmItemUse(Item item) {
		System.out.println("\n[" + item.getName().toUpperCase() + "]");

		System.out.println(item.getDescription());

		while (true) {
			System.out.println("\n[1] Usar | [0] Voltar");
			System.out.print("> ");

			String confirmation = scanner.nextLine();

			switch (confirmation) {
			case "1" -> {
				return true;
			}

			case "0" -> {
				return false;
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private void displayHealing(PlayableCharacter target, int recoveredHp) {
		System.out.println(target.getName() + " recuperou " + recoveredHp + " de HP. HP atual: " + target.getCurrentHp()
				+ "/" + target.getMaxHp() + ".");
	}

	private void displayItemUse(PlayableCharacter actingCharacter, PlayableCharacter target, Item item) {
		if (actingCharacter == null || actingCharacter == target) {

			System.out.println(target.getName() + " usou " + item.getName() + ".");

			return;
		}

		System.out.println(actingCharacter.getName() + " usou " + item.getName() + " em " + target.getName() + ".");
	}
}