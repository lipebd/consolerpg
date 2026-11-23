package com.consolerpg.marketplaceapi.transaction;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketplaceTransactionJpaRepository
        extends JpaRepository<MarketplaceTransactionEntity, UUID> {
}
