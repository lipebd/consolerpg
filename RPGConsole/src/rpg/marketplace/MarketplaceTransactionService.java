package rpg.marketplace;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Map;

import rpg.characters.Party;
import rpg.items.Item;
import rpg.items.ItemCatalog;

public class MarketplaceTransactionService {

	private final Party party;
	private final ItemCatalog itemCatalog;
	private final String playerId;
	private final List<MarketplaceTransaction> transactions;
	private final List<PendingReward> pendingRewards;
	private final List<MarketplaceListing> listings;

	public MarketplaceTransactionService(
			Party party,
			ItemCatalog itemCatalog,
			String playerId,
			List<MarketplaceListing> listings,
			List<MarketplaceTransaction> transactions,
			List<PendingReward> pendingRewards) {

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

		this.listings = Objects.requireNonNull(
				listings,
				"Listings cannot be null."
				);

		this.transactions = Objects.requireNonNull(
				transactions,
				"Transactions cannot be null."
				);

		this.pendingRewards = Objects.requireNonNull(
				pendingRewards,
				"Pending rewards cannot be null."
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
				"transaction-" + (transactions.size() + 1);

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

		transactions.add(transaction);

		String rewardId =
				"reward-" + (pendingRewards.size() + 1);

		PendingReward reward =
				new PendingReward(
						rewardId,
						transactionId,
						listing.getSellerId(),
						totalPrice
						);

		pendingRewards.add(reward);
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

		String listingId = String.format(
				"listing-%03d",
				listings.size() + 1
				);

		MarketplaceListing listing =
				new MarketplaceListing(
						listingId,
						playerId,
						itemId,
						quantity,
						unitPrice
						);

		if (!party.getInventory().removeItem(item, quantity)) {
			return MarketplaceListingCreationResult.INVENTORY_ERROR;
		}

		listings.add(listing);

		return MarketplaceListingCreationResult.SUCCESS;
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

		for (PendingReward reward : pendingRewards) {
			if (reward.getStatus() == RewardStatus.PENDING
					&& reward.getSellerId().equals(playerId)) {

				playerRewards.add(reward);
			}
		}

		return playerRewards;
	}

}
