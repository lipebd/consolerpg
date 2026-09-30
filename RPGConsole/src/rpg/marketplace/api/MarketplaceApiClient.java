package rpg.marketplace.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rpg.marketplace.ListingStatus;
import rpg.marketplace.MarketplaceListing;
import rpg.marketplace.PendingReward;
import rpg.marketplace.RewardStatus;

// Communicates with the marketplace API using only Java standard-library features.
public final class MarketplaceApiClient {

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

	private final HttpClient httpClient;
	private final String baseUrl;

	public MarketplaceApiClient(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			throw new IllegalArgumentException("Marketplace API base URL cannot be empty.");
		}

		this.baseUrl = baseUrl.endsWith("/")
				? baseUrl.substring(0, baseUrl.length() - 1)
				: baseUrl;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(REQUEST_TIMEOUT)
				.build();
	}

	public List<MarketplaceListing> getActiveListings()
			throws IOException, InterruptedException {
		String responseBody = send("GET", "/api/listings", null, 200);
		return parseListings(responseBody);
	}

	public List<MarketplaceListing> getListingsForSeller(String sellerId)
			throws IOException, InterruptedException {
		String query = "?sellerId=" + encode(sellerId);
		String responseBody = send("GET", "/api/listings" + query, null, 200);
		return parseListings(responseBody);
	}

	public MarketplaceListing createListing(
			String sellerId,
			String itemId,
			int quantity,
			int unitPrice) throws IOException, InterruptedException {
		Map<String, Object> fields = new LinkedHashMap<>();
		fields.put("sellerId", sellerId);
		fields.put("itemId", itemId);
		fields.put("quantity", quantity);
		fields.put("unitPrice", unitPrice);

		String responseBody = send("POST", "/api/listings", JsonSupport.object(fields), 201);
		return parseListing(JsonSupport.parseObject(responseBody));
	}

	public MarketplaceListing purchaseListing(String listingId, String buyerId)
			throws IOException, InterruptedException {
		String requestBody = JsonSupport.object(Map.of("buyerId", buyerId));
		String path = "/api/listings/" + encode(listingId) + "/purchase";
		String responseBody = send("POST", path, requestBody, 200);
		return parseListing(JsonSupport.parseObject(responseBody));
	}

	public MarketplaceListing cancelListing(String listingId, String sellerId)
			throws IOException, InterruptedException {
		String requestBody = JsonSupport.object(Map.of("sellerId", sellerId));
		String path = "/api/listings/" + encode(listingId) + "/cancel";
		String responseBody = send("POST", path, requestBody, 200);
		return parseListing(JsonSupport.parseObject(responseBody));
	}

	public List<PendingReward> getPendingRewards(String sellerId)
			throws IOException, InterruptedException {
		String path = "/api/rewards?sellerId=" + encode(sellerId);
		String responseBody = send("GET", path, null, 200);
		return parseRewards(responseBody);
	}

	public PendingReward claimReward(String rewardId, String sellerId)
			throws IOException, InterruptedException {
		String requestBody = JsonSupport.object(Map.of("sellerId", sellerId));
		String path = "/api/rewards/" + encode(rewardId) + "/claim";
		String responseBody = send("POST", path, requestBody, 200);
		return parseReward(JsonSupport.parseObject(responseBody));
	}

	private String send(String method, String path, String requestBody, int expectedStatus)
			throws IOException, InterruptedException {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + path))
				.timeout(REQUEST_TIMEOUT)
				.header("Accept", "application/json");

		if (requestBody == null) {
			requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
		} else {
			requestBuilder
					.header("Content-Type", "application/json; charset=UTF-8")
					.method(method, HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));
		}

		HttpResponse<String> response = httpClient.send(
				requestBuilder.build(),
				HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

		if (response.statusCode() != expectedStatus) {
			throw new MarketplaceApiException(response.statusCode(), response.body());
		}

		return response.body();
	}

	private static List<MarketplaceListing> parseListings(String json) throws IOException {
		try {
			List<MarketplaceListing> listings = new ArrayList<>();

			for (Map<String, Object> object : JsonSupport.parseObjectArray(json)) {
				listings.add(parseListing(object));
			}

			return List.copyOf(listings);
		} catch (IllegalArgumentException | ArithmeticException exception) {
			throw new IOException("Marketplace API returned invalid listing data.", exception);
		}
	}

	private static MarketplaceListing parseListing(Map<String, Object> object) {
		return new MarketplaceListing(
				JsonSupport.requiredString(object, "id"),
				JsonSupport.requiredString(object, "sellerId"),
				JsonSupport.requiredString(object, "itemId"),
				JsonSupport.requiredInt(object, "quantity"),
				JsonSupport.requiredInt(object, "unitPrice"),
				JsonSupport.requiredInt(object, "totalPrice"),
				ListingStatus.valueOf(JsonSupport.requiredString(object, "status")));
	}

	private static List<PendingReward> parseRewards(String json) throws IOException {
		try {
			List<PendingReward> rewards = new ArrayList<>();

			for (Map<String, Object> object : JsonSupport.parseObjectArray(json)) {
				rewards.add(parseReward(object));
			}

			return List.copyOf(rewards);
		} catch (IllegalArgumentException | ArithmeticException exception) {
			throw new IOException("Marketplace API returned invalid reward data.", exception);
		}
	}

	private static PendingReward parseReward(Map<String, Object> object) {
		return new PendingReward(
				JsonSupport.requiredString(object, "id"),
				JsonSupport.requiredString(object, "transactionId"),
				JsonSupport.requiredString(object, "sellerId"),
				JsonSupport.requiredInt(object, "amount"),
				RewardStatus.valueOf(JsonSupport.requiredString(object, "status")));
	}

	private static String encode(String value) {
		Objects.requireNonNull(value, "Marketplace URL value cannot be null.");
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
