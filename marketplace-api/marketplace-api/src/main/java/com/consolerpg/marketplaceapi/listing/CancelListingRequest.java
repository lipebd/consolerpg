package com.consolerpg.marketplaceapi.listing;

import jakarta.validation.constraints.NotBlank;

public record CancelListingRequest(
		@NotBlank
		String sellerId
		) {

}
