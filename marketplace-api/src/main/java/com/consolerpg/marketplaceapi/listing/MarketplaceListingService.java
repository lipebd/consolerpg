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
            UUID sellerCampaignId, CreateListingRequest request) {

        try {
            Math.multiplyExact(request.quantity(), request.unitPrice());
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Listing total price is too large.");
        }

        MarketplaceListingEntity listing =
                new MarketplaceListingEntity(
                        sellerCampaignId,
                        request.sellerName(),
                        request.itemId(),
                        request.quantity(),
                        request.unitPrice()
                );

        MarketplaceListingEntity savedListing =
                repository.save(listing);

        return ListingResponse.from(savedListing, sellerCampaignId);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getActiveListings(UUID campaignId) {
        return repository
                .findAllByStatus(ListingStatus.ACTIVE)
                .stream()
                .map(listing -> ListingResponse.from(listing, campaignId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getListingsForSeller(UUID campaignId) {
        return repository
                .findAllBySellerCampaignId(campaignId)
                .stream()
                .filter(listing -> listing.getStatus() != ListingStatus.CANCELLED)
                .map(listing -> ListingResponse.from(listing, campaignId))
                .toList();
    }

    @Transactional
    public ListingResponse purchaseListing(
            UUID listingId,
            UUID campaignId) {

        MarketplaceListingEntity listing = findLockedListing(listingId);

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing is not active.");
        }
        if (listing.getSellerCampaignId().equals(campaignId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A seller cannot buy their own listing.");
        }

        listing.markAsSold();

        MarketplaceTransactionEntity transaction = transactionRepository.save(
                new MarketplaceTransactionEntity(listing, campaignId));

        rewardRepository.save(new MarketplaceRewardEntity(transaction));

        return ListingResponse.from(listing, campaignId);
    }

    @Transactional
    public ListingResponse cancelListing(
            UUID listingId,
            UUID campaignId) {

        MarketplaceListingEntity listing = findLockedListing(listingId);

        if (!listing.getSellerCampaignId().equals(campaignId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the seller can cancel this listing.");
        }
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing is not active.");
        }

        listing.cancel();

        return ListingResponse.from(listing, campaignId);
    }

    private MarketplaceListingEntity findLockedListing(UUID listingId) {
        return repository.findByIdForUpdate(listingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Listing was not found."));
    }
}
