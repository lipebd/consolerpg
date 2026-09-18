package rpg.marketplace.api;

import java.io.IOException;

public class MarketplaceApiException extends IOException {
	
	private static final long serialVersionUID = 1L;
	
	private final int statusCode;

    public MarketplaceApiException(
            int statusCode,
            String responseBody) {

        super(
                "Marketplace API returned status "
                        + statusCode
                        + ": "
                        + responseBody
        );

        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
