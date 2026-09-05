package rpg.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import rpg.GameCurrency;
import rpg.characters.Party;
import rpg.items.Item;
import rpg.items.ItemCategory;
import rpg.items.RecoveryItem;

// Displays the general party inventory.
public final class InventoryMenu {

	private static final int ITEMS_PER_PAGE = 8;

	private final Party party;
	private final Scanner scanner;
	private final RecoveryItemUseMenu recoveryItemUseMenu;

	public InventoryMenu(Party party, Scanner scanner) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");

		this.recoveryItemUseMenu = new RecoveryItemUseMenu(party, scanner);
	}

	public boolean open() {
		List<ItemCategory> categories = List.of(ItemCategory.values());

		while (true) {
			System.out.println("\n[INVENTÁRIO]");
			System.out.println(
			GameCurrency.getName() + ": " + GameCurrency.format(party.getGold())
	);

			displayCategoryMenu(categories);

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return false;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= categories.size()) {
					System.out.println("Categoria inválida.");
					continue;
				}

				ItemCategory selectedCategory = categories.get(selectedIndex);

				boolean itemUsed = openCategory(selectedCategory);

				if (itemUsed) {
					return true;
				}

			} catch (NumberFormatException exception) {
				System.out.println("Digite o número de uma categoria.");
			}
		}
	}

	private void displayCategoryMenu(List<ItemCategory> categories) {
		System.out.println();
		System.out.println("Escolha uma categoria:");

		for (int index = 0; index < categories.size(); index++) {
			System.out.println("[" + (index + 1) + "] " + getCategoryDisplayName(categories.get(index)));
		}
		System.out.println();
		System.out.println("[0] Fechar inventário");
		System.out.print("> ");
	}

	private boolean openCategory(ItemCategory category) {
		List<Map.Entry<Item, Integer>> entries = party.getInventory().getContents().entrySet().stream()
				.filter(entry -> entry.getKey().getCategory() == category)
				.sorted(Comparator.comparing((Map.Entry<Item, Integer> entry) -> entry.getKey().getName(),
						String.CASE_INSENSITIVE_ORDER).thenComparing(entry -> entry.getKey().getId()))
				.toList();
		
		if (entries.isEmpty()) {
			openEmptyCategory(category);
			return false;
			
		}
		int currentPage = 0;
		int totalPages = (entries.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

		while (true) {
			int firstIndex = currentPage * ITEMS_PER_PAGE;

			int lastIndex = Math.min(firstIndex + ITEMS_PER_PAGE, entries.size());

			displayItemPage(category, entries, firstIndex, lastIndex, currentPage, totalPages);

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

				boolean itemUsed = openDetails(entry.getKey(), entry.getValue());

				if (itemUsed) {
					return true;
				}

			} catch (NumberFormatException exception) {
				System.out.println("Comando inválido.");
			}
		}
	}

	private void openEmptyCategory(ItemCategory category) {
		while (true) {
			System.out.println();
			System.out.println("[" + getCategoryDisplayName(category).toUpperCase(Locale.ROOT) + "]");

			System.out.println();
			System.out.println("Você não possui itens nesta categoria.");

			System.out.println();
			System.out.println("[0] Voltar às categorias");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return;
			}

			System.out.println("Opção inválida.");
		}
	}

	private void displayItemPage(ItemCategory category, List<Map.Entry<Item, Integer>> entries, int firstIndex,
			int lastIndex, int currentPage, int totalPages) {
		System.out.println();
		System.out.println("[" + getCategoryDisplayName(category).toUpperCase(Locale.ROOT) + " — PÁGINA "
				+ (currentPage + 1) + "/" + totalPages + "]");

		System.out.println();
		System.out.println("Selecione um item para ver detalhes:");

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
		System.out.println("[0] Voltar às categorias");
		System.out.print("> ");
	}

	private boolean openDetails(Item item, int quantity) {
		while (true) {
			System.out.println();
			System.out.println("[" + item.getName().toUpperCase(Locale.ROOT) + "]");

			System.out.println();
			System.out.println(item.getDescription());

			System.out.println();
			System.out.println("Categoria: " + item.getCategory());

			System.out.println("Quantidade: " + quantity);

			if (item instanceof RecoveryItem recoveryItem) {
				System.out.println("Alvo: " + recoveryItem.getTargetType());

				System.out.println("Efeitos: " + ItemMenuFormatter.getRecoveryEffectSummary(recoveryItem));

				System.out.println();
				System.out.println("[1] Usar | [0] Voltar à lista");
			} else {
				System.out.println();
				System.out.println("[0] Voltar à lista");
			}

			System.out.print("> ");

			String choice = scanner.nextLine();

			if (choice.equals("0")) {
				return false;
			}

			if (choice.equals("1") && item instanceof RecoveryItem recoveryItem) {
				return recoveryItemUseMenu.use(recoveryItem, null);
			}

			System.out.println("Opção inválida.");
		}
	}

	private String getCategoryDisplayName(ItemCategory category) {
		return switch (category) {
		case CONSUMABLE -> "Consumíveis";
		case WEAPON -> "Armas";
		case ARMOR -> "Armaduras";
		case TRINKET -> "Berloques";
		case KEY_ITEM -> "Itens-chave";
		case MATERIAL -> "Materiais";
		case SKILL_SCROLL -> "Pergaminhos";
		};
	}
}