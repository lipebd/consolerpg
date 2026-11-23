package com.consolerpg.marketplaceapi.reward;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketplaceRewardJpaRepository
        extends JpaRepository<MarketplaceRewardEntity, UUID> {

    List<MarketplaceRewardEntity> findAllBySellerIdAndStatus(
            String sellerId,
            RewardStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reward from MarketplaceRewardEntity reward where reward.id = :id")
    Optional<MarketplaceRewardEntity> findByIdForUpdate(@Param("id") UUID id);
}
