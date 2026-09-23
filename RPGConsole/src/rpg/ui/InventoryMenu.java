package rpg.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import rpg.GameCurrency;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.Item;
import rpg.items.ItemCategory;
import rpg.items.RecoveryItem;
import rpg.items.SkillScroll;

// Displays the general party inventory.
public final class InventoryMenu {

	private static final int ITEMS_PER_PAGE = 8;

	private final SkillScrollUseMenu skillScrollUseMenu;
	private final EquipmentMenu equipmentMenu;
	private final Party party;
	private final ConsoleInput consoleInput;
	private final RecoveryItemUseMenu recoveryItemUseMenu;

	public InventoryMenu(Party party, ConsoleInput consoleInput, EquipmentMenu equipmentMenu) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		this.equipmentMenu = Objects.requireNonNull(equipmentMenu, "Equipment menu cannot be null.");

		this.recoveryItemUseMenu = new RecoveryItemUseMenu(party, consoleInput);

		this.skillScrollUseMenu = new SkillScrollUseMenu(party, consoleInput);
	}

	public boolean open() {
		List<ItemCategory> categories = List.of(ItemCategory.values());
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("[INVENTÁRIO]");
			System.out.println();
			System.out.println(GameCurrency.getName() + ": " + GameCurrency.format(party.getGold()));

			displayCategoryMenu(categories, feedbackMessage);

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return false;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= categories.size()) {
					feedbackMessage = "Categoria inválida.";
					continue;
				}

				feedbackMessage = null;

				ItemCategory selectedCategory = categories.get(selectedIndex);

				boolean itemUsed = openCategory(selectedCategory);

				if (itemUsed) {
					return true;
				}

			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número de uma categoria.";
			}
		}
	}

	private void displayCategoryMenu(
	        List<ItemCategory> categories,
	        String feedbackMessage
	) {
		System.out.println();
		System.out.println("Escolha uma categoria:\n");

		for (int index = 0; index < categories.size(); index++) {
			System.out.println("[" + (index + 1) + "] " + getCategoryDisplayName(categories.get(index)));
		}
		if (feedbackMessage != null) {
		    System.out.println();
		    ConsoleUI.displayWrappedText(feedbackMessage);
		}
		System.out.println();
		System.out.println("[0] Fechar inventário");
		System.out.print("> ");
	}

	private boolean openCategory(ItemCategory category) {
		List<Map.Entry<Item, Integer>> entries = getCategoryEntries(category);

		if (entries.isEmpty()) {
			openEmptyCategory(category);

			return false;
		}

		int currentPage = 0;

		String feedbackMessage = null;
		while (true) {
			int totalPages = (entries.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

			currentPage = Math.min(currentPage, totalPages - 1);

			int firstIndex = currentPage * ITEMS_PER_PAGE;

			int lastIndex = Math.min(firstIndex + ITEMS_PER_PAGE, entries.size());

			displayItemPage(
			        category,
			        entries,
			        firstIndex,
			        lastIndex,
			        currentPage,
			        totalPages,
			        feedbackMessage
			);

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return false;
			}

			if (choice.equalsIgnoreCase("P") && currentPage < totalPages - 1) {
			    feedbackMessage = null;
			    currentPage++;
			    continue;
			}

			if (choice.equalsIgnoreCase("A") && currentPage > 0) {
			    feedbackMessage = null;
			    currentPage--;
			    continue;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				int pageSize = lastIndex - firstIndex;

				if (selectedIndex < 0 || selectedIndex >= pageSize) {
					feedbackMessage = "Item inválido.";
					continue;
				}

				feedbackMessage = null;

				Map.Entry<Item, Integer> entry =
				        entries.get(firstIndex + selectedIndex);

				boolean inventoryChanged = openDetails(entry.getKey(), entry.getValue());

				if (!inventoryChanged) {
					continue;
				}

				if (category == ItemCategory.SKILL_SCROLL) {
					entries = getCategoryEntries(category);

					if (entries.isEmpty()) {
						openEmptyCategory(category);

						return false;
					}

					continue;
				}

				if (!isEquipmentCategory(category)) {
					return true;
				}

				entries = getCategoryEntries(category);

				if (entries.isEmpty()) {
					return false;
				}
			} catch (NumberFormatException exception) {
				feedbackMessage = "Comando inválido.";
			}
		}
	}

	private void openEmptyCategory(ItemCategory category) {
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("[" + getCategoryDisplayName(category).toUpperCase(Locale.ROOT) + "]");

			System.out.println();
			System.out.println("Você não possui itens nesta categoria.");
			if (feedbackMessage != null) {
			    System.out.println();
			    ConsoleUI.displayWrappedText(feedbackMessage);
			}
			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return;
			}

			feedbackMessage = "Opção inválida.";
		}
	}

	private void displayItemPage(
			ItemCategory category,
			List<Map.Entry<Item, Integer>> entries,
			int firstIndex,
			int lastIndex,
			int currentPage,
			int totalPages,
			String feedbackMessage
	) {
		ConsoleUI.clearScreen();
		System.out.println(
				"[" + getCategoryDisplayName(category).toUpperCase(Locale.ROOT) + " — PÁGINA " + (currentPage + 1) + "/"
						+ totalPages + "]"
		);

		System.out.println();
		System.out.println("Selecione um item para ver detalhes:");

		System.out.printf("%-4s %-" + ItemMenuFormatter.ITEM_NAME_WIDTH + "s %s%n", "", "Item", "Qtd.");

		for (int index = firstIndex; index < lastIndex; index++) {
			Map.Entry<Item, Integer> entry = entries.get(index);

			String displayName = ItemMenuFormatter.truncateName(entry.getKey().getName());

			int pageOption = index - firstIndex + 1;

			System.out.printf(
					"[%d]  %-" + ItemMenuFormatter.ITEM_NAME_WIDTH + "s x%d%n",
					pageOption,
					displayName,
					entry.getValue()
			);
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
		if (feedbackMessage != null) {
		    System.out.println();
		    ConsoleUI.displayWrappedText(feedbackMessage);
		}
		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private boolean openDetails(Item item, int quantity) {
		if (isEquipmentCategory(item.getCategory())) {
			return equipmentMenu.openForItem(item);
		}
		if (item instanceof SkillScroll skillScroll) {
			return openSkillScrollDetails(skillScroll);
		}
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("[" + item.getName().toUpperCase(Locale.ROOT) + "]");

			System.out.println();
			ConsoleUI.displayWrappedText(item.getDescription());

			System.out.println();
			System.out.println("Categoria: " + item.getCategory());

			System.out.println("Quantidade: " + quantity);

			if (item instanceof RecoveryItem recoveryItem) {
			    System.out.println("Alvo: " + recoveryItem.getTargetType());
			    System.out.println(
			            "Efeitos: " + ItemMenuFormatter.getRecoveryEffectSummary(recoveryItem));
			}

			if (feedbackMessage != null) {
			    System.out.println();
			    ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();

			if (item instanceof RecoveryItem) {
			    System.out.println("[1] Usar");
			}

			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readLine();

			if (choice.equals("0")) {
				return false;
			}

			if (choice.equals("1") && item instanceof RecoveryItem recoveryItem) {
				return recoveryItemUseMenu.use(recoveryItem, null);
			}

			feedbackMessage = "Opção inválida.";
		}
	}

	private boolean openSkillScrollDetails(SkillScroll skillScroll) {
		Optional<PlayableCharacter> compatibleCharacter = skillScrollUseMenu.findCompatibleCharacter(skillScroll);
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("[PERGAMINHO DE HABILIDADE]");

			System.out.println();
			System.out.println(SkillDetailFormatter.formatDetails(skillScroll.getSkillDefinition()));

			System.out.println();
			if (compatibleCharacter.isPresent()) {
			    System.out.println(
			            "Aprendiz: " + compatibleCharacter.get().getName());
			} else {
			    ConsoleUI.displayWrappedText(
			            "Nenhum membro do grupo pode aprender esta habilidade.");
			}

			if (feedbackMessage != null) {
			    System.out.println();
			    ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();

			if (compatibleCharacter.isPresent()) {
			    System.out.println("[1] Aprender");
			}

			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return false;
			}

			if (choice.equals("1") && compatibleCharacter.isPresent()) {
				return skillScrollUseMenu.use(skillScroll);
			}

			feedbackMessage = "Opção inválida.";
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

	private List<Map.Entry<Item, Integer>> getCategoryEntries(ItemCategory category) {
		return party.getInventory().getContents().entrySet().stream()
				.filter(entry -> entry.getKey().getCategory() == category)
				.sorted(
						Comparator.comparing(
								(Map.Entry<Item, Integer> entry) -> entry.getKey().getName(),
								String.CASE_INSENSITIVE_ORDER
						).thenComparing(entry -> entry.getKey().getId())
				).toList();
	}

	private boolean isEquipmentCategory(ItemCategory category) {
		return switch (category) {
		case WEAPON, ARMOR, TRINKET -> true;
		default -> false;
		};
	}

}
