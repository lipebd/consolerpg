package rpg.ui;

import java.util.Objects;
import java.util.Scanner;
import java.util.List;
import rpg.marketplace.MarketplaceListing;

// Displays the marketplace main menu.
public final class MarketplaceMenu {

	private final Scanner scanner;
	private final List<MarketplaceListing> listings;

	public MarketplaceMenu(Scanner scanner, List<MarketplaceListing> listings) {
		this.scanner = Objects.requireNonNull(
				scanner,
				"Scanner cannot be null."
				);
		this.listings = Objects.requireNonNull(
	            listings,
	            "Listings cannot be null."
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

			if (listings.isEmpty()) {
				System.out.println("\nNenhum anúncio disponível.");
				waitForReturn();
				return;
			}

			for (int i = 0; i < listings.size(); i++) {
				MarketplaceListing listing = listings.get(i);

				System.out.println();
				System.out.println("[" + (i + 1) + "] " + listing.getItemId());
				System.out.println("Quantidade: " + listing.getQuantity());
				System.out.println("Preço unitário: " + listing.getUnitPrice());
				System.out.println("Vendedor: " + listing.getSellerId());
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
			showPlaceholder(
					"RECOMPENSAS",
					"Não há recompensas disponíveis no momento."
					);
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