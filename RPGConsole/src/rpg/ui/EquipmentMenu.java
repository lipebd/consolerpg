package rpg.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

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
	private final Scanner scanner;
	private final EquipmentCatalog equipmentCatalog;
	private final ItemCatalog itemCatalog;
	private final EquipmentService equipmentService;

	public EquipmentMenu(
			Party party,
			Scanner scanner,
			EquipmentCatalog equipmentCatalog,
			ItemCatalog itemCatalog,
			EquipmentService equipmentService
	) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");

		this.equipmentCatalog = Objects.requireNonNull(equipmentCatalog, "Equipment catalog cannot be null.");

		this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");

		this.equipmentService = Objects.requireNonNull(equipmentService, "Equipment service cannot be null.");
	}

	public void openForCharacter(PlayableCharacter character) {
		Objects.requireNonNull(character, "Playable character cannot be null.");

		while (true) {
			displayCharacterEquipment(character);

			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			switch (choice) {
			case "0" -> {
				return;
			}

			case "1" -> openSlot(character, ItemCategory.WEAPON);
			case "2" -> openSlot(character, ItemCategory.ARMOR);
			case "3" -> openSlot(character, ItemCategory.TRINKET);

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	public boolean openForItem(Item item) {
		Objects.requireNonNull(item, "Item cannot be null.");

		EquipmentDefinition definition = equipmentCatalog.getByItemId(item.getId());

		while (true) {
			System.out.println("\n" + EquipmentDetailFormatter.formatDetails(item, definition));

			System.out.println("Quantidade disponível: " + party.getInventory().getQuantity(item));

			System.out.println("\n[1] Equipar | [0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return false;
			}

			if (!choice.equals("1")) {
				System.out.println("Opção inválida.");
				continue;
			}

			Optional<PlayableCharacter> selectedCharacter = selectCharacterForItem(item, definition);

			if (selectedCharacter.isEmpty()) {
				continue;
			}

			if (confirmEquipment(selectedCharacter.get(), item, definition)) {
				return true;
			}
		}
	}

	private void displayCharacterEquipment(PlayableCharacter character) {
		CharacterEquipment equipment = character.getEquipment();

		System.out.println("\n[EQUIPAMENTOS DE " + character.getName().toUpperCase(Locale.ROOT) + "]");

		System.out.println();
		System.out.println("[1] Arma:      " + getEquippedItemName(equipment.getWeapon().orElse(null)));

		System.out.println("[2] Armadura:  " + getEquippedItemName(equipment.getArmor().orElse(null)));

		System.out.println("[3] Berloque:  " + getEquippedItemName(equipment.getTrinket().orElse(null)));

		System.out.println("\n[0] Voltar");
	}

	private String getEquippedItemName(EquipmentDefinition definition) {
		if (definition == null) {
			return "--";
		}

		return itemCatalog.getById(definition.getItemId()).getName();
	}

	private void openSlot(PlayableCharacter character, ItemCategory equipmentCategory) {
		while (true) {
			EquipmentDefinition equippedDefinition = getEquippedDefinition(character.getEquipment(), equipmentCategory);

			displaySlotDetails(character, equipmentCategory, equippedDefinition);

			System.out.println();

			if (equippedDefinition == null) {
				System.out.println("[1] Equipar | [0] Voltar");
			} else {
				System.out.println("[1] Trocar | [2] Desequipar | [0] Voltar");
			}

			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return;
			}

			if (choice.equals("1")) {
				Optional<Item> selectedItem = selectItemForCharacter(character, equipmentCategory);

				if (selectedItem.isEmpty()) {
					continue;
				}

				EquipmentDefinition selectedDefinition = equipmentCatalog.getByItemId(selectedItem.get().getId());

				confirmEquipment(character, selectedItem.get(), selectedDefinition);

				continue;
			}

			if (choice.equals("2") && equippedDefinition != null) {
				confirmUnequip(character, equipmentCategory);
				continue;
			}

			System.out.println("Opção inválida.");
		}
	}

	private void displaySlotDetails(
			PlayableCharacter character,
			ItemCategory equipmentCategory,
			EquipmentDefinition definition
	) {
		System.out.println(
				"\n[" + getSlotName(equipmentCategory).toUpperCase(Locale.ROOT) + " DE "
						+ character.getName().toUpperCase(Locale.ROOT) + "]"
		);

		if (definition == null) {
			System.out.println("\nNenhum item equipado.");
			return;
		}

		Item item = itemCatalog.getById(definition.getItemId());

		System.out.println("\n" + EquipmentDetailFormatter.formatDetails(item, definition));
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
			System.out.println(
					"\nNenhum " + getSlotName(equipmentCategory).toLowerCase(Locale.ROOT)
							+ " compatível está disponível no inventário."
			);

			waitForBack();

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
					totalPages
			);

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return Optional.empty();
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
					System.out.println("Equipamento inválido.");
					continue;
				}

				return Optional.of(availableItems.get(firstIndex + selectedIndex));
			} catch (NumberFormatException exception) {
				System.out.println("Comando inválido.");
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
			int totalPages
	) {
		System.out.println(
				"\n[" + getSlotName(equipmentCategory).toUpperCase(Locale.ROOT) + "S DISPONÍVEIS PARA "
						+ character.getName().toUpperCase(Locale.ROOT) + " — PÁGINA " + (currentPage + 1) + "/"
						+ totalPages + "]"
		);

		System.out.println();

		for (int index = firstIndex; index < lastIndex; index++) {
			Item item = availableItems.get(index);

			EquipmentDefinition definition = equipmentCatalog.getByItemId(item.getId());

			int optionNumber = index - firstIndex + 1;

			System.out.println(
					"[" + optionNumber + "] " + item.getName() + " [Nv. " + definition.getLevelRequirement() + "]"
			);

			System.out.println("    " + EquipmentDetailFormatter.formatBonuses(definition));
		}

		if (currentPage > 0) {
			System.out.println("\n[A] Página anterior");
		}

		if (currentPage < totalPages - 1) {
			System.out.println("[P] Próxima página");
		}

		System.out.println("\n[0] Voltar");
		System.out.print("> ");
	}

	private Optional<PlayableCharacter> selectCharacterForItem(Item item, EquipmentDefinition definition) {
		List<PlayableCharacter> eligibleCharacters = party.getMembers().stream()
				.filter(character -> character.canEquip(definition))
				.filter(character -> !isCurrentlyEquipped(character, definition)).toList();

		if (eligibleCharacters.isEmpty()) {
			System.out.println("\nNenhum personagem pode equipar " + item.getName() + " no momento.");

			waitForBack();

			return Optional.empty();
		}

		while (true) {
			System.out.println("\n[QUEM DEVE EQUIPAR?]\n");

			for (int index = 0; index < eligibleCharacters.size(); index++) {
				PlayableCharacter character = eligibleCharacters.get(index);

				EquipmentDefinition currentDefinition = getEquippedDefinition(
						character.getEquipment(),
						definition.getItemCategory()
				);

				System.out.println(
						"[" + (index + 1) + "] " + character.getName() + " | Atual: "
								+ getEquippedItemName(currentDefinition)
				);
			}

			System.out.println("\n[0] Cancelar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= eligibleCharacters.size()) {
					System.out.println("Personagem inválido.");
					continue;
				}

				return Optional.of(eligibleCharacters.get(selectedIndex));
			} catch (NumberFormatException exception) {
				System.out.println("Digite o número de um personagem.");
			}
		}
	}

	private boolean confirmEquipment(PlayableCharacter character, Item item, EquipmentDefinition definition) {
		EquipmentDefinition currentDefinition = getEquippedDefinition(
				character.getEquipment(),
				definition.getItemCategory()
		);
		Item currentItem = currentDefinition == null ? null : itemCatalog.getById(currentDefinition.getItemId());

		while (true) {
			System.out.println("\n[CONFIRMAR EQUIPAMENTO]\n");

			System.out.println(character.getName().toUpperCase(Locale.ROOT));

			System.out.println(
					EquipmentDetailFormatter
							.formatComparison(character, currentItem, currentDefinition, item, definition)
			);

			System.out.println("\n[1] Confirmar | [0] Cancelar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return false;
			}

			if (!choice.equals("1")) {
				System.out.println("Opção inválida.");
				continue;
			}

			boolean equipped = equipmentService.equip(party, character, item);

			if (!equipped) {
				System.out.println("Não foi possível equipar o item.");

				return false;
			}

			System.out.println(character.getName() + " equipou " + item.getName() + ".");

			return true;
		}
	}

	private boolean confirmUnequip(PlayableCharacter character, ItemCategory equipmentCategory) {
		EquipmentDefinition currentDefinition = getEquippedDefinition(character.getEquipment(), equipmentCategory);

		if (currentDefinition == null) {
			return false;
		}

		Item item = itemCatalog.getById(currentDefinition.getItemId());

		while (true) {
			System.out.println("\n[CONFIRMAR REMOÇÃO]\n");

			System.out.println(character.getName().toUpperCase(Locale.ROOT));

			System.out
					.println(EquipmentDetailFormatter.formatComparison(character, item, currentDefinition, null, null));

			System.out.println("\n[1] Confirmar | [0] Cancelar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return false;
			}

			if (!choice.equals("1")) {
				System.out.println("Opção inválida.");
				continue;
			}

			boolean unequipped = equipmentService.unequip(party, character, equipmentCategory);

			if (!unequipped) {
				System.out.println("Não foi possível desequipar o item.");

				return false;
			}

			System.out.println(item.getName() + " retornou ao inventário.");

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

	private void waitForBack() {
		while (true) {
			System.out.println("\n[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return;
			}

			System.out.println("Opção inválida.");
		}
	}
}