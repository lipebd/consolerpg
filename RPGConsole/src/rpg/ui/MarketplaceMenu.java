package rpg.ui;

import java.util.Objects;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import rpg.marketplace.MarketplaceListing;
import rpg.marketplace.MarketplaceTransactionService;
import rpg.marketplace.MarketplacePurchaseResult;
import rpg.marketplace.PendingReward;
import rpg.marketplace.ListingStatus;
import rpg.marketplace.MarketplaceRewardClaimResult;
import rpg.items.ItemCatalog;
import rpg.items.Item;

// Displays the marketplace main menu.
public final class MarketplaceMenu {

	private final Scanner scanner;
	private final List<MarketplaceListing> listings;
	private final ItemCatalog itemCatalog;
	private final MarketplaceTransactionService transactionService;

	public MarketplaceMenu(Scanner scanner,
			List<MarketplaceListing> listings,
			ItemCatalog itemCatalog,
			MarketplaceTransactionService transactionService) {
		
		this.scanner = Objects.requireNonNull(
				scanner,
				"Scanner cannot be null."
				);
		this.listings = Objects.requireNonNull(
				listings,
				"Listings cannot be null."
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
	
	private List<MarketplaceListing> getActiveListings() {
	    List<MarketplaceListing> activeListings = new ArrayList<>();

	    for (MarketplaceListing listing : listings) {
	        if (listing.getStatus() == ListingStatus.ACTIVE) {
	            activeListings.add(listing);
	        }
	    }

	    return activeListings;
	}

	private void showListings() {
	    System.out.println("\n[ANÚNCIOS]");

	    List<MarketplaceListing> activeListings = getActiveListings();

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
		showPlaceholder(
				"CRIAR ANÚNCIO",
				"A criação de anúncios ainda não está disponível."
				);
	}

	private void showMySales() {
		showPlaceholder(
				"MINHAS VENDAS",
				"O histórico de vendas ainda não está disponível."
				);
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

	private void showPlaceholder(String title, String message) {
		System.out.println("\n[" + title + "]");

		System.out.println();
		System.out.println(message);

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