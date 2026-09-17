package com.consolerpg.marketplaceapi.listing;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketplaceListingJpaRepository
        extends JpaRepository<
                MarketplaceListingEntity,
                UUID> {

    List<MarketplaceListingEntity> findAllByStatus(
            ListingStatus status
    );

    List<MarketplaceListingEntity> findAllBySellerId(
            String sellerId
    );
}
