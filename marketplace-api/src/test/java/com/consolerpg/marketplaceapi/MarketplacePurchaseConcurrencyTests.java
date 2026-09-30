package com.consolerpg.marketplaceapi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import com.consolerpg.marketplaceapi.listing.CreateListingRequest;
import com.consolerpg.marketplaceapi.listing.ListingResponse;
import com.consolerpg.marketplaceapi.listing.ListingStatus;
import com.consolerpg.marketplaceapi.listing.MarketplaceListingJpaRepository;
import com.consolerpg.marketplaceapi.listing.MarketplaceListingService;
import com.consolerpg.marketplaceapi.listing.PurchaseListingRequest;
import com.consolerpg.marketplaceapi.reward.MarketplaceRewardJpaRepository;
import com.consolerpg.marketplaceapi.transaction.MarketplaceTransactionJpaRepository;

@SpringBootTest
class MarketplacePurchaseConcurrencyTests {

    @Autowired
    private MarketplaceListingService listingService;

    @Autowired
    private MarketplaceListingJpaRepository listingRepository;

    @Autowired
    private MarketplaceTransactionJpaRepository transactionRepository;

    @Autowired
    private MarketplaceRewardJpaRepository rewardRepository;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        rewardRepository.deleteAll();
        transactionRepository.deleteAll();
        listingRepository.deleteAll();
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void onlyOneBuyerCanPurchaseTheSameListing() throws Exception {
        ListingResponse listing = listingService.createListing(
                new CreateListingRequest("César", "meruma", 1, 5));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Integer> matheusPurchase = purchaseAttempt(
                listing, "Matheus", ready, start);
        Callable<Integer> felipePurchase = purchaseAttempt(
                listing, "Felipe", ready, start);

        List<Future<Integer>> attempts = List.of(
                executor.submit(matheusPurchase),
                executor.submit(felipePurchase));

        ready.await();
        start.countDown();

        int successfulPurchases = 0;
        for (Future<Integer> attempt : attempts) {
            successfulPurchases += attempt.get();
        }

        assertEquals(1, successfulPurchases);
        assertEquals(ListingStatus.SOLD,
                listingRepository.findById(listing.id()).orElseThrow().getStatus());
        assertEquals(1, transactionRepository.count());
        assertEquals(1, rewardRepository.count());
    }

    private Callable<Integer> purchaseAttempt(
            ListingResponse listing,
            String buyerId,
            CountDownLatch ready,
            CountDownLatch start) {

        return () -> {
            ready.countDown();
            start.await();

            try {
                listingService.purchaseListing(
                        listing.id(), new PurchaseListingRequest(buyerId));
                return 1;
            } catch (ResponseStatusException exception) {
                return 0;
            }
        };
    }
}
