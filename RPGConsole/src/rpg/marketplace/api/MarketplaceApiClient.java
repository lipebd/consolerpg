package rpg.marketplace.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import com.google.gson.Gson;

public class MarketplaceApiClient {

	private final HttpClient httpClient;
	private final Gson gson;
	private final String baseUrl;

	public MarketplaceApiClient(String baseUrl) {
		this.baseUrl = baseUrl;

		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();

		this.gson = new Gson();
	}

	public List<MarketplaceListingResponse> getActiveListings()
			throws IOException, InterruptedException {

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/api/listings"))
				.timeout(Duration.ofSeconds(5))
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(
				request,
				HttpResponse.BodyHandlers.ofString()
				);

		if (response.statusCode() != 200) {
			throw new IOException(
					"Marketplace API returned status "
							+ response.statusCode()
					);
		}

		MarketplaceListingResponse[] listings =
				gson.fromJson(
						response.body(),
						MarketplaceListingResponse[].class
						);

		return List.of(listings);
	}

	public MarketplaceListingResponse createListing(
			CreateListingRequest createRequest
			) throws IOException, InterruptedException {

		String requestJson = gson.toJson(createRequest);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/api/listings"))
				.timeout(Duration.ofSeconds(5))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(requestJson))
				.build();

		HttpResponse<String> response = httpClient.send(
				request,
				HttpResponse.BodyHandlers.ofString()
				);

		if (response.statusCode() != 201) {
			throw new IOException(
					"Marketplace API returned status "
							+ response.statusCode()
							+ ": "
							+ response.body()
					);
		}

		return gson.fromJson(
				response.body(),
				MarketplaceListingResponse.class
				);
	}

	public MarketplaceListingResponse cancelListing(
			String listingId,
			String sellerId
			) throws IOException, InterruptedException {

		CancelListingRequest cancelRequest =
				new CancelListingRequest(sellerId);

		String requestJson = gson.toJson(cancelRequest);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(
						URI.create(
								baseUrl
								+ "/api/listings/"
								+ listingId
								+ "/cancel"
								)
						)
				.timeout(Duration.ofSeconds(5))
				.header("Content-Type", "application/json")
				.POST(
						HttpRequest.BodyPublishers.ofString(
								requestJson
								)
						)
				.build();

		HttpResponse<String> response = httpClient.send(
				request,
				HttpResponse.BodyHandlers.ofString()
				);

		if (response.statusCode() != 200) {
			throw new MarketplaceApiException(
					response.statusCode(),
					response.body()
					);
		}

		return gson.fromJson(
				response.body(),
				MarketplaceListingResponse.class
				);
	}
}