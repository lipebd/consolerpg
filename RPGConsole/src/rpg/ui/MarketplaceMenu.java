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
	private static final int REWARDS_PER_PAGE = 9;
	private static final int REWARD_ITEM_WIDTH = 36;
	private static final String REWARD_ROW_FORMAT = "%-4s %-" + REWARD_ITEM_WIDTH + "s %s%n";

	private static final int LISTINGS_PER_PAGE = 9;
	private static final int LISTING_ITEM_WIDTH = 25;
	private static final int LISTING_OWNER_WIDTH = 19;
	private static final String LISTING_ROW_FORMAT = "%-4s %-" + LISTING_ITEM_WIDTH + "s %-12s %-13s %s%n";

	private final ConsoleInput consoleInput;
	private final ItemCatalog itemCatalog;
	private final MarketplaceTransactionService transactionService;
	private Supplier<CampaignSaveResult> campaignSaver;

	public MarketplaceMenu(ConsoleInput consoleInput, ItemCatalog itemCatalog,
			MarketplaceTransactionService transactionService) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");
		this.transactionService = Objects.requireNonNull(transactionService,
				"Marketplace transaction service cannot be null.");
	}

	public void open(Supplier<CampaignSaveResult> campaignSaver) {
		this.campaignSaver = Objects.requireNonNull(campaignSaver, "Campaign saver cannot be null.");
		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("[POSTO DE TROCAS]");
			System.out.println("\nCompre e venda itens com outros jogadores.");
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

	private void showListingDetails(MarketplaceListing listing) {
		ConsoleUI.clearScreen();
		System.out.println("[DETALHES DO ANÚNCIO]");
		displayListingFields(listing, true);
		System.out.println("\n[1] Comprar");
		System.out.println("[0] Voltar");
		System.out.print("> ");

		if (!consoleInput.readTrimmedLine().equals("1")) {
			return;
		}
		
		ConsoleUI.clearScreen();
		System.out.println("[COMPRA]");
		System.out.println("\nRealizando a transação. Aguarde...");

		MarketplacePurchaseResult result = transactionService.purchase(listing);
		ConsoleUI.clearScreen();
		System.out.println("[COMPRA]");
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

			displayListingItemPage(availableItems, inventoryItems, firstIndex, lastIndex, currentPage, totalPages,
					feedbackMessage);

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

	private void displayListingItemPage(List<Item> availableItems, Map<Item, Integer> inventoryItems, int firstIndex,
			int lastIndex, int currentPage, int totalPages, String feedbackMessage) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading("Criar anúncio - Página " + (currentPage + 1) + "/" + totalPages);
		System.out.println();
		System.out.println("Selecione um item:");
		System.out.println(ItemMenuFormatter.formatItemTableHeader());

		for (int index = firstIndex; index < lastIndex; index++) {
			Item item = availableItems.get(index);
			System.out.println(ItemMenuFormatter.formatItemTableRow(index - firstIndex + 1, item.getName(),
					inventoryItems.get(item)));
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
		ConsoleUI.clearScreen();
		System.out.println("[NOVO ANÚNCIO]");
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
		
		ConsoleUI.clearScreen();
		System.out.println("[NOVO ANÚNCIO]");
		System.out.println("\nRegistrando anúncio. Aguarde...");

		MarketplaceListingCreationResult result = transactionService.createListing(item.getId(), quantity, unitPrice);
		
		ConsoleUI.clearScreen();
		System.out.println("[NOVO ANÚNCIO]");
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
		case API_UNAVAILABLE ->
			System.out.println("\nNão foi possível acessar o Posto de Trocas. Seus itens foram devolvidos.");
		case SERVER_REJECTED -> System.out.println("\nA API recusou o anúncio. Seus itens foram devolvidos.");
		}

		waitForReturn();
	}

	private void showListings() {
		browseListings(true);
	}

	private void showMySales() {
		browseListings(false);
	}

	private void browseListings(boolean showSeller) {
		int pageIndex = 0;

		while (true) {
			List<MarketplaceListing> listings;
			try {
				listings = showSeller ? transactionService.getActiveListings()
						: transactionService.getListingsForCurrentPlayer();
			} catch (IOException exception) {
				displayApiUnavailable();
				return;
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				displayApiUnavailable();
				return;
			}

			if (listings.isEmpty()) {
				ConsoleUI.clearScreen();
				System.out.println(showSeller ? "[ANÚNCIOS]" : "[MINHAS VENDAS]");
				System.out.println(
						showSeller ? "\nNenhum anúncio disponível." : "\nVocê ainda não criou nenhum anúncio.");
				waitForReturn();
				return;
			}

			int pageCount = 1 + (listings.size() - 1) / LISTINGS_PER_PAGE;
			pageIndex = Math.min(pageIndex, pageCount - 1);
			String feedback = "";

			while (true) {
				displayListingsPage(listings, showSeller, pageIndex, pageCount, feedback);
				String choice = consoleInput.readTrimmedLine();
				feedback = "";

				if (choice.equals("0")) {
					return;
				}
				if (choice.equalsIgnoreCase("A") && pageIndex > 0) {
					pageIndex--;
					continue;
				}
				if (choice.equalsIgnoreCase("P") && pageIndex + 1 < pageCount) {
					pageIndex++;
					continue;
				}

				int firstIndex = pageIndex * LISTINGS_PER_PAGE;
				int visibleCount = Math.min(LISTINGS_PER_PAGE, listings.size() - firstIndex);
				if (choice.length() != 1 || choice.charAt(0) < '1' || choice.charAt(0) > '0' + visibleCount) {
					feedback = "Opção inválida.";
					continue;
				}

				MarketplaceListing listing = listings.get(firstIndex + choice.charAt(0) - '1');
				if (showSeller) {
					showListingDetails(listing);
				} else {
					showMyListingDetails(listing);
				}
				break;
			}
		}
	}

	private void showMyListingDetails(MarketplaceListing listing) {
		ConsoleUI.clearScreen();
		System.out.println("[MEU ANÚNCIO]");
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
		
		ConsoleUI.clearScreen();
		System.out.println("[CANCELAR ANÚNCIO]");
		System.out.println("\nSolicitiando o cancelamento do anúncio. Aguarde...");

		MarketplaceListingCancellationResult result = transactionService.cancelListing(listing);

		System.out.println("\n[CANCELAR ANÚNCIO]");
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
		int pageIndex = 0;

		while (true) {
			ConsoleUI.clearScreen();
			System.out.println("[RECOMPENSAS]");
			System.out.println("\nCarregando recompensas. Aguarde...");

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

			if (rewards.isEmpty()) {
				ConsoleUI.clearScreen();
				System.out.println("[RECOMPENSAS]");
				System.out.println("\nNão há recompensas disponíveis.");
				waitForReturn();
				return;
			}

			int pageCount = 1 + (rewards.size() - 1) / REWARDS_PER_PAGE;
			pageIndex = Math.min(pageIndex, pageCount - 1);
			String feedback = "";

			while (true) {
				displayRewardsPage(rewards, pageIndex, pageCount, feedback);
				String choice = consoleInput.readTrimmedLine();
				feedback = "";

				if (choice.equals("0")) {
					return;
				}
				if (choice.equalsIgnoreCase("T")) {
				    claimAllRewards(rewards);
				    break;
				}
				if (choice.equalsIgnoreCase("A") && pageIndex > 0) {
					pageIndex--;
					continue;
				}
				if (choice.equalsIgnoreCase("P") && pageIndex + 1 < pageCount) {
					pageIndex++;
					continue;
				}

				int firstIndex = pageIndex * REWARDS_PER_PAGE;
				int visibleCount = Math.min(REWARDS_PER_PAGE, rewards.size() - firstIndex);
				if (choice.length() != 1 || choice.charAt(0) < '1' || choice.charAt(0) > '0' + visibleCount) {
					feedback = "Opção inválida.";
					continue;
				}

				int selectedSlot = choice.charAt(0) - '1';
				claimReward(rewards.get(firstIndex + selectedSlot));
				break;
			}
		}
	}

private void displayRewardsPage(
	        List<PendingReward> rewards, int pageIndex, int pageCount, String feedback) {
	    int firstIndex = pageIndex * REWARDS_PER_PAGE;

	    ConsoleUI.clearScreen();
	    System.out.printf("[RECOMPENSAS - PÁGINA %d/%d]%n%n", pageIndex + 1, pageCount);
	    System.out.println("Selecione uma recompensa:");
	    System.out.printf(REWARD_ROW_FORMAT, "", "Item vendido", "Valor");

	    for (int slot = 0; slot < REWARDS_PER_PAGE; slot++) {
	        int index = firstIndex + slot;
	        if (index >= rewards.size()) {
	            System.out.printf(REWARD_ROW_FORMAT, "", "--", "--");
	            continue;
	        }

	        PendingReward reward = rewards.get(index);
	        String quantitySuffix = " x" + reward.quantity();
	        String itemName = abbreviateColumn(
	                getItemName(reward.itemId()), REWARD_ITEM_WIDTH - quantitySuffix.length());

	        System.out.printf(REWARD_ROW_FORMAT,
	                "[" + (slot + 1) + "]",
	                itemName + quantitySuffix,
	                GameCurrency.format(reward.amount()));
	    }

	    displayPageFooter(pageIndex, pageCount, feedback, "[T] Resgatar tudo     [0] Voltar");
	}

	private void claimReward(PendingReward reward) {
		ConsoleUI.clearScreen();
		System.out.println("[RECOMPENSA]");
		System.out.println("\nValor: " + GameCurrency.format(reward.amount()));
		System.out.println("Transação: " + reward.transactionId());
		System.out.println("\n[1] Resgatar");
		System.out.println("[0] Voltar");
		System.out.print("> ");

		if (!consoleInput.readTrimmedLine().equals("1")) {
			return;
		}

		ConsoleUI.clearScreen();
		System.out.println("[RECOMPENSA]");
		System.out.println("\nResgatando recompensa. Aguarde...");

		MarketplaceRewardClaimResult result = transactionService.claimReward(reward);

		ConsoleUI.clearScreen();
		System.out.println("[RECOMPENSA]");

		System.out.println("\n" + getRewardClaimMessage(result));
		if (result == MarketplaceRewardClaimResult.SUCCESS) {
		    saveCompletedTransaction();
		}

		waitForReturn();
	}

	private void claimAllRewards(List<PendingReward> rewards) {
		long total = rewards.stream().mapToLong(PendingReward::amount).sum();

		ConsoleUI.clearScreen();
		System.out.println("[RESGATAR TUDO]");
		System.out.println("\nRecompensas: " + rewards.size());
		System.out.println("Valor total: " + total + " " + GameCurrency.getName());
		System.out.println("\n[1] Confirmar     [0] Voltar");
		System.out.print("> ");

		String choice = consoleInput.readTrimmedLine();
		while (!choice.equals("1") && !choice.equals("0")) {
			ConsoleUI.clearScreen();
			System.out.println("[RESGATAR TUDO]");
			System.out.println("\nRecompensas: " + rewards.size());
			System.out.println("Valor total: " + total + " " + GameCurrency.getName());
			System.out.println("\nOpção inválida.");
			System.out.println("[1] Confirmar     [0] Voltar");
			System.out.print("> ");
			choice = consoleInput.readTrimmedLine();
		}

		if (choice.equals("0")) {
			return;
		}

		int claimedCount = 0;
		for (PendingReward reward : rewards) {
			ConsoleUI.clearScreen();
			System.out.println("[RESGATANDO RECOMPENSAS]");
			System.out.printf("%nResgatando %d de %d. Aguarde...%n", claimedCount + 1, rewards.size());

			MarketplaceRewardClaimResult result = transactionService.claimReward(reward);
			if (result != MarketplaceRewardClaimResult.SUCCESS) {
				ConsoleUI.clearScreen();
				System.out.println("[RESGATE INTERROMPIDO]");
				System.out.println("\n" + getRewardClaimMessage(result));
				System.out.printf("Resgates confirmados: %d de %d.%n", claimedCount, rewards.size());
				waitForReturn();
				return;
			}

			saveCompletedTransaction();
			claimedCount++;
		}

		ConsoleUI.clearScreen();
		System.out.println("[RESGATE CONCLUÍDO]");
		System.out.printf("%nRecompensas resgatadas: %d.%n", claimedCount);
		System.out.println("Valor recebido: " + total + " " + GameCurrency.getName());
		System.out.println("Campanha salva automaticamente.");
		waitForReturn();
	}

	private static String abbreviateColumn(String value, int width) {
		if (value.length() <= width) {
			return value;
		}

		int end = width - 3;
		if (Character.isHighSurrogate(value.charAt(end - 1))) {
			end--;
		}

		return value.substring(0, end).stripTrailing() + "...";
	}

	private void displayListingsPage(List<MarketplaceListing> listings, boolean showSeller, int pageIndex,
			int pageCount, String feedback) {

		String title = showSeller ? "ANÚNCIOS" : "MINHAS VENDAS";
		int firstIndex = pageIndex * LISTINGS_PER_PAGE;

		ConsoleUI.clearScreen();
		System.out.printf("[%s - PÁGINA %d/%d]%n%n", title, pageIndex + 1, pageCount);
		System.out.println("Selecione um anúncio:");
		System.out.printf(LISTING_ROW_FORMAT, "", "Item", "Qtd.", "Preço total", showSeller ? "Vendedor" : "Status");

		for (int slot = 0; slot < LISTINGS_PER_PAGE; slot++) {
			int index = firstIndex + slot;
			if (index >= listings.size()) {
				System.out.printf(LISTING_ROW_FORMAT, "", "--", "--", "--", "--");
				continue;
			}

			MarketplaceListing listing = listings.get(index);
			String itemName = abbreviateColumn(getItemName(listing.itemId()), LISTING_ITEM_WIDTH);
			String owner = showSeller ? abbreviateColumn(listing.sellerName(), LISTING_OWNER_WIDTH)
					: getStatusLabel(listing.status());

			System.out.printf(LISTING_ROW_FORMAT, "[" + (slot + 1) + "]", itemName, "x" + listing.quantity(),
					GameCurrency.format(listing.totalPrice()), owner);
		}

		displayPageFooter(pageIndex, pageCount, feedback, "[0] Voltar");
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
		ConsoleUI.clearScreen();
		System.out.println("Não foi possível acessar o Posto de Trocas.");
		System.out.println("Aguarde 1 minuto e tente novamente.");
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

	private void displayPageFooter(int pageIndex, int pageCount, String feedback, String actions) {
	    String previous = pageIndex > 0 ? "[A] Anterior" : "";
	    String next = pageIndex + 1 < pageCount ? "[P] Próxima" : "";

	    System.out.println((previous + "  " + next).strip());
	    System.out.println(feedback);
	    System.out.println(actions);
	    System.out.print("> ");
	}

	private static String getRewardClaimMessage(MarketplaceRewardClaimResult result) {
	    return switch (result) {
	        case SUCCESS -> "Recompensa resgatada com sucesso!";
	        case INVALID_REWARD -> "A recompensa é inválida.";
	        case REWARD_NOT_PENDING -> "Esta recompensa já foi resgatada.";
	        case NOT_REWARD_OWNER -> "Esta recompensa pertence a outro jogador.";
	        case GOLD_CAPACITY_EXCEEDED -> "Você não consegue carregar todo esse GOLD.";
	        case API_UNAVAILABLE -> "Não foi possível acessar o Posto de Trocas.";
	        case SERVER_REJECTED -> "A API recusou o resgate.";
	    };
	}

}