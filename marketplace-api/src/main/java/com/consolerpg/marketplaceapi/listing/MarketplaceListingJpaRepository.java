package com.consolerpg.marketplaceapi.listing;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketplaceListingJpaRepository
        extends JpaRepository<
                MarketplaceListingEntity,
                UUID> {

    List<MarketplaceListingEntity> findAllByStatus(
            ListingStatus status
    );

    List<MarketplaceListingEntity> findAllBySellerCampaignId(
            UUID sellerCampaignId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select listing from MarketplaceListingEntity listing where listing.id = :id")
    Optional<MarketplaceListingEntity> findByIdForUpdate(@Param("id") UUID id);
}
