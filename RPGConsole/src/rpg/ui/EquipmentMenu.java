package rpg.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import rpg.characters.CharacterEquipment;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.items.ItemCategory;
import rpg.items.equipment.EquipmentCatalog;
import rpg.items.equipment.EquipmentDefinition;
import rpg.items.equipment.EquipmentService;

// Allows equipment management through items or playable characters.
public final class EquipmentMenu {

	private static final int ITEMS_PER_PAGE = 8;

	private final Party party;
	private final ConsoleInput consoleInput;
	private final EquipmentCatalog equipmentCatalog;
	private final ItemCatalog itemCatalog;
	private final EquipmentService equipmentService;

	public EquipmentMenu(
			Party party,
			ConsoleInput consoleInput,
			EquipmentCatalog equipmentCatalog,
			ItemCatalog itemCatalog,
			EquipmentService equipmentService
	) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");

		this.equipmentCatalog = Objects.requireNonNull(equipmentCatalog, "Equipment catalog cannot be null.");

		this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");

		this.equipmentService = Objects.requireNonNull(equipmentService, "Equipment service cannot be null.");
	}

	public void openForCharacter(PlayableCharacter character) {
		Objects.requireNonNull(character, "Playable character cannot be null.");
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			displayCharacterEquipment(character, feedbackMessage);

			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "0" -> {
				return;
			}

			case "1" -> {
			    feedbackMessage = null;
			    openSlot(character, ItemCategory.WEAPON);
			}

			case "2" -> {
			    feedbackMessage = null;
			    openSlot(character, ItemCategory.ARMOR);
			}

			case "3" -> {
			    feedbackMessage = null;
			    openSlot(character, ItemCategory.TRINKET);
			}

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	public boolean openForItem(Item item) {
		Objects.requireNonNull(item, "Item cannot be null.");

		EquipmentDefinition definition = equipmentCatalog.getByItemId(item.getId());
		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Equipamento", getSlotName(definition.getItemCategory()));

			System.out.println();
			System.out.println(item.getName() + ", Nv. " + definition.getLevelRequirement());
			ConsoleUI.displayWrappedText(item.getDescription());

			System.out.println();
			System.out.println(EquipmentDetailFormatter.formatDetails(item, definition));

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println("[1] Equipar");

			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return false;
			}

			if (!choice.equals("1")) {
				feedbackMessage = "Opção inválida.";
				continue;
			}

			feedbackMessage = null;

			Optional<PlayableCharacter> selectedCharacter = selectCharacterForItem(item, definition);

			if (selectedCharacter.isEmpty()) {
				continue;
			}

			if (confirmEquipment(selectedCharacter.get(), item, definition)) {
				return true;
			}
		}
	}

	private void displayCharacterEquipment(PlayableCharacter character, String feedbackMessage) {
		CharacterEquipment equipment = character.getEquipment();

		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading("Equipamentos");

		System.out.println();
		System.out.println("Personagem: " + character.getName());

		System.out.println();
		System.out.println("[1] Arma:      " + getEquippedItemName(equipment.getWeapon().orElse(null)));
		System.out.println("[2] Armadura:  " + getEquippedItemName(equipment.getArmor().orElse(null)));
		System.out.println("[3] Berloque:  " + getEquippedItemName(equipment.getTrinket().orElse(null)));
		if (feedbackMessage != null) {
		    System.out.println();
		    ConsoleUI.displayWrappedText(feedbackMessage);
		}
		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private String getEquippedItemName(EquipmentDefinition definition) {
		if (definition == null) {
			return "--";
		}

		return itemCatalog.getById(definition.getItemId()).getName();
	}

	private void openSlot(PlayableCharacter character, ItemCategory equipmentCategory) {
		String feedbackMessage = null;
		while (true) {
			ConsoleUI.clearScreen();
			EquipmentDefinition equippedDefinition = getEquippedDefinition(character.getEquipment(), equipmentCategory);

			displaySlotDetails(character, equipmentCategory, equippedDefinition);

			System.out.println();

			if (equippedDefinition == null) {
			    System.out.println("[1] Equipar");
			} else {
			    System.out.println("[1] Trocar");
			    System.out.println("[2] Desequipar");
			}

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

			if (choice.equals("1")) {
			    feedbackMessage = null;

			    Optional<Item> selectedItem =
			            selectItemForCharacter(character, equipmentCategory);

			    if (selectedItem.isEmpty()) {
			        continue;
			    }

			    EquipmentDefinition selectedDefinition =
			            equipmentCatalog.getByItemId(
			                    selectedItem.get().getId());

			    confirmEquipment(
			            character,
			            selectedItem.get(),
			            selectedDefinition);

			    continue;
			}

			if (choice.equals("2") && equippedDefinition != null) {
			    feedbackMessage = null;
			    confirmUnequip(character, equipmentCategory);
			    continue;
			}

			feedbackMessage = "Opção inválida.";
		}
	}

	private void displaySlotDetails(
			PlayableCharacter character,
			ItemCategory equipmentCategory,
			EquipmentDefinition definition
	) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(
				"Equipamento",
				getSlotName(equipmentCategory)
		);

		if (definition == null) {
			System.out.println();
			System.out.println("Nenhum item equipado em " + character.getName() + ".");
			return;
		}

		Item item = itemCatalog.getById(definition.getItemId());

		System.out.println();
		System.out.println(
				item.getName()
						+ ", Nv. "
						+ definition.getLevelRequirement()
		);
		ConsoleUI.displayWrappedText(item.getDescription());

		System.out.println();
		System.out.println(EquipmentDetailFormatter.formatDetails(item, definition));

		System.out.println();
		System.out.println("Equipado em: " + character.getName());
	}

	private Optional<Item> selectItemForCharacter(PlayableCharacter character, ItemCategory equipmentCategory) {
		List<Item> availableItems = party.getInventory().getContents().entrySet().stream()
				.filter(entry -> entry.getValue() > 0).map(Map.Entry::getKey)
				.filter(item -> item.getCategory() == equipmentCategory).filter(item -> {
					EquipmentDefinition definition = equipmentCatalog.getByItemId(item.getId());

					return character.canEquip(definition) && !isCurrentlyEquipped(character, definition);
				})
				.sorted(
						Comparator.comparingInt(
								(Item item) -> equipmentCatalog.getByItemId(item.getId()).getLevelRequirement()
						).thenComparing(Item::getName, String.CASE_INSENSITIVE_ORDER)
				).toList();

		if (availableItems.isEmpty()) {
			displayMessageAndWaitForBack(
					equipmentCategory,
					"Não há "
							+ getSlotName(equipmentCategory).toLowerCase(Locale.ROOT)
							+ "s compatíveis."
			);

		    return Optional.empty();
		}

		return selectItemFromPages(character, equipmentCategory, availableItems);
	}

	private Optional<Item> selectItemFromPages(
			PlayableCharacter character,
			ItemCategory equipmentCategory,
			List<Item> availableItems
	) {
		int currentPage = 0;
		String feedbackMessage = null;
		int totalPages = (availableItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
		while (true) {
			int firstIndex = currentPage * ITEMS_PER_PAGE;

			int lastIndex = Math.min(firstIndex + ITEMS_PER_PAGE, availableItems.size());

			displayItemSelectionPage(
					character,
					equipmentCategory,
					availableItems,
					firstIndex,
					lastIndex,
					currentPage,
					totalPages,
					feedbackMessage
			);

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}
			if (choice.equalsIgnoreCase("P") && currentPage < totalPages - 1) {
				currentPage++;
				feedbackMessage = null;
				continue;
			}

			if (choice.equalsIgnoreCase("A") && currentPage > 0) {
				currentPage--;
				feedbackMessage = null;
				continue;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;
				int pageSize = lastIndex - firstIndex;

				if (selectedIndex < 0 || selectedIndex >= pageSize) {
					feedbackMessage = "Equipamento inválido.";
					continue;
				}

				return Optional.of(availableItems.get(firstIndex + selectedIndex));
			} catch (NumberFormatException exception) {
				feedbackMessage = "Comando inválido.";
			}
		}
	}

	private void displayItemSelectionPage(
			PlayableCharacter character,
			ItemCategory equipmentCategory,
			List<Item> availableItems,
			int firstIndex,
			int lastIndex,
			int currentPage,
			int totalPages,
			String feedbackMessage
	) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading("Equipamento", getSlotName(equipmentCategory));

		System.out.println();
		System.out.println("Selecione um item para equipar:");

		System.out.println(ItemMenuFormatter.formatItemTableHeader());

		for (int index = firstIndex; index < lastIndex; index++) {
			Item item = availableItems.get(index);
			int optionNumber = index - firstIndex + 1;
			int quantity = party.getInventory().getQuantity(item);

			System.out.println(ItemMenuFormatter.formatItemTableRow(optionNumber, item.getName(), quantity));
		}

		int displayedItemCount = lastIndex - firstIndex;

		for (int emptySlot = displayedItemCount; emptySlot < ITEMS_PER_PAGE; emptySlot++) {
			System.out.println(ItemMenuFormatter.formatEmptyItemTableRow());
		}

		System.out.println();
		System.out.println("Página " + (currentPage + 1) + "/" + totalPages);

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

	private Optional<PlayableCharacter> selectCharacterForItem(Item item, EquipmentDefinition definition) {
		List<PlayableCharacter> eligibleCharacters = party.getMembers().stream()
				.filter(character -> character.canEquip(definition))
				.filter(character -> !isCurrentlyEquipped(character, definition)).toList();

		if (eligibleCharacters.isEmpty()) {
			displayMessageAndWaitForBack(
					definition.getItemCategory(),
					"Nenhum personagem pode equipar " + item.getName() + " no momento."
			);

			return Optional.empty();
		}

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Equipamento", getSlotName(definition.getItemCategory()));

			System.out.println();
			System.out.println(item.getName() + ", Nv. " + definition.getLevelRequirement());

			System.out.println();
			System.out.println("Quem deve equipar este item?");

			System.out.println();

			for (int index = 0; index < eligibleCharacters.size(); index++) {
				PlayableCharacter character = eligibleCharacters.get(index);

				EquipmentDefinition currentDefinition = getEquippedDefinition(
						character.getEquipment(),
						definition.getItemCategory()
				);

				System.out.println("[" + (index + 1) + "] " + character.getName());
				System.out.println("    Atual: " + getEquippedItemName(currentDefinition));
			}

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println("[0] Cancelar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= eligibleCharacters.size()) {
					feedbackMessage = "Personagem inválido.";
					continue;
				}

				return Optional.of(eligibleCharacters.get(selectedIndex));
			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número de um personagem.";
			}
		}
	}

	private boolean confirmEquipment(PlayableCharacter character, Item item, EquipmentDefinition definition) {
		EquipmentDefinition currentDefinition = getEquippedDefinition(
				character.getEquipment(),
				definition.getItemCategory()
		);
		Item currentItem = currentDefinition == null ? null : itemCatalog.getById(currentDefinition.getItemId());

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Equipamento", getSlotName(definition.getItemCategory()));

			System.out.println();
			System.out.println("Confirmar alteração no equipamento de " + character.getName() + "?");

			System.out.println();
			System.out.println(
					EquipmentDetailFormatter
							.formatComparison(character, currentItem, currentDefinition, item, definition)
			);

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println("[1] Confirmar");

			System.out.println();
			System.out.println("[0] Cancelar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return false;
			}

			if (!choice.equals("1")) {
				feedbackMessage = "Opção inválida.";
				continue;
			}

			boolean equipped = equipmentService.equip(party, character, item);

			if (!equipped) {
				feedbackMessage = "Não foi possível equipar o item.";
				continue;
			}

			return true;
		}
	}

	private boolean confirmUnequip(PlayableCharacter character, ItemCategory equipmentCategory) {
		EquipmentDefinition currentDefinition = getEquippedDefinition(character.getEquipment(), equipmentCategory);

		if (currentDefinition == null) {
			return false;
		}

		Item item = itemCatalog.getById(currentDefinition.getItemId());

		String slotReference = switch (equipmentCategory) {
		case WEAPON -> "a arma";
		case ARMOR -> "a armadura";
		case TRINKET -> "o berloque";

		default -> throw new IllegalArgumentException("Item category is not an equipment slot: " + equipmentCategory);
		};

		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Equipamento", getSlotName(equipmentCategory));

			System.out.println();
			System.out.println("Desequipar " + slotReference + " de " + character.getName() + "?");

			System.out.println();
			System.out
					.println(EquipmentDetailFormatter.formatComparison(character, item, currentDefinition, null, null));

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println("[1] Confirmar");

			System.out.println();
			System.out.println("[0] Cancelar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return false;
			}

			if (!choice.equals("1")) {
				feedbackMessage = "Opção inválida.";
				continue;
			}

			boolean unequipped = equipmentService.unequip(party, character, equipmentCategory);

			if (!unequipped) {
				feedbackMessage = "Não foi possível desequipar o item.";
				continue;
			}

			return true;
		}
	}

	private boolean isCurrentlyEquipped(PlayableCharacter character, EquipmentDefinition definition) {
		EquipmentDefinition currentDefinition = getEquippedDefinition(
				character.getEquipment(),
				definition.getItemCategory()
		);

		return currentDefinition != null && currentDefinition.getItemId().equals(definition.getItemId());
	}

	private EquipmentDefinition getEquippedDefinition(CharacterEquipment equipment, ItemCategory equipmentCategory) {
		return switch (equipmentCategory) {
		case WEAPON -> equipment.getWeapon().orElse(null);
		case ARMOR -> equipment.getArmor().orElse(null);
		case TRINKET -> equipment.getTrinket().orElse(null);

		default -> throw new IllegalArgumentException("Item category is not an equipment slot: " + equipmentCategory);
		};
	}

	private String getSlotName(ItemCategory equipmentCategory) {
		return switch (equipmentCategory) {
		case WEAPON -> "Arma";
		case ARMOR -> "Armadura";
		case TRINKET -> "Berloque";

		default -> throw new IllegalArgumentException("Item category is not an equipment slot: " + equipmentCategory);
		};
	}

	private void displayMessageAndWaitForBack(ItemCategory equipmentCategory, String message) {
		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Equipamento", getSlotName(equipmentCategory));

			System.out.println();
			ConsoleUI.displayWrappedText(message);

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
}