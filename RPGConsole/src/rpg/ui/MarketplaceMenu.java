package rpg.ui;

import java.util.Objects;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;

import rpg.marketplace.MarketplaceListing;
import rpg.marketplace.MarketplaceTransactionService;
import rpg.marketplace.MarketplaceListingCreationResult;
import rpg.marketplace.MarketplaceListingCancellationResult;
import rpg.marketplace.MarketplacePurchaseResult;
import rpg.marketplace.PendingReward;
import rpg.marketplace.ListingStatus;
import rpg.marketplace.MarketplaceRewardClaimResult;
import rpg.items.ItemCatalog;
import rpg.items.Item;

// Displays the marketplace main menu.
public final class MarketplaceMenu {

	private final Scanner scanner;
	private final ItemCatalog itemCatalog;
	private final MarketplaceTransactionService transactionService;

	public MarketplaceMenu(Scanner scanner,
			ItemCatalog itemCatalog,
			MarketplaceTransactionService transactionService) {

		this.scanner = Objects.requireNonNull(
				scanner,
				"Scanner cannot be null."
				);
		this.itemCatalog = Objects.requireNonNull(
				itemCatalog,
				"Item catalog cannot be null."
				);
		this.transactionService = Objects.requireNonNull(
				transactionService,
				"Transaction service cannot be null."
				);
	}


	public void open() {
		while (true) {
			displayMenu();

			String choice = scanner.nextLine().trim();

			switch (choice) {
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

	private void displayMenu() {
		System.out.println("\n[POSTO DE TROCAS]");

		System.out.println();
		System.out.println("Compre e venda itens com outros jogadores.");

		System.out.println();
		System.out.println("[1] Ver anúncios");
		System.out.println("[2] Criar anúncio");
		System.out.println("[3] Minhas vendas");
		System.out.println("[4] Recompensas");

		System.out.println();
		System.out.println("[0] Voltar");

		System.out.print("> ");
	}

	private void showListings() {
	    System.out.println("\n[ANÚNCIOS]");

	    List<MarketplaceListing> activeListings;

	    try {
	        activeListings =
	                transactionService.getActiveListings();

	    } catch (InterruptedException exception) {
	        Thread.currentThread().interrupt();

	        System.out.println();
	        System.out.println(
	                "A atualização dos anúncios foi interrompida."
	        );

	        waitForReturn();
	        return;

	    } catch (IOException exception) {
	        System.out.println();
	        System.out.println(
	                "Não foi possível acessar o posto de trocas."
	        );
	        System.out.println(
	                "Verifique se a Marketplace API está ligada."
	        );

	        waitForReturn();
	        return;
	    }

		if (activeListings.isEmpty()) {
			System.out.println();
			System.out.println("Nenhum anúncio disponível.");

			waitForReturn();
			return;
		}

		for (int i = 0; i < activeListings.size(); i++) {
			MarketplaceListing listing = activeListings.get(i);

			Item item = itemCatalog.getById(listing.getItemId());

			int totalPrice =
					listing.getUnitPrice() * listing.getQuantity();

			System.out.println();
			System.out.println("[" + (i + 1) + "] " + item.getName());
			System.out.println("Quantidade: " + listing.getQuantity());
			System.out.println("Preço unitário: " + listing.getUnitPrice());
			System.out.println("Total: " + totalPrice);
			System.out.println("Vendedor: " + listing.getSellerId());
		}

		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");

		String input = scanner.nextLine().trim();

		if (input.equals("0")) {
			return;
		}

		try {
			int option = Integer.parseInt(input);

			if (option < 1 || option > activeListings.size()) {
				System.out.println("\nOpção inválida.");
				waitForReturn();
				return;
			}

			MarketplaceListing selectedListing =
					activeListings.get(option - 1);

			showListingDetails(selectedListing);

		} catch (NumberFormatException exception) {
			System.out.println("\nOpção inválida.");
			waitForReturn();
		}
	}

	private String getListingStatusLabel(
			ListingStatus status) {

		return switch (status) {
		case ACTIVE -> "Ativo";
		case SOLD -> "Vendido";
		case CANCELLED -> "Cancelado";
		};
	}

	private void showListingDetails(MarketplaceListing listing) {
		Item item = itemCatalog.getById(listing.getItemId());

		int totalPrice =
				listing.getUnitPrice() * listing.getQuantity();

		while (true) {
			System.out.println("\n[DETALHES DO ANÚNCIO]");

			System.out.println();
			System.out.println("Item: " + item.getName());
			System.out.println("Quantidade: " + listing.getQuantity());
			System.out.println("Preço unitário: " + listing.getUnitPrice());
			System.out.println("Total: " + totalPrice);
			System.out.println("Vendedor: " + listing.getSellerId());

			System.out.println();
			System.out.println("[1] Comprar");
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			switch (choice) {
			case "1" -> {
				purchaseListing(listing);
				return;
			}

			case "0" -> {
				return;
			}

			default -> System.out.println("\nOpção inválida.");
			}
		}
	}

	private void purchaseListing(MarketplaceListing listing) {
		MarketplacePurchaseResult result =
				transactionService.purchase(listing);

		System.out.println("\n[COMPRA]");

		switch (result) {

		case SUCCESS ->
		System.out.println("\nCompra realizada com sucesso!");

		case INVALID_LISTING ->
		System.out.println("\nAnúncio inválido.");

		case LISTING_NOT_ACTIVE ->
		System.out.println("\nEste anúncio não está mais disponível.");

		case CANNOT_BUY_OWN_LISTING ->
		System.out.println("\nVocê não pode comprar seu próprio anúncio.");

		case INSUFFICIENT_GOLD ->
		System.out.println("\nVocê não possui moedas suficientes.");

		case INVENTORY_ERROR ->
		System.out.println("\nNão foi possível adicionar o item ao inventário.");
		}

		waitForReturn();
	}

	private void createListing() {
		System.out.println("\n[CRIAR ANÚNCIO]");

		Map<Item, Integer> inventoryItems =
				transactionService.getInventoryItemsForListing();

		if (inventoryItems.isEmpty()) {
			System.out.println();
			System.out.println(
					"Você não possui itens disponíveis para venda."
					);

			waitForReturn();
			return;
		}

		List<Item> availableItems =
				new ArrayList<>(inventoryItems.keySet());

		for (int i = 0; i < availableItems.size(); i++) {
			Item item = availableItems.get(i);
			int quantity = inventoryItems.get(item);

			System.out.println();
			System.out.println(
					"[" + (i + 1) + "] " + item.getName()
					);
			System.out.println(
					"Quantidade disponível: " + quantity
					);
		}

		while (true) {
			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String input = scanner.nextLine().trim();

			if (input.equals("0")) {
				return;
			}

			int option;

			try {
				option = Integer.parseInt(input);
			} catch (NumberFormatException exception) {
				System.out.println("\nOpção inválida.");
				continue;
			}

			if (option < 1 || option > availableItems.size()) {
				System.out.println("\nOpção inválida.");
				continue;
			}

			Item selectedItem =
					availableItems.get(option - 1);

			int availableQuantity =
					inventoryItems.get(selectedItem);

			showListingCreationForm(
					selectedItem,
					availableQuantity
					);

			return;
		}
	}

	private void showListingCreationForm(
			Item item,
			int availableQuantity) {

		System.out.println("\n[NOVO ANÚNCIO]");

		System.out.println();
		System.out.println("Item: " + item.getName());
		System.out.println(
				"Quantidade disponível: " + availableQuantity
				);

		System.out.println();
		System.out.print("Quantidade para venda (0 para cancelar): ");

		String quantityInput = scanner.nextLine().trim();

		if (quantityInput.equals("0")) {
			return;
		}

		int quantity;

		try {
			quantity = Integer.parseInt(quantityInput);
		} catch (NumberFormatException exception) {
			System.out.println("\nQuantidade inválida.");
			waitForReturn();
			return;
		}

		System.out.print("Preço por unidade (0 para cancelar): ");

		String priceInput = scanner.nextLine().trim();

		if (priceInput.equals("0")) {
			return;
		}

		int unitPrice;

		try {
			unitPrice = Integer.parseInt(priceInput);
		} catch (NumberFormatException exception) {
			System.out.println("\nPreço inválido.");
			waitForReturn();
			return;
		}

		MarketplaceListingCreationResult result =
				transactionService.createListing(
						item.getId(),
						quantity,
						unitPrice
						);

		System.out.println("\n[CRIAÇÃO DO ANÚNCIO]");

		switch (result) {
		case SUCCESS ->
		System.out.println(
				"\nAnúncio criado com sucesso!"
				);

		case INVALID_ITEM ->
		System.out.println(
				"\nO item selecionado é inválido."
				);

		case INVALID_QUANTITY ->
		System.out.println(
				"\nA quantidade deve ser maior que zero."
				);

		case INVALID_UNIT_PRICE ->
		System.out.println(
				"\nO preço deve ser maior que zero."
				);

		case INSUFFICIENT_ITEMS ->
		System.out.println(
				"\nVocê não possui essa quantidade de itens."
				);

		case INVENTORY_ERROR ->
		System.out.println(
				"\nNão foi possível retirar os itens do inventário."
				);

		case API_UNAVAILABLE ->
		System.out.println(
				"\nNão foi possível acessar o posto de trocas."
						+ "\nOs itens foram devolvidos ao inventário."
				);
		}

		waitForReturn();
	}

	private void showMySales() {
		System.out.println("\n[MINHAS VENDAS]");

		List<MarketplaceListing> playerListings =
				transactionService.getListingsForCurrentPlayer();

		if (playerListings.isEmpty()) {
			System.out.println();
			System.out.println("Você ainda não criou nenhum anúncio.");

			waitForReturn();
			return;
		}

		for (int i = 0; i < playerListings.size(); i++) {
			MarketplaceListing listing =
					playerListings.get(i);

			Item item = itemCatalog.getById(
					listing.getItemId()
					);

			int totalPrice =
					listing.getUnitPrice() * listing.getQuantity();

			System.out.println();
			System.out.println(
					"[" + (i + 1) + "] " + item.getName()
					);
			System.out.println(
					"Quantidade: " + listing.getQuantity()
					);
			System.out.println(
					"Preço unitário: " + listing.getUnitPrice()
					);
			System.out.println("Total: " + totalPrice);
			System.out.println(
					"Status: "
							+ getListingStatusLabel(listing.getStatus())
					);
		}

		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");

		String input = scanner.nextLine().trim();

		if (input.equals("0")) {
			return;
		}

		try {
			int option = Integer.parseInt(input);

			if (option < 1 || option > playerListings.size()) {
				System.out.println("\nOpção inválida.");
				waitForReturn();
				return;
			}

			MarketplaceListing selectedListing =
					playerListings.get(option - 1);

			showMyListingDetails(selectedListing);

		} catch (NumberFormatException exception) {
			System.out.println("\nOpção inválida.");
			waitForReturn();
		}
	}

	private void showMyListingDetails(
			MarketplaceListing listing) {

		Item item = itemCatalog.getById(
				listing.getItemId()
				);

		int totalPrice =
				listing.getUnitPrice() * listing.getQuantity();

		while (true) {
			System.out.println("\n[MEU ANÚNCIO]");

			System.out.println();
			System.out.println("Item: " + item.getName());
			System.out.println(
					"Quantidade: " + listing.getQuantity()
					);
			System.out.println(
					"Preço unitário: " + listing.getUnitPrice()
					);
			System.out.println("Total: " + totalPrice);
			System.out.println(
					"Status: "
							+ getListingStatusLabel(listing.getStatus())
					);

			if (listing.getStatus() == ListingStatus.ACTIVE) {
				System.out.println();
				System.out.println("[1] Cancelar anúncio");
			}

			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine().trim();

			switch (choice) {
			case "1" -> {
				if (listing.getStatus() != ListingStatus.ACTIVE) {
					System.out.println("\nOpção inválida.");
					continue;
				}

				cancelMyListing(listing);
				return;
			}

			case "0" -> {
				return;
			}

			default ->
			System.out.println("\nOpção inválida.");
			}
		}
	}

	private void cancelMyListing(
			MarketplaceListing listing) {

		MarketplaceListingCancellationResult result =
				transactionService.cancelListing(listing);

		System.out.println("\n[CANCELAMENTO]");

		switch (result) {
		case SUCCESS ->
		System.out.println(
				"\nAnúncio cancelado com sucesso!"
				);

		case INVALID_LISTING ->
		System.out.println(
				"\nO anúncio selecionado é inválido."
				);

		case NOT_LISTING_OWNER ->
		System.out.println(
				"\nEste anúncio pertence a outro jogador."
				);

		case LISTING_NOT_ACTIVE ->
		System.out.println(
				"\nEste anúncio não está mais ativo."
				);

		case INVENTORY_ERROR ->
		System.out.println(
				"\nNão foi possível devolver os itens ao inventário."
				);

		case API_UNAVAILABLE ->
		System.out.println(
				"\nNão foi possível acessar o posto de trocas."
						+ "\nO anúncio continua ativo e nenhum item foi devolvido."
				);
		}

		waitForReturn();
	}

	private void showRewards() {
		System.out.println("\n[RECOMPENSAS]");

		List<PendingReward> availableRewards =
				transactionService.getPendingRewardsForCurrentPlayer();

		if (availableRewards.isEmpty()) {
			System.out.println();
			System.out.println("Não há recompensas disponíveis.");

			waitForReturn();
			return;
		}

		for (int i = 0; i < availableRewards.size(); i++) {
			PendingReward reward = availableRewards.get(i);

			System.out.println();
			System.out.println(
					"[" + (i + 1) + "] "
							+ reward.getAmount()
							+ " moedas"
					);
		}

		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");

		String input = scanner.nextLine().trim();

		if (input.equals("0")) {
			return;
		}

		try {
			int option = Integer.parseInt(input);

			if (option < 1 || option > availableRewards.size()) {
				System.out.println("\nOpção inválida.");
				waitForReturn();
				return;
			}

			PendingReward selectedReward =
					availableRewards.get(option - 1);

			showRewardDetails(selectedReward);

		} catch (NumberFormatException exception) {
			System.out.println("\nOpção inválida.");
			waitForReturn();
		}
	}

	private void showRewardDetails(PendingReward reward) {
		System.out.println("\n[RECOMPENSA]");

		System.out.println();
		System.out.println("Valor: " + reward.getAmount() + " moedas");
		System.out.println(
				"Transação: " + reward.getTransactionId()
				);

		System.out.println();
		System.out.println("[1] Resgatar");
		System.out.println("[0] Voltar");
		System.out.print("> ");

		String choice = scanner.nextLine().trim();

		switch (choice) {
		case "1" -> claimReward(reward);

		case "0" -> {
			return;
		}

		default -> {
			System.out.println("\nOpção inválida.");
			waitForReturn();
		}
		}
	}

	private void claimReward(PendingReward reward) {
		MarketplaceRewardClaimResult result =
				transactionService.claimReward(reward);

		System.out.println("\n[RESGATE]");

		switch (result) {
		case SUCCESS -> {
			System.out.println();
			System.out.println(
					"Recompensa resgatada com sucesso!"
					);
			System.out.println(
					"+" + reward.getAmount() + " moedas"
					);
		}

		case INVALID_REWARD ->
		System.out.println("\nRecompensa inválida.");

		case REWARD_NOT_PENDING ->
		System.out.println(
				"\nEsta recompensa já foi resgatada."
				);

		case NOT_REWARD_OWNER ->
		System.out.println(
				"\nEsta recompensa pertence a outro jogador."
				);

		case GOLD_CAPACITY_EXCEEDED ->
		System.out.println(
				"\nNão foi possível adicionar as moedas."
				);
		}

		waitForReturn();
	}

	private void waitForReturn() {
		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");

		while (!scanner.nextLine().trim().equals("0")) {
			System.out.println("Digite 0 para voltar.");
			System.out.print("> ");
		}
	}
}