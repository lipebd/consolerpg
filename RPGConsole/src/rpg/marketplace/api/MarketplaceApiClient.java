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
import java.util.UUID;

import rpg.marketplace.ListingStatus;
import rpg.marketplace.MarketplaceListing;
import rpg.marketplace.PendingReward;
import rpg.marketplace.RewardStatus;

// Communicates with the marketplace API using only Java standard-library features.
public final class MarketplaceApiClient {

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

	private final HttpClient httpClient;
	private final String baseUrl;
	private final UUID campaignId;

	public MarketplaceApiClient(String baseUrl, UUID campaignId) {
		if (baseUrl == null || baseUrl.isBlank()) {
			throw new IllegalArgumentException("Marketplace API base URL cannot be empty.");
		}

		this.campaignId = Objects.requireNonNull(campaignId, "Campaign ID cannot be null.");
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

	public List<MarketplaceListing> getOwnListings()
			throws IOException, InterruptedException {
		String responseBody = send("GET", "/api/listings?mine=true", null, 200);
		return parseListings(responseBody);
	}

	public MarketplaceListing createListing(
			String sellerName,
			String itemId,
			int quantity,
			int unitPrice) throws IOException, InterruptedException {
		Map<String, Object> fields = new LinkedHashMap<>();
		fields.put("sellerName", sellerName);
		fields.put("itemId", itemId);
		fields.put("quantity", quantity);
		fields.put("unitPrice", unitPrice);

		String responseBody = send("POST", "/api/listings", JsonSupport.object(fields), 201);
		return parseListing(JsonSupport.parseObject(responseBody));
	}

	public MarketplaceListing purchaseListing(String listingId)
			throws IOException, InterruptedException {
		String path = "/api/listings/" + encode(listingId) + "/purchase";
		String responseBody = send("POST", path, null, 200);
		return parseListing(JsonSupport.parseObject(responseBody));
	}

	public MarketplaceListing cancelListing(String listingId)
			throws IOException, InterruptedException {
		String path = "/api/listings/" + encode(listingId) + "/cancel";
		String responseBody = send("POST", path, null, 200);
		return parseListing(JsonSupport.parseObject(responseBody));
	}

	public List<PendingReward> getPendingRewards()
			throws IOException, InterruptedException {
		String responseBody = send("GET", "/api/rewards", null, 200);
		return parseRewards(responseBody);
	}

	public PendingReward claimReward(String rewardId)
			throws IOException, InterruptedException {
		String path = "/api/rewards/" + encode(rewardId) + "/claim";
		String responseBody = send("POST", path, null, 200);
		return parseReward(JsonSupport.parseObject(responseBody));
	}

	private String send(String method, String path, String requestBody, int expectedStatus)
			throws IOException, InterruptedException {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + path))
				.timeout(REQUEST_TIMEOUT)
				.header("Accept", "application/json")
				.header("X-Campaign-Id", campaignId.toString());

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
				JsonSupport.requiredString(object, "sellerName"),
				JsonSupport.requiredString(object, "itemId"),
				JsonSupport.requiredInt(object, "quantity"),
				JsonSupport.requiredInt(object, "unitPrice"),
				JsonSupport.requiredInt(object, "totalPrice"),
				ListingStatus.valueOf(JsonSupport.requiredString(object, "status")),
				JsonSupport.requiredBoolean(object, "ownedByCurrentCampaign"));
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
	            JsonSupport.requiredInt(object, "amount"),
	            RewardStatus.valueOf(JsonSupport.requiredString(object, "status")),
	            JsonSupport.requiredString(object, "itemId"),
	            JsonSupport.requiredInt(object, "quantity"));
	}

	private static String encode(String value) {
		Objects.requireNonNull(value, "Marketplace URL value cannot be null.");
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
