package com.consolerpg.marketplaceapi.listing;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/listings")
public class MarketplaceListingController {

    private final MarketplaceListingService service;

    public MarketplaceListingController(
            MarketplaceListingService service) {

        this.service = service;
    }

    @GetMapping
    public List<ListingResponse> getListings(
            @RequestHeader("X-Campaign-Id") UUID campaignId,
            @RequestParam(name = "mine", defaultValue = "false") boolean mine) {

        return mine
                ? service.getListingsForSeller(campaignId)
                : service.getActiveListings(campaignId);
    }

    @PostMapping
    public ResponseEntity<ListingResponse> createListing(
            @RequestHeader("X-Campaign-Id") UUID campaignId,
            @Valid @RequestBody CreateListingRequest request) {

        ListingResponse response =
                service.createListing(campaignId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{listingId}/purchase")
    public ListingResponse purchaseListing(
            @PathVariable("listingId") UUID listingId,
            @RequestHeader("X-Campaign-Id") UUID campaignId) {

        return service.purchaseListing(listingId, campaignId);
    }

    @PostMapping("/{listingId}/cancel")
    public ListingResponse cancelListing(
            @PathVariable("listingId") UUID listingId,
            @RequestHeader("X-Campaign-Id") UUID campaignId) {

        return service.cancelListing(listingId, campaignId);
    }
}
