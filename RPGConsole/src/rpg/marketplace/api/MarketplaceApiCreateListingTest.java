package rpg.marketplace.api;

import java.io.IOException;

public class MarketplaceApiCreateListingTest {

	public static void main(String[] args) {
		MarketplaceApiClient apiClient =
				new MarketplaceApiClient("http://localhost:8080");

		CreateListingRequest request =
				new CreateListingRequest(
						"rpg-test",
						"meruma",
						2,
						7
						);

		try {
			MarketplaceListingResponse createdListing =
					apiClient.createListing(request);

			System.out.println(
					"[ANÚNCIO CRIADO NA API]"
					);

			System.out.println(
					"ID: " + createdListing.id()
					);

			System.out.println(
					"Vendedor: " + createdListing.sellerId()
					);

			System.out.println(
					"Item: " + createdListing.itemId()
					);

			System.out.println(
					"Quantidade: " + createdListing.quantity()
					);

			System.out.println(
					"Preço unitário: " + createdListing.unitPrice()
					);

			System.out.println(
					"Preço total: " + createdListing.totalPrice()
					);

			System.out.println(
					"Status: " + createdListing.status()
					);

		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();

			System.out.println(
					"A criação do anúncio foi interrompida."
					);

		} catch (IOException exception) {
			System.out.println(
					"Não foi possível criar o anúncio na API."
					);

			System.out.println(
					"Detalhes: " + exception.getMessage()
					);
		}
	}

}
