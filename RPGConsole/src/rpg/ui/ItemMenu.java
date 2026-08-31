package rpg.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Objects;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.HealingItem;
import rpg.items.Item;

// Controls item selection and usage through the console.
public class ItemMenu {

	private final Party party;
	private final PlayableCharacter targetCharacter;
	private final Scanner scanner;

	public ItemMenu(Party party, Scanner scanner) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");

		/*
		 * Temporary target while ally selection is not implemented.
		 */
		this.targetCharacter = party.getLeader();
	}

	public boolean open() {
		Map<Item, Integer> inventoryContents = party.getInventory().getContents();

		if (inventoryContents.isEmpty()) {
			System.out.println("Seu inventário está vazio.");
			return false;
		}

		List<Map.Entry<Item, Integer>> itemEntries = new ArrayList<>(inventoryContents.entrySet());

		while (true) {
			System.out.println("\nEscolha um item:");

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
					if (targetCharacter.getCurrentHp() == targetCharacter.getMaxHp()) {
						System.out.println(targetCharacter.getName() + " já está com o HP cheio.");

						continue;
					}

					int recoveredHp = healingItem.use(targetCharacter);

					if (recoveredHp <= 0) {
						System.out.println("O item não produziu nenhum efeito.");

						continue;
					}

					party.getInventory().removeItem(selectedItem, 1);

					displayHealing(recoveredHp);

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

	private void displayHealing(int recoveredHp) {
		System.out.println(targetCharacter.getName() + " recuperou " + recoveredHp + " de HP. HP atual: "
				+ targetCharacter.getCurrentHp() + "/" + targetCharacter.getMaxHp() + ".");
	}
}