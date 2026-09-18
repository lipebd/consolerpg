package rpg.marketplace;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;

import rpg.characters.Party;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.marketplace.repository.MarketplaceRepository;
import rpg.marketplace.api.CreateListingRequest;
import rpg.marketplace.api.MarketplaceApiClient;
import rpg.marketplace.api.MarketplaceApiException;
import rpg.marketplace.api.MarketplaceListingResponse;

public class MarketplaceTransactionService {

	private final Party party;
	private final ItemCatalog itemCatalog;
	private final String playerId;
	private final MarketplaceRepository repository;
	private final MarketplaceApiClient marketplaceApiClient;

	public MarketplaceTransactionService(
			Party party,
			ItemCatalog itemCatalog,
			String playerId,
			MarketplaceRepository repository,
			MarketplaceApiClient marketplaceApiClient) {

		this.party = Objects.requireNonNull(
				party,
				"Party cannot be null."
				);

		this.itemCatalog = Objects.requireNonNull(
				itemCatalog,
				"Item catalog cannot be null."
				);

		this.playerId = Objects.requireNonNull(
				playerId,
				"Player ID cannot be null."
				);

		this.repository = Objects.requireNonNull(
				repository,
				"Marketplace repository cannot be null."
				);

		this.marketplaceApiClient = Objects.requireNonNull(
				marketplaceApiClient,
				"Marketplace API client cannot be null."
				);
	}

	public MarketplacePurchaseResult purchase(
			MarketplaceListing listing) {

		if (listing == null) {
			return MarketplacePurchaseResult.INVALID_LISTING;
		}

		if (listing.getStatus() != ListingStatus.ACTIVE) {
			return MarketplacePurchaseResult.LISTING_NOT_ACTIVE;
		}

		if (listing.getSellerId().equals(playerId)) {
			return MarketplacePurchaseResult.CANNOT_BUY_OWN_LISTING;
		}

		int totalPrice;

		try {
			totalPrice = Math.multiplyExact(
					listing.getUnitPrice(),
					listing.getQuantity()
					);
		} catch (ArithmeticException exception) {
			return MarketplacePurchaseResult.INVALID_LISTING;
		}

		if (party.getGold() < totalPrice) {
			return MarketplacePurchaseResult.INSUFFICIENT_GOLD;
		}

		Item item = itemCatalog.getById(
				listing.getItemId()
				);

		if (!party.spendGold(totalPrice)) {
			return MarketplacePurchaseResult.INSUFFICIENT_GOLD;
		}

		if (!party.getInventory().addItem(
				item,
				listing.getQuantity())) {

			party.addGold(totalPrice);

			return MarketplacePurchaseResult.INVENTORY_ERROR;
		}

		listing.markAsSold();

		createTransactionAndReward(
				listing,
				totalPrice
				);

		return MarketplacePurchaseResult.SUCCESS;
	}

	private void createTransactionAndReward(
			MarketplaceListing listing,
			int totalPrice) {

		String transactionId =
				"transaction-" + (repository.getAllTransactions().size() + 1);

		MarketplaceTransaction transaction =
				new MarketplaceTransaction(
						transactionId,
						listing.getId(),
						playerId,
						listing.getSellerId(),
						listing.getItemId(),
						listing.getQuantity(),
						listing.getUnitPrice()
						);

		repository.addTransaction(transaction);

		String rewardId =
				"reward-" + (repository.getAllPendingRewards().size() + 1);

		PendingReward reward =
				new PendingReward(
						rewardId,
						transactionId,
						listing.getSellerId(),
						totalPrice
						);

		repository.addPendingReward(reward);
	}

	public MarketplaceListingCreationResult createListing(
			String itemId,
			int quantity,
			int unitPrice) {

		if (itemId == null || itemId.isBlank()) {
			return MarketplaceListingCreationResult.INVALID_ITEM;
		}

		if (quantity <= 0) {
			return MarketplaceListingCreationResult.INVALID_QUANTITY;
		}

		if (unitPrice <= 0) {
			return MarketplaceListingCreationResult.INVALID_UNIT_PRICE;
		}

		Item item;

		try {
			item = itemCatalog.getById(itemId);
		} catch (IllegalArgumentException exception) {
			return MarketplaceListingCreationResult.INVALID_ITEM;
		}

		if (party.getInventory().getQuantity(item) < quantity) {
			return MarketplaceListingCreationResult.INSUFFICIENT_ITEMS;
		}

		if (!party.getInventory().removeItem(item, quantity)) {
			return MarketplaceListingCreationResult.INVENTORY_ERROR;
		}

		CreateListingRequest request =
				new CreateListingRequest(
						playerId,
						itemId,
						quantity,
						unitPrice
						);

		MarketplaceListingResponse response;

		try {
			response = marketplaceApiClient.createListing(request);

		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();

			party.getInventory().addItem(item, quantity);

			return MarketplaceListingCreationResult.API_UNAVAILABLE;

		} catch (IOException exception) {
			party.getInventory().addItem(item, quantity);

			return MarketplaceListingCreationResult.API_UNAVAILABLE;
		}

		MarketplaceListing listing =
				response.toMarketplaceListing();

		repository.addListing(listing);

		return MarketplaceListingCreationResult.SUCCESS;
	}

	public List<MarketplaceListing> getListingsForCurrentPlayer() {
		List<MarketplaceListing> playerListings =
				new ArrayList<>();

		for (MarketplaceListing listing : repository.getAllListings()) {

			if (listing.getSellerId().equals(playerId)
					&& listing.getStatus() != ListingStatus.CANCELLED) {

				playerListings.add(listing);
			}
		}

		return playerListings;
	}

	public List<MarketplaceListing> getActiveListings()
			throws IOException, InterruptedException {

		return marketplaceApiClient
				.getActiveListings()
				.stream()
				.map(
						MarketplaceListingResponse
						::toMarketplaceListing
						)

				.toList();
	}

	public MarketplaceListingCancellationResult cancelListing(
			MarketplaceListing listing) {

		if (listing == null) {
			return MarketplaceListingCancellationResult.INVALID_LISTING;
		}

		if (!listing.getSellerId().equals(playerId)) {
			return MarketplaceListingCancellationResult.NOT_LISTING_OWNER;
		}

		if (listing.getStatus() != ListingStatus.ACTIVE) {
			return MarketplaceListingCancellationResult.LISTING_NOT_ACTIVE;
		}

		Item item;

		try {
			item = itemCatalog.getById(listing.getItemId());
		} catch (IllegalArgumentException exception) {
			return MarketplaceListingCancellationResult.INVALID_LISTING;
		}

		MarketplaceListingResponse response;

		try {
			response = marketplaceApiClient.cancelListing(
					listing.getId(),
					playerId
					);

		} catch (MarketplaceApiException exception) {
			return switch (exception.getStatusCode()) {
			case 404 ->
			MarketplaceListingCancellationResult.INVALID_LISTING;

			case 403 ->
			MarketplaceListingCancellationResult.NOT_LISTING_OWNER;

			case 409 ->
			MarketplaceListingCancellationResult.LISTING_NOT_ACTIVE;

			default ->
			MarketplaceListingCancellationResult.API_UNAVAILABLE;
			};

		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();

			return MarketplaceListingCancellationResult.API_UNAVAILABLE;

		} catch (IOException exception) {
			return MarketplaceListingCancellationResult.API_UNAVAILABLE;
		}

		if (!response.status().equals("CANCELLED")) {
			return MarketplaceListingCancellationResult.API_UNAVAILABLE;
		}

		if (!party.getInventory().addItem(
				item,
				listing.getQuantity())) {

			return MarketplaceListingCancellationResult.INVENTORY_ERROR;
		}

		listing.cancel();

		return MarketplaceListingCancellationResult.SUCCESS;
	}

	public Map<Item, Integer> getInventoryItemsForListing() {
		return party.getInventory().getContents();
	}

	public MarketplaceRewardClaimResult claimReward(
			PendingReward reward) {

		if (reward == null) {
			return MarketplaceRewardClaimResult.INVALID_REWARD;
		}

		if (reward.getStatus() != RewardStatus.PENDING) {
			return MarketplaceRewardClaimResult.REWARD_NOT_PENDING;
		}

		if (!reward.getSellerId().equals(playerId)) {
			return MarketplaceRewardClaimResult.NOT_REWARD_OWNER;
		}

		int resultingGold;

		try {
			resultingGold = Math.addExact(
					party.getGold(),
					reward.getAmount()
					);
		} catch (ArithmeticException exception) {
			return MarketplaceRewardClaimResult.GOLD_CAPACITY_EXCEEDED;
		}

		party.addGold(reward.getAmount());

		if (party.getGold() != resultingGold) {
			throw new IllegalStateException(
					"Party gold changed while claiming marketplace reward."
					);
		}

		reward.claim();

		return MarketplaceRewardClaimResult.SUCCESS;
	}

	public List<PendingReward> getPendingRewardsForCurrentPlayer() {
		List<PendingReward> playerRewards = new ArrayList<>();

		for (PendingReward reward : repository.getAllPendingRewards()) {
			if (reward.getStatus() == RewardStatus.PENDING
					&& reward.getSellerId().equals(playerId)) {

				playerRewards.add(reward);
			}
		}

		return playerRewards;
	}

}
