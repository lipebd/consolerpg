package com.consolerpg.marketplaceapi.listing;

import java.util.List;

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
}
