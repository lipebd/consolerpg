package rpg.marketplace.api;

import java.io.IOException;
import java.util.List;

public class MarketplaceApiConnectionTest {

    public static void main(String[] args) {
        MarketplaceApiClient apiClient =
                new MarketplaceApiClient("http://localhost:8080");

        try {
            List<MarketplaceListingResponse> listings =
                    apiClient.getActiveListings();

            System.out.println("[MARKETPLACE API CONECTADA]");
            System.out.println(
                    "Anúncios encontrados: " + listings.size()
            );

            for (MarketplaceListingResponse listing : listings) {
                System.out.println();
                System.out.println("ID: " + listing.id());
                System.out.println("Vendedor: " + listing.sellerId());
                System.out.println("Item: " + listing.itemId());
                System.out.println("Quantidade: " + listing.quantity());
                System.out.println(
                        "Preço unitário: " + listing.unitPrice()
                );
                System.out.println(
                        "Preço total: " + listing.totalPrice()
                );
                System.out.println("Status: " + listing.status());
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            System.out.println(
                    "A comunicação com a API foi interrompida."
            );

        } catch (IOException exception) {
            System.out.println(
                    "Não foi possível acessar a Marketplace API."
            );

            System.out.println(
                    "Detalhes: " + exception.getMessage()
            );
        }
    }
}