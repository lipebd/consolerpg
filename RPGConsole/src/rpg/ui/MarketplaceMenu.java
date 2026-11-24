package rpg.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import rpg.GameCurrency;
import rpg.persistence.CampaignSaveResult;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.marketplace.ListingStatus;
import rpg.marketplace.MarketplaceListing;
import rpg.marketplace.MarketplaceListingCancellationResult;
import rpg.marketplace.MarketplaceListingCreationResult;
import rpg.marketplace.MarketplacePurchaseResult;
import rpg.marketplace.MarketplaceRewardClaimResult;
import rpg.marketplace.MarketplaceTransactionService;
import rpg.marketplace.PendingReward;

// Displays the online marketplace accessed through an INN.
public final class MarketplaceMenu {

	private static final int ITEMS_PER_PAGE = 9;

	private final ConsoleInput consoleInput;
	private final ItemCatalog itemCatalog;
	private final MarketplaceTransactionService transactionService;
	private Supplier<CampaignSaveResult> campaignSaver;

	public MarketplaceMenu(
			ConsoleInput consoleInput,
			ItemCatalog itemCatalog,
			MarketplaceTransactionService transactionService) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");
		this.transactionService = Objects.requireNonNull(
				transactionService, "Marketplace transaction service cannot be null.");
	}

	public void open(Supplier<CampaignSaveResult> campaignSaver) {
		this.campaignSaver = Objects.requireNonNull(campaignSaver, "Campaign saver cannot be null.");
		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("\n[POSTO DE TROCAS]");
			System.out.println("Compre e venda itens com outros jogadores.");
			System.out.println("As negociações salvam a campanha automaticamente.");
			System.out.println("\n[1] Ver anúncios");
			System.out.println("[2] Criar anúncio");
			System.out.println("[3] Minhas vendas");
			System.out.println("[4] Recompensas");
			System.out.println("\n[0] Voltar");
			System.out.print("> ");

			switch (consoleInput.readTrimmedLine()) {
				case "1" -> showListings();
				case "2" -> createListing();
				case "3" -> showMySales();
				case "4" -> showRewards();
				case "0" -> {
					return;
				}
				default -> System.out.println("\nOpção inválida.");
			}
		}
	}

	private void showListings() {
		List<MarketplaceListing> listings;

		try {
			listings = transactionService.getActiveListings();
		} catch (IOException exception) {
			displayApiUnavailable();
			return;
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			displayApiUnavailable();
			return;
		}

		System.out.println("\n[ANÚNCIOS]");

		if (listings.isEmpty()) {
			System.out.println("\nNenhum anúncio disponível.");
			waitForReturn();
			return;
		}

		for (int index = 0; index < listings.size(); index++) {
			displayListingSummary(index + 1, listings.get(index), true);
		}

		int selection = readSelection(listings.size());
		if (selection == 0) {
			return;
		}

		showListingDetails(listings.get(selection - 1));
	}

	private void showListingDetails(MarketplaceListing listing) {
		System.out.println("\n[DETALHES DO ANÚNCIO]");
		displayListingFields(listing, true);
		System.out.println("\n[1] Comprar");
		System.out.println("[0] Voltar");
		System.out.print("> ");

		if (!consoleInput.readTrimmedLine().equals("1")) {
			return;
		}

		MarketplacePurchaseResult result = transactionService.purchase(listing);

		System.out.println("\n[COMPRA]");
		switch (result) {
			case SUCCESS -> {
				System.out.println("\nCompra realizada com sucesso!");
				saveCompletedTransaction();
			}
			case INVALID_LISTING -> System.out.println("\nO anúncio é inválido para este jogo.");
			case LISTING_NOT_ACTIVE -> System.out.println("\nEste anúncio não está mais disponível.");
			case CANNOT_BUY_OWN_LISTING -> System.out.println("\nVocê não pode comprar seu próprio anúncio.");
			case INSUFFICIENT_GOLD -> System.out.println("\nVocê não possui GOLD suficiente.");
			case INVENTORY_CAPACITY_EXCEEDED -> System.out.println("\nNão há espaço suficiente no inventário.");
			case API_UNAVAILABLE -> System.out.println("\nNão foi possível acessar o Posto de Trocas.");
			case SERVER_REJECTED -> System.out.println("\nA API recusou a compra.");
		}

		waitForReturn();
	}

	private void createListing() {
		Map<Item, Integer> inventoryItems = transactionService.getInventoryItemsForListing();

		if (inventoryItems.isEmpty()) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("Criar anúncio");
			System.out.println("\nVocê não possui itens negociáveis.");
			waitForReturn();
			return;
		}

		List<Item> availableItems = new ArrayList<>(inventoryItems.keySet());
		availableItems.sort(Comparator.comparing(Item::getName));

		int currentPage = 0;
		int totalPages = (availableItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
		String feedbackMessage = null;

		while (true) {
			int firstIndex = currentPage * ITEMS_PER_PAGE;
			int lastIndex = Math.min(firstIndex + ITEMS_PER_PAGE, availableItems.size());

			displayListingItemPage(availableItems, inventoryItems, firstIndex, lastIndex,
					currentPage, totalPages, feedbackMessage);

			String choice = consoleInput.readTrimmedLine();

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
				if (selectedIndex < 0 || selectedIndex >= lastIndex - firstIndex) {
					feedbackMessage = "Item inválido.";
					continue;
				}

				Item selectedItem = availableItems.get(firstIndex + selectedIndex);
				ConsoleUI.clearScreen();
				createListingForItem(selectedItem, inventoryItems.get(selectedItem));
				return;
			} catch (NumberFormatException exception) {
				feedbackMessage = "Comando inválido.";
			}
		}
	}

	private void displayListingItemPage(
			List<Item> availableItems,
			Map<Item, Integer> inventoryItems,
			int firstIndex,
			int lastIndex,
			int currentPage,
			int totalPages,
			String feedbackMessage) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading("Criar anúncio - Página " + (currentPage + 1) + "/" + totalPages);
		System.out.println();
		System.out.println("Selecione um item:");
		System.out.println(ItemMenuFormatter.formatItemTableHeader());

		for (int index = firstIndex; index < lastIndex; index++) {
			Item item = availableItems.get(index);
			System.out.println(ItemMenuFormatter.formatItemTableRow(
					index - firstIndex + 1, item.getName(), inventoryItems.get(item)));
		}
		for (int emptySlot = lastIndex - firstIndex; emptySlot < ITEMS_PER_PAGE; emptySlot++) {
			System.out.println(ItemMenuFormatter.formatEmptyItemTableRow());
		}

		StringBuilder navigation = new StringBuilder();
		if (currentPage > 0) {
			navigation.append("    [A] Anterior");
		}
		if (currentPage < totalPages - 1) {
			navigation.append("    [P] Próxima");
		}
		System.out.println(navigation);
		System.out.println(feedbackMessage == null ? "" : feedbackMessage);
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private void createListingForItem(Item item, int availableQuantity) {
		System.out.println("\n[NOVO ANÚNCIO]");
		System.out.println("\nItem: " + item.getName());
		System.out.println("Quantidade disponível: " + availableQuantity);

		Integer quantity = readPositiveValue("Quantidade para venda (0 para cancelar): ");
		if (quantity == null) {
			return;
		}

		Integer unitPrice = readPositiveValue("Preço por unidade (0 para cancelar): ");
		if (unitPrice == null) {
			return;
		}

		MarketplaceListingCreationResult result = transactionService.createListing(
				item.getId(), quantity, unitPrice);

		System.out.println("\n[CRIAÇÃO DO ANÚNCIO]");
		switch (result) {
			case SUCCESS -> {
				System.out.println("\nAnúncio criado com sucesso!");
				saveCompletedTransaction();
			}
			case INVALID_ITEM -> System.out.println("\nO item selecionado é inválido.");
			case ITEM_NOT_TRADABLE -> System.out.println("\nEste item não pode ser negociado.");
			case INVALID_QUANTITY -> System.out.println("\nA quantidade informada é inválida.");
			case INVALID_UNIT_PRICE -> System.out.println("\nO preço informado é inválido.");
			case INSUFFICIENT_ITEMS -> System.out.println("\nVocê não possui essa quantidade de itens.");
			case API_UNAVAILABLE -> System.out.println("\nNão foi possível acessar o Posto de Trocas. Seus itens foram devolvidos.");
			case SERVER_REJECTED -> System.out.println("\nA API recusou o anúncio. Seus itens foram devolvidos.");
		}

		waitForReturn();
	}

	private void showMySales() {
		List<MarketplaceListing> listings;

		try {
			listings = transactionService.getListingsForCurrentPlayer();
		} catch (IOException exception) {
			displayApiUnavailable();
			return;
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			displayApiUnavailable();
			return;
		}

		System.out.println("\n[MINHAS VENDAS]");

		if (listings.isEmpty()) {
			System.out.println("\nVocê ainda não criou nenhum anúncio.");
			waitForReturn();
			return;
		}

		for (int index = 0; index < listings.size(); index++) {
			displayListingSummary(index + 1, listings.get(index), false);
		}

		int selection = readSelection(listings.size());
		if (selection == 0) {
			return;
		}

		showMyListingDetails(listings.get(selection - 1));
	}

	private void showMyListingDetails(MarketplaceListing listing) {
		System.out.println("\n[MEU ANÚNCIO]");
		displayListingFields(listing, false);

		if (listing.status() != ListingStatus.ACTIVE) {
			waitForReturn();
			return;
		}

		System.out.println("\n[1] Cancelar anúncio");
		System.out.println("[0] Voltar");
		System.out.print("> ");

		if (!consoleInput.readTrimmedLine().equals("1")) {
			return;
		}

		MarketplaceListingCancellationResult result = transactionService.cancelListing(listing);

		System.out.println("\n[CANCELAMENTO]");
		switch (result) {
			case SUCCESS -> {
				System.out.println("\nAnúncio cancelado e itens devolvidos!");
				saveCompletedTransaction();
			}
			case INVALID_LISTING -> System.out.println("\nO anúncio selecionado é inválido.");
			case NOT_LISTING_OWNER -> System.out.println("\nEste anúncio pertence a outro jogador.");
			case LISTING_NOT_ACTIVE -> System.out.println("\nEste anúncio não está mais ativo.");
			case INVENTORY_CAPACITY_EXCEEDED -> System.out.println("\nLibere espaço no inventário antes de cancelar.");
			case API_UNAVAILABLE -> System.out.println("\nNão foi possível acessar o Posto de Trocas.");
			case SERVER_REJECTED -> System.out.println("\nA API recusou o cancelamento.");
		}

		waitForReturn();
	}

	private void showRewards() {
		List<PendingReward> rewards;

		try {
			rewards = transactionService.getPendingRewardsForCurrentPlayer();
		} catch (IOException exception) {
			displayApiUnavailable();
			return;
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			displayApiUnavailable();
			return;
		}

		System.out.println("\n[RECOMPENSAS]");

		if (rewards.isEmpty()) {
			System.out.println("\nNão há recompensas disponíveis.");
			waitForReturn();
			return;
		}

		for (int index = 0; index < rewards.size(); index++) {
			System.out.println("\n[" + (index + 1) + "] " + GameCurrency.format(rewards.get(index).amount()));
		}

		int selection = readSelection(rewards.size());
		if (selection == 0) {
			return;
		}

		claimReward(rewards.get(selection - 1));
	}

	private void claimReward(PendingReward reward) {
		System.out.println("\n[RECOMPENSA]");
		System.out.println("\nValor: " + GameCurrency.format(reward.amount()));
		System.out.println("Transação: " + reward.transactionId());
		System.out.println("\n[1] Resgatar");
		System.out.println("[0] Voltar");
		System.out.print("> ");

		if (!consoleInput.readTrimmedLine().equals("1")) {
			return;
		}

		MarketplaceRewardClaimResult result = transactionService.claimReward(reward);

		switch (result) {
			case SUCCESS -> {
				System.out.println("\nRecompensa resgatada com sucesso!");
				saveCompletedTransaction();
			}
			case INVALID_REWARD -> System.out.println("\nA recompensa é inválida.");
			case REWARD_NOT_PENDING -> System.out.println("\nEsta recompensa já foi resgatada.");
			case NOT_REWARD_OWNER -> System.out.println("\nEsta recompensa pertence a outro jogador.");
			case GOLD_CAPACITY_EXCEEDED -> System.out.println("\nVocê não consegue carregar todo esse GOLD.");
			case API_UNAVAILABLE -> System.out.println("\nNão foi possível acessar o Posto de Trocas.");
			case SERVER_REJECTED -> System.out.println("\nA API recusou o resgate.");
		}

		waitForReturn();
	}

	private void displayListingSummary(int position, MarketplaceListing listing, boolean showSeller) {
		System.out.println("\n[" + position + "] " + getItemName(listing.itemId()));
		System.out.println("Quantidade: " + listing.quantity());
		System.out.println("Total: " + GameCurrency.format(listing.totalPrice()));
		if (showSeller) {
			System.out.println("Vendedor: " + listing.sellerName());
		} else {
			System.out.println("Status: " + getStatusLabel(listing.status()));
		}
	}

	private void displayListingFields(MarketplaceListing listing, boolean showSeller) {
		System.out.println("\nItem: " + getItemName(listing.itemId()));
		System.out.println("Quantidade: " + listing.quantity());
		System.out.println("Preço unitário: " + GameCurrency.format(listing.unitPrice()));
		System.out.println("Total: " + GameCurrency.format(listing.totalPrice()));
		if (showSeller) {
			System.out.println("Vendedor: " + listing.sellerName());
		}
		System.out.println("Status: " + getStatusLabel(listing.status()));
	}

	private String getItemName(String itemId) {
		try {
			return itemCatalog.getById(itemId).getName();
		} catch (IllegalArgumentException exception) {
			return "Item desconhecido (" + itemId + ")";
		}
	}

	private static String getStatusLabel(ListingStatus status) {
		return switch (status) {
			case ACTIVE -> "Ativo";
			case SOLD -> "Vendido";
			case CANCELLED -> "Cancelado";
		};
	}

	private int readSelection(int maximum) {
		while (true) {
			System.out.println("\n[0] Voltar");
			System.out.print("> ");

			try {
				int selection = Integer.parseInt(consoleInput.readTrimmedLine());
				if (selection >= 0 && selection <= maximum) {
					return selection;
				}
			} catch (NumberFormatException exception) {
				// The common feedback below also covers non-numeric input.
			}

			System.out.println("Opção inválida.");
		}
	}

	private Integer readPositiveValue(String prompt) {
		while (true) {
			System.out.print("\n" + prompt);

			try {
				int value = Integer.parseInt(consoleInput.readTrimmedLine());
				if (value == 0) {
					return null;
				}
				if (value > 0) {
					return value;
				}
			} catch (NumberFormatException exception) {
				// The common feedback below also covers non-numeric input.
			}

			System.out.println("Valor inválido.");
		}
	}

	private void displayApiUnavailable() {
		System.out.println("\nNão foi possível acessar o Posto de Trocas.");
		System.out.println("Confirme se a api está em execução.");
		waitForReturn();
	}

	private void saveCompletedTransaction() {
		while (!campaignSaver.get().isSuccess()) {
			System.out.println("\nA negociação foi concluída, mas a campanha não foi salva.");
			System.out.println("Mantenha o jogo aberto para preservar seus itens e GOLD.");
			System.out.println("[1] Tentar salvar novamente");
			System.out.print("> ");
			while (!consoleInput.readTrimmedLine().equals("1")) {
				System.out.println("Salve a campanha antes de continuar. Digite 1 para tentar novamente.");
				System.out.print("> ");
			}
		}
		System.out.println("Campanha salva automaticamente.");
	}

	private void waitForReturn() {
		System.out.println("\n[0] Voltar");
		System.out.print("> ");

		while (!consoleInput.readTrimmedLine().equals("0")) {
			System.out.println("Digite 0 para voltar.");
			System.out.print("> ");
		}
	}
}