package com.consolerpg.marketplaceapi.listing;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "marketplace_listings")
public class MarketplaceListingEntity {

    @Id
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID sellerCampaignId;

    @Column(nullable = false, updatable = false)
    private String sellerName;

    @Column(nullable = false, updatable = false)
    private String itemId;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Column(nullable = false, updatable = false)
    private int unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @Version
    private long version;

    protected MarketplaceListingEntity() {
    }

    public MarketplaceListingEntity(
            UUID sellerCampaignId,
            String sellerName,
            String itemId,
            int quantity,
            int unitPrice) {

        if (sellerCampaignId == null) {
            throw new IllegalArgumentException("Seller campaign ID cannot be null.");
        }
        if (sellerName == null || sellerName.isBlank()) {
            throw new IllegalArgumentException(
                    "Seller name cannot be null or blank."
            );
        }

        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException(
                    "Item ID cannot be null or blank."
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        if (unitPrice <= 0) {
            throw new IllegalArgumentException(
                    "Unit price must be greater than zero."
            );
        }

        this.id = UUID.randomUUID();
        this.sellerCampaignId = sellerCampaignId;
        this.sellerName = sellerName;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.status = ListingStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSellerCampaignId() {
        return sellerCampaignId;
    }

    public String getSellerName() {
        return sellerName;
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
        return Math.multiplyExact(
                quantity,
                unitPrice
        );
    }

    public ListingStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }

    public void markAsSold() {
        if (status != ListingStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active listings can be sold."
            );
        }

        status = ListingStatus.SOLD;
    }

    public void cancel() {
        if (status != ListingStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active listings can be cancelled."
            );
        }

        status = ListingStatus.CANCELLED;
    }
}
