package com.consolerpg.marketplaceapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.consolerpg.marketplaceapi.listing.CreateListingRequest;
import com.consolerpg.marketplaceapi.listing.ListingResponse;
import com.consolerpg.marketplaceapi.listing.ListingStatus;
import com.consolerpg.marketplaceapi.listing.MarketplaceListingService;
import com.consolerpg.marketplaceapi.reward.MarketplaceRewardService;
import com.consolerpg.marketplaceapi.reward.RewardStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MarketplaceApiApplicationTests {

	@Autowired
	private MarketplaceListingService listings;

	@Autowired
	private MarketplaceRewardService rewards;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void campaignsWithTheSameLeaderNameKeepSeparateListingsAndRewards() {
		UUID seller = UUID.randomUUID();
		UUID buyer = UUID.randomUUID();
		CreateListingRequest request = new CreateListingRequest("Alex", "meruma", 1, 5);
		ListingResponse sellerListing = listings.createListing(seller, request);
		ListingResponse buyerListing = listings.createListing(buyer, request);

		assertEquals(List.of(sellerListing), listings.getListingsForSeller(seller));
		assertEquals(List.of(buyerListing), listings.getListingsForSeller(buyer));
		var visibleOffer = listings.getActiveListings(buyer).stream()
				.filter(listing -> listing.id().equals(sellerListing.id()))
				.findFirst().orElseThrow();
		assertEquals("Alex", visibleOffer.sellerName());
		assertTrue(sellerListing.ownedByCurrentCampaign());
		assertFalse(visibleOffer.ownedByCurrentCampaign());
		assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
				() -> listings.cancelListing(sellerListing.id(), buyer)).getStatusCode());
		assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
				() -> listings.purchaseListing(sellerListing.id(), seller)).getStatusCode());

		assertEquals(ListingStatus.SOLD, listings.purchaseListing(sellerListing.id(), buyer).status());
		assertEquals(ListingStatus.CANCELLED, listings.cancelListing(buyerListing.id(), buyer).status());
		assertTrue(rewards.getPendingRewards(buyer).isEmpty());
		var pendingReward = rewards.getPendingRewards(seller).get(0);
		assertEquals(5, pendingReward.amount());
		assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
				() -> rewards.claimReward(pendingReward.id(), buyer)).getStatusCode());
		assertEquals(RewardStatus.CLAIMED, rewards.claimReward(pendingReward.id(), seller).status());
		assertTrue(rewards.getPendingRewards(seller).isEmpty());
	}

}
