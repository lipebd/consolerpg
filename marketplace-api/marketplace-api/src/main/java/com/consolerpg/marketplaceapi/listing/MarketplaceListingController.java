package com.consolerpg.marketplaceapi.listing;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/listings")
public class MarketplaceListingController {

	private final MarketplaceListingService service;

	public MarketplaceListingController(
			MarketplaceListingService service) {

		this.service = service;
	}

	@GetMapping
	public List<ListingResponse> getActiveListings() {
		return service.getActiveListings();
	}

	@PostMapping
	public ResponseEntity<ListingResponse> createListing(
			@Valid @RequestBody CreateListingRequest request) {

		ListingResponse response =
				service.createListing(request);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(response);
	}

	@PostMapping("/{listingId}/cancel")
	public ListingResponse cancelListing(
			@PathVariable UUID listingId,
			@Valid @RequestBody CancelListingRequest request) {

		return service.cancelListing(
				listingId,
				request
				);
	}
}