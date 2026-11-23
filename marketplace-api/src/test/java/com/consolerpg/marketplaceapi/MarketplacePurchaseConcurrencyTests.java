package com.consolerpg.marketplaceapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.consolerpg.marketplaceapi.listing.CreateListingRequest;
import com.consolerpg.marketplaceapi.listing.ListingResponse;
import com.consolerpg.marketplaceapi.listing.ListingStatus;
import com.consolerpg.marketplaceapi.listing.MarketplaceListingJpaRepository;
import com.consolerpg.marketplaceapi.listing.MarketplaceListingService;
import com.consolerpg.marketplaceapi.reward.MarketplaceRewardEntity;
import com.consolerpg.marketplaceapi.reward.MarketplaceRewardJpaRepository;
import com.consolerpg.marketplaceapi.reward.RewardStatus;
import com.consolerpg.marketplaceapi.transaction.MarketplaceTransactionEntity;
import com.consolerpg.marketplaceapi.transaction.MarketplaceTransactionJpaRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MarketplacePurchaseConcurrencyTests {

    @LocalServerPort
    private int port;

    @Autowired
    private MarketplaceListingService listingService;

    @Autowired
    private MarketplaceListingJpaRepository listingRepository;

    @Autowired
    private MarketplaceTransactionJpaRepository transactionRepository;

    @Autowired
    private MarketplaceRewardJpaRepository rewardRepository;

    private ExecutorService executor;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        rewardRepository.deleteAll();
        transactionRepository.deleteAll();
        listingRepository.deleteAll();
        executor = Executors.newFixedThreadPool(2);
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @RepeatedTest(value = 200, name = "Round {currentRepetition}/{totalRepetitions}")
    void twoHttpBuyersProduceOneSaleAndOnePendingReward(RepetitionInfo repetition)
            throws Exception {
        ListingResponse listing = listingService.createListing(
                new CreateListingRequest("cesar", "meruma", 1, 5));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<HttpResponse<String>>> attempts = List.of(
                    executor.submit(purchaseAttempt(listing, "matheus", ready, start)),
                    executor.submit(purchaseAttempt(listing, "felipe", ready, start)));

            assertTrue(ready.await(10, TimeUnit.SECONDS), "Both buyers must be ready");
            start.countDown();

            List<HttpResponse<String>> responses = List.of(
                    attempts.get(0).get(15, TimeUnit.SECONDS),
                    attempts.get(1).get(15, TimeUnit.SECONDS));

            assertEquals(List.of(200, 409),
                    responses.stream().map(HttpResponse::statusCode).sorted().toList(),
                    () -> "Purchase responses: " + responses);
            assertTrue(responses.stream().filter(response -> response.statusCode() == 200)
                    .allMatch(response -> response.body().contains("\"status\":\"SOLD\"")));

            assertEquals(ListingStatus.SOLD,
                    listingRepository.findById(listing.id()).orElseThrow().getStatus());
            List<MarketplaceTransactionEntity> transactions = transactionRepository.findAll();
            assertEquals(1, transactions.size());
            MarketplaceTransactionEntity sale = transactions.get(0);
            assertEquals(listing.id(), sale.getListingId());
            assertTrue(Set.of("matheus", "felipe").contains(sale.getBuyerId()));
            int winnerIndex = sale.getBuyerId().equals("matheus") ? 0 : 1;
            assertEquals(200, responses.get(winnerIndex).statusCode());
            assertEquals(5, sale.getTotalPrice());

            List<MarketplaceRewardEntity> rewards = rewardRepository.findAll();
            assertEquals(1, rewards.size());
            assertEquals(sale.getId(), rewards.get(0).getTransactionId());
            assertEquals(RewardStatus.PENDING, rewards.get(0).getStatus());
            System.out.printf(
                    "%nMarketplace concurrency test | Listing %s%n"
                    + "Buyer matheus -> HTTP %d%n"
                    + "Buyer felipe  -> HTTP %d%n"
                    + "Persisted sales: %d | Buyer: %s | Total price: %d%n"
                    + "Persisted rewards: %d | Status: %s%n",
                    listing.id(),
                    responses.get(0).statusCode(),
                    responses.get(1).statusCode(),
                    transactions.size(), sale.getBuyerId(), sale.getTotalPrice(),
                    rewards.size(), rewards.get(0).getStatus());
        } finally {
            start.countDown();
        }
    }

    private Callable<HttpResponse<String>> purchaseAttempt(
            ListingResponse listing, String buyerId,
            CountDownLatch ready, CountDownLatch start) {
        return () -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port
                            + "/api/listings/" + listing.id() + "/purchase"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"buyerId\":\"" + buyerId + "\"}"))
                    .build();
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The buyers were not released");
            }
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        };
    }
}
