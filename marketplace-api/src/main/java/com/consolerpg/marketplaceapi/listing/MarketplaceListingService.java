package com.consolerpg.marketplaceapi.listing;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.consolerpg.marketplaceapi.reward.MarketplaceRewardEntity;
import com.consolerpg.marketplaceapi.reward.MarketplaceRewardJpaRepository;
import com.consolerpg.marketplaceapi.transaction.MarketplaceTransactionEntity;
import com.consolerpg.marketplaceapi.transaction.MarketplaceTransactionJpaRepository;

@Service
public class MarketplaceListingService {

    private final MarketplaceListingJpaRepository repository;
    private final MarketplaceTransactionJpaRepository transactionRepository;
    private final MarketplaceRewardJpaRepository rewardRepository;

    public MarketplaceListingService(
            MarketplaceListingJpaRepository repository,
            MarketplaceTransactionJpaRepository transactionRepository,
            MarketplaceRewardJpaRepository rewardRepository) {

        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.rewardRepository = rewardRepository;
    }

    @Transactional
    public ListingResponse createListing(
            CreateListingRequest request) {

        try {
            Math.multiplyExact(request.quantity(), request.unitPrice());
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Listing total price is too large.");
        }

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

    @Transactional(readOnly = true)
    public List<ListingResponse> getListingsForSeller(String sellerId) {
        if (sellerId == null || sellerId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seller ID cannot be empty.");
        }

        return repository
                .findAllBySellerId(sellerId)
                .stream()
                .filter(listing -> listing.getStatus() != ListingStatus.CANCELLED)
                .map(ListingResponse::from)
                .toList();
    }

    @Transactional
    public ListingResponse purchaseListing(
            UUID listingId,
            PurchaseListingRequest request) {

        MarketplaceListingEntity listing = findLockedListing(listingId);

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing is not active.");
        }
        if (listing.getSellerId().equals(request.buyerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A seller cannot buy their own listing.");
        }

        listing.markAsSold();

        MarketplaceTransactionEntity transaction = transactionRepository.save(
                new MarketplaceTransactionEntity(listing, request.buyerId()));

        rewardRepository.save(new MarketplaceRewardEntity(transaction));

        return ListingResponse.from(listing);
    }

    @Transactional
    public ListingResponse cancelListing(
            UUID listingId,
            CancelListingRequest request) {

        MarketplaceListingEntity listing = findLockedListing(listingId);

        if (!listing.getSellerId().equals(request.sellerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the seller can cancel this listing.");
        }
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing is not active.");
        }

        listing.cancel();

        return ListingResponse.from(listing);
    }

    private MarketplaceListingEntity findLockedListing(UUID listingId) {
        return repository.findByIdForUpdate(listingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Listing was not found."));
    }
}
