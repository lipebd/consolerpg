package com.consolerpg.marketplaceapi.transaction;

import java.util.UUID;

import com.consolerpg.marketplaceapi.listing.MarketplaceListingEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "marketplace_transactions")
public class MarketplaceTransactionEntity {

    @Id
    private UUID id;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID listingId;

    @Column(nullable = false, updatable = false)
    private UUID buyerCampaignId;

    @Column(nullable = false, updatable = false)
    private UUID sellerCampaignId;

    @Column(nullable = false, updatable = false)
    private String itemId;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Column(nullable = false, updatable = false)
    private int unitPrice;

    @Column(nullable = false, updatable = false)
    private int totalPrice;

    protected MarketplaceTransactionEntity() {
    }

    public MarketplaceTransactionEntity(
            MarketplaceListingEntity listing,
            UUID buyerCampaignId) {

        if (listing == null) {
            throw new IllegalArgumentException("Listing cannot be null.");
        }
        if (buyerCampaignId == null) {
            throw new IllegalArgumentException("Buyer campaign ID cannot be null.");
        }
        if (buyerCampaignId.equals(listing.getSellerCampaignId())) {
            throw new IllegalArgumentException("Buyer and seller cannot be the same player.");
        }

        this.id = UUID.randomUUID();
        this.listingId = listing.getId();
        this.buyerCampaignId = buyerCampaignId;
        this.sellerCampaignId = listing.getSellerCampaignId();
        this.itemId = listing.getItemId();
        this.quantity = listing.getQuantity();
        this.unitPrice = listing.getUnitPrice();
        this.totalPrice = listing.getTotalPrice();
    }

    public UUID getId() {
        return id;
    }

    public UUID getListingId() {
        return listingId;
    }

    public UUID getBuyerCampaignId() {
        return buyerCampaignId;
    }

    public UUID getSellerCampaignId() {
        return sellerCampaignId;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public int getTotalPrice() {
        return totalPrice;
    }
}
