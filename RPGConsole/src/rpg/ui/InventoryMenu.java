package rpg.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import rpg.GameCurrency;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.Item;
import rpg.items.ItemCategory;
import rpg.items.RecoveryItem;
import rpg.items.RewardModifierItem;
import rpg.items.SkillScroll;

// Displays the general party inventory.
public final class InventoryMenu {

	private static final int ITEMS_PER_PAGE = 9;

	private final AllyTargetMenu allyTargetMenu;
	private final SkillScrollUseMenu skillScrollUseMenu;
	private final EquipmentMenu equipmentMenu;
	private final Party party;
	private final ConsoleInput consoleInput;

	public InventoryMenu(Party party, ConsoleInput consoleInput, EquipmentMenu equipmentMenu) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		this.equipmentMenu = Objects.requireNonNull(equipmentMenu, "Equipment menu cannot be null.");

		this.skillScrollUseMenu = new SkillScrollUseMenu(party, consoleInput);

		this.allyTargetMenu = new AllyTargetMenu(consoleInput);
	}

	public boolean open() {
		List<ItemCategory> categories = List.of(ItemCategory.values());
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Inventário");
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

	private void displayCategoryMenu(List<ItemCategory> categories, String feedbackMessage) {
		System.out.println();
		System.out.println("Escolha uma categoria:\n");

		for (int index = 0; index < categories.size(); index++) {
			System.out.println("[" + (index + 1) + "] " + getCategoryDisplayName(categories.get(index)));
		}
		System.out.println();
		System.out.println(feedbackMessage == null ? "" : feedbackMessage);
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

			displayItemPage(category, entries, firstIndex, lastIndex, currentPage, totalPages, feedbackMessage);

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

				Map.Entry<Item, Integer> entry = entries.get(firstIndex + selectedIndex);

				boolean inventoryChanged = openDetails(entry.getKey(), entry.getValue());

				if (!inventoryChanged) {
					continue;
				}

				if (category == ItemCategory.SKILL_SCROLL || category == ItemCategory.CONSUMABLE) {
					entries = getCategoryEntries(category);

					if (entries.isEmpty()) {
						if (category == ItemCategory.SKILL_SCROLL) {
							openEmptyCategory(category);
						}

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
			ConsoleUI.displayHeading(getCategoryDisplayName(category));

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
		ConsoleUI
				.displayHeading(getCategoryDisplayName(category) + " - Página " + (currentPage + 1) + "/" + totalPages);

		System.out.println();
		System.out.println("Selecione um item:");
		System.out.println(ItemMenuFormatter.formatItemTableHeader());

		for (int index = firstIndex; index < lastIndex; index++) {
			Map.Entry<Item, Integer> entry = entries.get(index);

			int pageOption = index - firstIndex + 1;

			System.out.println(
					ItemMenuFormatter.formatItemTableRow(pageOption, entry.getKey().getName(), entry.getValue())
			);
		}

		int displayedItemCount = lastIndex - firstIndex;

		for (int emptySlot = displayedItemCount; emptySlot < ITEMS_PER_PAGE; emptySlot++) {
			System.out.println(ItemMenuFormatter.formatEmptyItemTableRow());
		}
		StringBuilder navigation = new StringBuilder("");
		if (currentPage > 0) {
			navigation.append("    [A] Anterior");
		}
		if (currentPage < totalPages - 1) {
			navigation.append("    [P] Próxima");
		}
		System.out.println(navigation);
		System.out.println(feedbackMessage == null ? "" : feedbackMessage);
		System.out.println("[0] Fechar inventário");
		System.out.print("> ");
	}

	private boolean openDetails(Item item, int quantity) {
		if (isEquipmentCategory(item.getCategory())) {
			return equipmentMenu.openForItem(item);
		}

		if (item instanceof SkillScroll skillScroll) {
			return openSkillScrollDetails(skillScroll);
		}

		List<String> descriptionLines = ConsoleUI.wrapText(item.getDescription());

		int currentPage = 0;
		String feedbackMessage = null;
		boolean confirmingUse = false;
		boolean itemUsed = false;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Inventário", "Itens");

			System.out.println();
			System.out.println(item.getName());
			System.out.println("Categoria: " + item.getCategory().getDisplayName());

			if (item instanceof RecoveryItem recoveryItem) {
				System.out.println("Alvo: " + recoveryItem.getTargetType().getDisplayName());
				System.out.println("Efeitos:");
				ConsoleUI.displayWrappedText(ItemMenuFormatter.getRecoveryEffectSummary(recoveryItem));
			} else if (item instanceof RewardModifierItem rewardItem) {
				System.out.println("Alvo: Grupo");
				System.out.println("Efeitos:");
				ConsoleUI.displayWrappedText(ItemMenuFormatter.getRewardModifierEffectSummary(rewardItem));
			} else {
				System.out.println();
				System.out.println();
				System.out.println();
			}

			System.out.println();

			int firstLine = currentPage * 3;
			int lastLine = Math.min(firstLine + 3, descriptionLines.size());

			for (int index = firstLine; index < lastLine; index++) {
				System.out.println(descriptionLines.get(index));
			}

			for (int index = lastLine - firstLine; index < 3; index++) {
				System.out.println();
			}

			System.out.println();
			System.out.println();
			System.out.println("Quantidade: " + party.getInventory().getQuantity(item));
			System.out.println(feedbackMessage == null ? "" : feedbackMessage);

			StringBuilder options = new StringBuilder();

			if (!itemUsed) {
				if (item instanceof RewardModifierItem) {
					options.append(confirmingUse ? "[1] Confirmar    [0] Cancelar" : "[1] Usar    [0] Voltar");
				} else {
					if (item instanceof RecoveryItem) {
						options.append("[1] Usar    ");
					}

					options.append("[0] Voltar");
				}

				if (!confirmingUse && currentPage > 0) {
					options.append("    [A] Anterior");
				}

				if (!confirmingUse && lastLine < descriptionLines.size()) {
					options.append("    [P] Próxima");
				}
			}

			System.out.println(options);

			if (itemUsed) {
				ConsoleUI.pause(2000L, consoleInput);
				return true;
			}

			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();
			feedbackMessage = null;

			if (choice.equals("0")) {
				if (confirmingUse) {
					confirmingUse = false;
					continue;
				}

				return false;
			}

			if (choice.equals("1") && item instanceof RewardModifierItem rewardItem) {
				if (!confirmingUse) {
					confirmingUse = true;
					continue;
				}

				if (rewardItem.use(party)) {
					feedbackMessage = "Bônus ativado por " + rewardItem.getRewardedVictories()
							+ " vitórias recompensadas.";
					itemUsed = true;
					continue;
				}

				boolean experienceBlocked = rewardItem.getExperiencePercent() > 0
						&& party.getRewardModifierState().hasActiveExperienceModifier();

				boolean goldBlocked = rewardItem.getGoldPercent() > 0
						&& party.getRewardModifierState().hasActiveGoldModifier();

				if (experienceBlocked && goldBlocked) {
					feedbackMessage = "Já há bônus ativos de EXP e GOLD.";
				} else if (experienceBlocked) {
					feedbackMessage = "Já há um bônus de EXP ativo.";
				} else if (goldBlocked) {
					feedbackMessage = "Já há um bônus de GOLD ativo.";
				} else {
					feedbackMessage = "O item não pôde ser usado.";
				}

				confirmingUse = false;
				continue;
			}

			if (choice.equals("1") && item instanceof RecoveryItem recoveryItem) {
				List<String> messages = new ArrayList<>();
				RecoveryItemUseMenu useMenu = new RecoveryItemUseMenu(party, consoleInput, messages::add);

				boolean recoveryUsed = useMenu.use(recoveryItem, null);

				if (recoveryUsed) {
					allyTargetMenu.displayRecoveryResult(party, recoveryItem, messages);
					return true;
				}

				if (!messages.isEmpty()) {
					feedbackMessage = messages.get(0);
				}

				continue;
			}

			if (!confirmingUse && choice.equalsIgnoreCase("A") && currentPage > 0) {
				currentPage--;
				continue;
			}

			if (!confirmingUse && choice.equalsIgnoreCase("P") && lastLine < descriptionLines.size()) {
				currentPage++;
				continue;
			}

			feedbackMessage = "Opção inválida.";
		}
	}

	private boolean openSkillScrollDetails(SkillScroll skillScroll) {
		Optional<PlayableCharacter> compatibleCharacter = skillScrollUseMenu.findCompatibleCharacter(skillScroll);

		List<String> detailLines = ConsoleUI.wrapText(
				SkillDetailFormatter.formatDetails(skillScroll.getSkillDefinition()).replace("\r\n", "\n")
						.replace('\r', '\n')
		);

		int currentPage = 0;
		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Pergaminho de habilidade");
			System.out.println();

			int firstLine = currentPage * 9;
			int lastLine = Math.min(firstLine + 9, detailLines.size());

			for (int index = firstLine; index < lastLine; index++) {
				System.out.println(detailLines.get(index));
			}

			for (int index = lastLine - firstLine; index < 9; index++) {
				System.out.println();
			}

			System.out.println();

			System.out.println(
					compatibleCharacter.map(character -> "Aprendiz: " + character.getName())
							.orElse("Nenhum membro do grupo pode aprender esta habilidade.")
			);

			System.out.println(feedbackMessage == null ? "" : feedbackMessage);
			System.out.println(compatibleCharacter.isPresent() ? "[1] Aprender" : "");

			StringBuilder options = new StringBuilder("[0] Voltar");

			if (currentPage > 0) {
				options.append("    [A] Anterior");
			}

			if (lastLine < detailLines.size()) {
				options.append("    [P] Próxima");
			}

			System.out.println(options);
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();
			feedbackMessage = null;

			if (choice.equals("0")) {
				return false;
			}

			if (choice.equals("1") && compatibleCharacter.isPresent()) {
				return skillScrollUseMenu.use(skillScroll);
			}

			if (choice.equalsIgnoreCase("A") && currentPage > 0) {
				currentPage--;
				continue;
			}

			if (choice.equalsIgnoreCase("P") && lastLine < detailLines.size()) {
				currentPage++;
				continue;
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
