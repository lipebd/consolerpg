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
    private String buyerId;

    @Column(nullable = false, updatable = false)
    private String sellerId;

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
            String buyerId) {

        if (listing == null) {
            throw new IllegalArgumentException("Listing cannot be null.");
        }
        if (buyerId == null || buyerId.isBlank()) {
            throw new IllegalArgumentException("Buyer ID cannot be empty.");
        }
        if (buyerId.equals(listing.getSellerId())) {
            throw new IllegalArgumentException("Buyer and seller cannot be the same player.");
        }

        this.id = UUID.randomUUID();
        this.listingId = listing.getId();
        this.buyerId = buyerId;
        this.sellerId = listing.getSellerId();
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

    public String getBuyerId() {
        return buyerId;
    }

    public String getSellerId() {
        return sellerId;
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
