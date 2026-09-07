package rpg.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

import rpg.GameCurrency;
import rpg.characters.Party;
import rpg.world.shops.ShopOfferState;
import rpg.world.shops.ShopState;

// Displays a shop and its currently available offers.
public final class ShopMenu {

	private static final int ITEMS_PER_PAGE = 8;
	private static final int PRICE_WIDTH = 14;

	private final Party party;
	private final Scanner scanner;

	public ShopMenu(Party party, Scanner scanner) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
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

			case "2" -> feedbackMessage = "As vendas ainda não estão disponíveis.";

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
		List<ShopOfferState> availableOffers = getSortedAvailableOffers(shopState);

		if (availableOffers.isEmpty()) {
			displayEmptyPurchaseList();
			return;
		}

		int currentPage = 0;
		int totalPages = (availableOffers.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

		String feedbackMessage = null;

		while (true) {
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

				feedbackMessage = "As compras ainda não estão disponíveis.";

			} catch (NumberFormatException exception) {
				feedbackMessage = "Comando inválido.";
			}
		}
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

	private void displayEmptyPurchaseList() {
		while (true) {
			System.out.println("\n[COMPRAR]");

			System.out.println();
			System.out.println("Não há itens disponíveis para compra.");

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