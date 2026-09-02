package rpg.ui;

import java.util.Comparator;
import java.util.Locale;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.Item;
import rpg.items.RecoveryItem;

// Displays and uses items available during battle.
public final class BattleItemMenu {

	private static final int ITEMS_PER_PAGE = 8;

	private final Party party;
	private final Scanner scanner;
	private final RecoveryItemUseMenu recoveryItemUseMenu;

	public BattleItemMenu(Party party, Scanner scanner) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");

		this.recoveryItemUseMenu = new RecoveryItemUseMenu(party, scanner);
	}

	public boolean open(PlayableCharacter actingCharacter) {
		Objects.requireNonNull(actingCharacter, "Acting character cannot be null.");

		List<Map.Entry<Item, Integer>> availableEntries = party.getInventory().getContents().entrySet().stream()
				.filter(entry -> entry.getKey() instanceof RecoveryItem)
				.sorted(Comparator.comparing((Map.Entry<Item, Integer> entry) -> entry.getKey().getName(),
						String.CASE_INSENSITIVE_ORDER).thenComparing(entry -> entry.getKey().getId()))
				.toList();

		System.out.println("\n[ITENS DE BATALHA]");

		if (availableEntries.isEmpty()) {
			System.out.println("Não há itens disponíveis para usar em batalha.");

			return false;
		}

		return openItemList(availableEntries, actingCharacter);
	}

	private boolean openItemList(List<Map.Entry<Item, Integer>> entries, PlayableCharacter actingCharacter) {
		int currentPage = 0;
		int totalPages = (entries.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

		while (true) {
			int firstIndex = currentPage * ITEMS_PER_PAGE;

			int lastIndex = Math.min(firstIndex + ITEMS_PER_PAGE, entries.size());

			displayPage(entries, firstIndex, lastIndex, currentPage, totalPages);

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return false;
			}

			if (choice.equalsIgnoreCase("P") && currentPage < totalPages - 1) {
				currentPage++;
				continue;
			}

			if (choice.equalsIgnoreCase("A") && currentPage > 0) {
				currentPage--;
				continue;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				int pageSize = lastIndex - firstIndex;

				if (selectedIndex < 0 || selectedIndex >= pageSize) {
					System.out.println("Item inválido.");

					continue;
				}

				Map.Entry<Item, Integer> entry = entries.get(firstIndex + selectedIndex);

				RecoveryItem recoveryItem = (RecoveryItem) entry.getKey();

				boolean itemUsed = openDetails(recoveryItem, entry.getValue(), actingCharacter);

				if (itemUsed) {
					return true;
				}

			} catch (NumberFormatException exception) {
				System.out.println("Comando inválido.");
			}
		}
	}

	private void displayPage(List<Map.Entry<Item, Integer>> entries, int firstIndex, int lastIndex, int currentPage,
			int totalPages) {
		System.out.println("\n[PÁGINA " + (currentPage + 1) + "/" + totalPages + "]");

		System.out.printf("%-4s %-" + ItemMenuFormatter.ITEM_NAME_WIDTH + "s %s%n", "", "Item", "Qtd.");

		for (int index = firstIndex; index < lastIndex; index++) {
			Map.Entry<Item, Integer> entry = entries.get(index);

			String displayName = ItemMenuFormatter.truncateName(entry.getKey().getName());

			int pageOption = index - firstIndex + 1;

			System.out.printf("[%d]  %-" + ItemMenuFormatter.ITEM_NAME_WIDTH + "s x%d%n", pageOption, displayName,
					entry.getValue());
		}

		int displayedItemCount = lastIndex - firstIndex;

		for (int emptySlot = displayedItemCount; emptySlot < ITEMS_PER_PAGE; emptySlot++) {
			System.out.printf("     %-" + ItemMenuFormatter.ITEM_NAME_WIDTH + "s %s%n", "--", "--");
		}

		if (currentPage > 0) {
			System.out.println("[A] Página anterior");
		}

		if (currentPage < totalPages - 1) {
			System.out.println("[P] Próxima página");
		}
		System.out.println();
		System.out.println("[0] Voltar à batalha");
		System.out.print("> ");
	}

	private boolean openDetails(RecoveryItem recoveryItem, int quantity, PlayableCharacter actingCharacter) {
		while (true) {
			System.out.println("\n[" + recoveryItem.getName().toUpperCase(Locale.ROOT) + "]");

			System.out.println();
			System.out.println(recoveryItem.getDescription());

			System.out.println();
			System.out.println("Categoria: " + recoveryItem.getCategory());

			System.out.println("Quantidade: " + quantity);

			System.out.println("Alvo: " + recoveryItem.getTargetType());

			System.out.println("Efeitos: " + ItemMenuFormatter.getRecoveryEffectSummary(recoveryItem));

			System.out.println();
			System.out.println("[1] Usar | [0] Voltar à lista");

			System.out.print("> ");

			String choice = scanner.nextLine();

			switch (choice) {
			case "1" -> {
				return recoveryItemUseMenu.use(recoveryItem, actingCharacter);
			}

			case "0" -> {
				return false;
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}
}