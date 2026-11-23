package rpg.marketplace.api;

import java.io.IOException;

// Reports an HTTP response that the marketplace client cannot accept.
public final class MarketplaceApiException extends IOException {

	private final int statusCode;

	public MarketplaceApiException(int statusCode, String responseBody) {
		super("Marketplace API returned HTTP " + statusCode + ": " + responseBody);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
