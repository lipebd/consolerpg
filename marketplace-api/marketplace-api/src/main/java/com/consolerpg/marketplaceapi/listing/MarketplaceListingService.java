package com.consolerpg.marketplaceapi.listing;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketplaceListingService {

	private final MarketplaceListingJpaRepository repository;

	public MarketplaceListingService(
			MarketplaceListingJpaRepository repository) {

		this.repository = repository;
	}

	@Transactional
	public ListingResponse createListing(
			CreateListingRequest request) {

		MarketplaceListingEntity listing =
				new MarketplaceListingEntity(
						request.sellerId(),
						request.itemId(),
						request.quantity(),
						request.unitPrice()
						);

		MarketplaceListingEntity savedListing =
				repository.save(listing);

		return ListingResponse.from(savedListing);
	}

	@Transactional(readOnly = true)
	public List<ListingResponse> getActiveListings() {
		return repository
				.findAllByStatus(ListingStatus.ACTIVE)
				.stream()
				.map(ListingResponse::from)
				.toList();
	}

	@Transactional
	public ListingResponse cancelListing(
			UUID listingId,
			CancelListingRequest request) {

		MarketplaceListingEntity listing =
				repository.findById(listingId)
				.orElseThrow(() ->
				new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Listing not found."
						)
						);

		if (!listing.getSellerId().equals(request.sellerId())) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN,
					"Only the seller can cancel this listing."
					);
		}

		if (listing.getStatus() != ListingStatus.ACTIVE) {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"Only active listings can be cancelled."
					);
		}

		listing.cancel();

		return ListingResponse.from(listing);
	}
}
