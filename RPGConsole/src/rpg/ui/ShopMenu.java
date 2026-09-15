package rpg.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Scanner;

import rpg.GameCurrency;
import rpg.characters.Party;
import rpg.items.Item;
import rpg.world.shops.ShopOfferState;
import rpg.world.shops.ShopPurchaseResult;
import rpg.world.shops.ShopState;
import rpg.world.shops.ShopTransactionService;
import rpg.items.ItemCategory;
import rpg.world.shops.ShopSaleResult;
import rpg.world.WorldClock;

// Displays a shop and its currently available offers.
public final class ShopMenu {

	private static final int ITEMS_PER_PAGE = 8;
	private static final int PRICE_WIDTH = 14;

	private final WorldClock worldClock;
	private final Party party;
	private final Scanner scanner;
	private final ShopTransactionService transactionService;

	public ShopMenu(Party party, Scanner scanner, WorldClock worldClock) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
		this.worldClock = Objects.requireNonNull(worldClock, "World clock cannot be null.");
		this.transactionService = new ShopTransactionService(this.party);
	}

	public void open(ShopState shopState) {
		Objects.requireNonNull(shopState, "Shop state cannot be null.");

		String feedbackMessage = null;

		while (true) {
			displayMainMenu(shopState, feedbackMessage);

			String choice = scanner.nextLine().trim();

			switch (choice) {
			case "1" -> {
				openPurchaseOffers(shopState);
				feedbackMessage = null;
			}

			case "2" -> {
				openSaleCategories();
				feedbackMessage = null;
			}

			case "0" -> {
				return;
			}

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	private void displayMainMenu(ShopState shopState, String feedbackMessage) {
		System.out.println("\n[" + shopState.getDefinition().getName().toUpperCase(Locale.ROOT) + "]");
		System.out.println();
		System.out.println(WorldTimeFormatter.formatCurrentTime(worldClock));
		System.out.println("Próxima reposição: " + WorldTimeFormatter.formatNextRestockingTime(worldClock));
		System.out.println();
		System.out.println(shopState.getDefinition().getDescription());

		System.out.println();
		System.out.println(GameCurrency.getName() + " disponível: " + GameCurrency.format(party.getGold()));

		if (feedbackMessage != null) {
			System.out.println();
			System.out.println(feedbackMessage);
		}

		System.out.println();
		System.out.println("[1] Comprar");
		System.out.println("[2] Vender");
		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private void openPurchaseOffers(ShopState shopState) {
		int currentPage = 0;
		String feedbackMessage = null;

		while (true) {
			List<ShopOfferState> availableOffers = getSortedAvailableOffers(shopState);

			if (availableOffers.isEmpty()) {
				displayEmptyPurchaseList(feedbackMessage);
				return;
			}

			int totalPages = (availableOffers.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

			currentPage = Math.min(currentPage, totalPages - 1);

			int firstIndex = currentPage * ITEMS_PER_PAGE;

			int lastIndex = Math.min(firstIndex + ITEMS_PER_PAGE, availableOffers.size());

			displayPurchasePage(availableOffers, firstIndex, lastIndex, currentPage, totalPages, feedbackMessage);

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return;
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
					feedbackMessage = "Item inválido.";
					continue;
				}

				ShopOfferState selectedOffer = availableOffers.get(firstIndex + selectedIndex);

				OptionalInt selectedQuantity = selectPurchaseQuantity(selectedOffer);

				if (selectedQuantity.isEmpty()) {
					feedbackMessage = "Compra cancelada.";
					continue;
				}

				int quantity = selectedQuantity.getAsInt();
				int totalPrice;

				try {
					totalPrice = Math.multiplyExact(selectedOffer.getStockEntry().getFinalShopSalePrice(), quantity);

				} catch (ArithmeticException exception) {
					feedbackMessage = "O valor total da compra excede o limite suportado.";
					continue;
				}

				if (!confirmPurchase(selectedOffer, quantity, totalPrice)) {
					feedbackMessage = "Compra cancelada.";
					continue;
				}

				ShopPurchaseResult purchaseResult = transactionService.purchase(shopState, selectedOffer, quantity);

				feedbackMessage = getPurchaseFeedback(purchaseResult);

			} catch (NumberFormatException exception) {
				feedbackMessage = "Comando inválido.";
			}
		}
	}

	private OptionalInt selectPurchaseQuantity(ShopOfferState offerState) {
		int availableQuantity = offerState.getRemainingStock();

		if (availableQuantity == 1) {
			return OptionalInt.of(1);
		}

		Item item = offerState.getStockEntry().getItem();

		while (true) {
			System.out.println("\n[QUANTIDADE]");

			System.out.println();
			System.out.println(item.getName());

			System.out.println("Estoque disponível: " + availableQuantity);

			System.out.println();
			System.out.println("Digite a quantidade que deseja comprar.");
			System.out.println("[0] Cancelar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return OptionalInt.empty();
			}

			try {
				int quantity = Integer.parseInt(choice);

				if (quantity < 1 || quantity > availableQuantity) {
					System.out.println("Quantidade inválida.");
					continue;
				}

				return OptionalInt.of(quantity);

			} catch (NumberFormatException exception) {
				System.out.println("Digite uma quantidade válida.");
			}
		}
	}

	private boolean confirmPurchase(ShopOfferState offerState, int quantity, int totalPrice) {
		Item item = offerState.getStockEntry().getItem();

		int unitPrice = offerState.getStockEntry().getFinalShopSalePrice();

		while (true) {
			System.out.println("\n[CONFIRMAR COMPRA]");

			System.out.println();
			System.out.println("[" + item.getName().toUpperCase(Locale.ROOT) + "]");

			System.out.println();
			System.out.println(item.getDescription());

			System.out.println();
			System.out.println("Quantidade: " + quantity);
			System.out.println("Preço unitário: " + GameCurrency.format(unitPrice));
			System.out.println("Total: " + GameCurrency.format(totalPrice));

			System.out.println();
			System.out.println("[1] Confirmar");
			System.out.println("[0] Cancelar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			switch (choice) {
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

	private String getPurchaseFeedback(ShopPurchaseResult purchaseResult) {
		return switch (purchaseResult) {
		case SUCCESS -> "Compra realizada com sucesso.";

		case INVALID_SELECTION -> "A oferta selecionada não está mais disponível.";

		case INVALID_QUANTITY -> "A quantidade selecionada é inválida.";

		case INSUFFICIENT_STOCK -> "O estoque disponível não é suficiente.";

		case INSUFFICIENT_GOLD -> "As moedas do grupo não são suficientes para esta compra.";

		case INVENTORY_CAPACITY_EXCEEDED -> "O inventário não comporta essa quantidade.";

		case TOTAL_OVERFLOW -> "O valor total da compra excede o limite suportado.";
		};
	}

	private void openSaleCategories() {
		List<ItemCategory> categories = List.of(
				ItemCategory.CONSUMABLE,
				ItemCategory.WEAPON,
				ItemCategory.ARMOR,
				ItemCategory.TRINKET,
				ItemCategory.MATERIAL,
				ItemCategory.SKILL_SCROLL
		);

		while (true) {
			displaySaleCategoryMenu(categories);

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= categories.size()) {
					System.out.println("Categoria inválida.");
					continue;
				}

				openSaleCategory(categories.get(selectedIndex));

			} catch (NumberFormatException exception) {
				System.out.println("Digite o número de uma categoria.");
			}
		}
	}

	private void displaySaleCategoryMenu(List<ItemCategory> categories) {
		System.out.println("\n[VENDER]");

		System.out.println();
		System.out.println("Escolha uma categoria:");

		for (int index = 0; index < categories.size(); index++) {
			System.out.println("[" + (index + 1) + "] " + getCategoryDisplayName(categories.get(index)));
		}

		System.out.println();
		System.out.println("[0] Voltar à loja");
		System.out.print("> ");
	}

	private void openSaleCategory(ItemCategory category) {
		int currentPage = 0;
		String feedbackMessage = null;

		while (true) {
			List<Item> sellableItems = getSortedSellableItems(category);

			if (sellableItems.isEmpty()) {
				displayEmptySaleCategory(category, feedbackMessage);
				return;
			}

			int totalPages = (sellableItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

			currentPage = Math.min(currentPage, totalPages - 1);

			int firstIndex = currentPage * ITEMS_PER_PAGE;

			int lastIndex = Math.min(firstIndex + ITEMS_PER_PAGE, sellableItems.size());

			displaySaleItemPage(
					category,
					sellableItems,
					firstIndex,
					lastIndex,
					currentPage,
					totalPages,
					feedbackMessage
			);

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return;
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
					feedbackMessage = "Item inválido.";
					continue;
				}

				Item selectedItem = sellableItems.get(firstIndex + selectedIndex);

				OptionalInt selectedQuantity = selectSaleQuantity(selectedItem);

				if (selectedQuantity.isEmpty()) {
					feedbackMessage = "Venda cancelada.";
					continue;
				}

				int quantity = selectedQuantity.getAsInt();
				int totalValue;

				try {
					totalValue = Math.multiplyExact(selectedItem.getShopPurchasePrice(), quantity);

				} catch (ArithmeticException exception) {
					feedbackMessage = "O valor total da venda excede o limite suportado.";
					continue;
				}

				if (!confirmSale(selectedItem, quantity, totalValue)) {
					feedbackMessage = "Venda cancelada.";
					continue;
				}

				ShopSaleResult saleResult = transactionService.sell(selectedItem, quantity);

				feedbackMessage = getSaleFeedback(saleResult);

			} catch (NumberFormatException exception) {
				feedbackMessage = "Comando inválido.";
			}
		}
	}

	private OptionalInt selectSaleQuantity(Item item) {
		int ownedQuantity = party.getInventory().getQuantity(item);

		if (ownedQuantity == 1) {
			return OptionalInt.of(1);
		}

		while (true) {
			System.out.println("\n[QUANTIDADE]");

			System.out.println();
			System.out.println(item.getName());

			System.out.println("Quantidade possuída: " + ownedQuantity);

			System.out.println();
			System.out.println("Digite a quantidade que deseja vender.");
			System.out.println("[0] Cancelar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return OptionalInt.empty();
			}

			try {
				int quantity = Integer.parseInt(choice);

				if (quantity < 1 || quantity > ownedQuantity) {
					System.out.println("Quantidade inválida.");
					continue;
				}

				return OptionalInt.of(quantity);

			} catch (NumberFormatException exception) {
				System.out.println("Digite uma quantidade válida.");
			}
		}
	}

	private boolean confirmSale(Item item, int quantity, int totalValue) {
		int unitPrice = item.getShopPurchasePrice();

		while (true) {
			System.out.println("\n[CONFIRMAR VENDA]");

			System.out.println();
			System.out.println("[" + item.getName().toUpperCase(Locale.ROOT) + "]");

			System.out.println();
			System.out.println(item.getDescription());

			System.out.println();
			System.out.println("Quantidade: " + quantity);
			System.out.println("Preço unitário: " + GameCurrency.format(unitPrice));
			System.out.println("Total: " + GameCurrency.format(totalValue));

			System.out.println();
			System.out.println("[1] Confirmar");
			System.out.println("[0] Cancelar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			switch (choice) {
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

	private String getSaleFeedback(ShopSaleResult saleResult) {
		return switch (saleResult) {
		case SUCCESS -> "Venda realizada com sucesso.";

		case INVALID_SELECTION -> "O item selecionado não é válido.";

		case ITEM_NOT_SELLABLE -> "Este item não pode ser vendido.";

		case INVALID_QUANTITY -> "A quantidade selecionada é inválida.";

		case INSUFFICIENT_INVENTORY -> "O grupo não possui a quantidade selecionada.";

		case TOTAL_OVERFLOW -> "O valor total da venda excede o limite suportado.";

		case GOLD_CAPACITY_EXCEEDED -> "O grupo não pode receber mais moedas.";
		};
	}

	private List<Item> getSortedSellableItems(ItemCategory category) {
		return party.getInventory().getContents().keySet().stream()
				.filter(item -> item.getCategory() == category && item.getShopPurchasePrice() != null)
				.sorted(Comparator.comparing(Item::getName, String.CASE_INSENSITIVE_ORDER).thenComparing(Item::getId))
				.toList();
	}

	private void displaySaleItemPage(
			ItemCategory category,
			List<Item> sellableItems,
			int firstIndex,
			int lastIndex,
			int currentPage,
			int totalPages,
			String feedbackMessage
	) {
		System.out.println(
				"\n[" + getCategoryDisplayName(category).toUpperCase(Locale.ROOT) + " — PÁGINA " + (currentPage + 1)
						+ "/" + totalPages + "]"
		);

		System.out.println();
		System.out.println("Selecione um item para vender:");

		System.out.printf(
				"%-4s %-" + ItemMenuFormatter.ITEM_NAME_WIDTH + "s %-" + PRICE_WIDTH + "s %s%n",
				"",
				"Item",
				"Preço",
				"Qtd."
		);

		for (int index = firstIndex; index < lastIndex; index++) {
			Item item = sellableItems.get(index);

			String displayName = ItemMenuFormatter.truncateName(item.getName());

			int pageOption = index - firstIndex + 1;

			System.out.printf(
					"[%d]  %-" + ItemMenuFormatter.ITEM_NAME_WIDTH + "s %-" + PRICE_WIDTH + "s x%d%n",
					pageOption,
					displayName,
					GameCurrency.format(item.getShopPurchasePrice()),
					party.getInventory().getQuantity(item)
			);
		}

		int displayedItemCount = lastIndex - firstIndex;

		for (int emptySlot = displayedItemCount; emptySlot < ITEMS_PER_PAGE; emptySlot++) {
			System.out.printf(
					"     %-" + ItemMenuFormatter.ITEM_NAME_WIDTH + "s %-" + PRICE_WIDTH + "s %s%n",
					"--",
					"--",
					"--"
			);
		}

		if (currentPage > 0) {
			System.out.println("[A] Página anterior");
		}

		if (currentPage < totalPages - 1) {
			System.out.println("[P] Próxima página");
		}

		if (feedbackMessage != null) {
			System.out.println();
			System.out.println(feedbackMessage);
		}

		System.out.println();
		System.out.println("[0] Voltar às categorias");
		System.out.print("> ");
	}

	private void displayEmptySaleCategory(ItemCategory category, String feedbackMessage) {
		while (true) {
			System.out.println("\n[" + getCategoryDisplayName(category).toUpperCase(Locale.ROOT) + "]");

			System.out.println();
			System.out.println("Você não possui itens vendáveis nesta categoria.");
			if (feedbackMessage != null) {
				System.out.println();
				System.out.println(feedbackMessage);
			}

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

	private List<ShopOfferState> getSortedAvailableOffers(ShopState shopState) {
		return shopState.getAvailableOffers().stream()
				.sorted(
						Comparator
								.comparing(
										(ShopOfferState offerState) -> offerState.getStockEntry().getItem()
												.getCategory()
								)
								.thenComparing(
										offerState -> offerState.getStockEntry().getItem().getName(),
										String.CASE_INSENSITIVE_ORDER
								).thenComparingInt(offerState -> offerState.getStockEntry().getFinalShopSalePrice())
								.thenComparing(offerState -> offerState.getStockEntry().getId())
				).toList();
	}

	private void displayPurchasePage(
			List<ShopOfferState> availableOffers,
			int firstIndex,
			int lastIndex,
			int currentPage,
			int totalPages,
			String feedbackMessage
	) {
		System.out.println("\n[COMPRAR — PÁGINA " + (currentPage + 1) + "/" + totalPages + "]");

		System.out.println();
		System.out.println("Selecione um item para comprar:");

		System.out.printf(
				"%-4s %-" + ItemMenuFormatter.ITEM_NAME_WIDTH + "s %-" + PRICE_WIDTH + "s %s%n",
				"",
				"Item",
				"Preço",
				"Estoque"
		);

		for (int index = firstIndex; index < lastIndex; index++) {
			ShopOfferState offerState = availableOffers.get(index);

			String displayName = ItemMenuFormatter.truncateName(offerState.getStockEntry().getItem().getName());

			int pageOption = index - firstIndex + 1;

			System.out.printf(
					"[%d]  %-" + ItemMenuFormatter.ITEM_NAME_WIDTH + "s %-" + PRICE_WIDTH + "s x%d%n",
					pageOption,
					displayName,
					GameCurrency.format(offerState.getStockEntry().getFinalShopSalePrice()),
					offerState.getRemainingStock()
			);
		}

		int displayedItemCount = lastIndex - firstIndex;

		for (int emptySlot = displayedItemCount; emptySlot < ITEMS_PER_PAGE; emptySlot++) {
			System.out.printf(
					"     %-" + ItemMenuFormatter.ITEM_NAME_WIDTH + "s %-" + PRICE_WIDTH + "s %s%n",
					"--",
					"--",
					"--"
			);
		}

		if (currentPage > 0) {
			System.out.println("[A] Página anterior");
		}

		if (currentPage < totalPages - 1) {
			System.out.println("[P] Próxima página");
		}

		if (feedbackMessage != null) {
			System.out.println();
			System.out.println(feedbackMessage);
		}

		System.out.println();
		System.out.println("[0] Voltar à loja");
		System.out.print("> ");
	}

	private void displayEmptyPurchaseList(String feedbackMessage) {
		while (true) {
			System.out.println("\n[COMPRAR]");

			System.out.println();
			System.out.println("Não há itens disponíveis para compra.");
			if (feedbackMessage != null) {
				System.out.println();
				System.out.println(feedbackMessage);
			}

			System.out.println();
			System.out.println("[0] Voltar à loja");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			if (choice.equals("0")) {
				return;
			}

			System.out.println("Opção inválida.");
		}
	}
}