package rpg.marketplace;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rpg.characters.Party;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.marketplace.api.MarketplaceApiClient;
import rpg.marketplace.api.MarketplaceApiException;

// Coordinates remote marketplace state with the current campaign's local resources.
public final class MarketplaceTransactionService {

	private final Party party;
	private final ItemCatalog itemCatalog;
	private final String playerId;
	private final MarketplaceApiClient apiClient;

	public MarketplaceTransactionService(
			Party party,
			ItemCatalog itemCatalog,
			String playerId,
			MarketplaceApiClient apiClient) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");
		if (playerId == null || playerId.isBlank()) {
			throw new IllegalArgumentException("Marketplace player ID cannot be empty.");
		}
		this.playerId = playerId;
		this.apiClient = Objects.requireNonNull(apiClient, "Marketplace API client cannot be null.");
	}

	public List<MarketplaceListing> getActiveListings()
			throws IOException, InterruptedException {
		return apiClient.getActiveListings();
	}

	public List<MarketplaceListing> getListingsForCurrentPlayer()
			throws IOException, InterruptedException {
		return apiClient.getListingsForSeller(playerId);
	}

	public List<PendingReward> getPendingRewardsForCurrentPlayer()
			throws IOException, InterruptedException {
		return apiClient.getPendingRewards(playerId);
	}

	public Map<Item, Integer> getInventoryItemsForListing() {
		Map<Item, Integer> tradableItems = new LinkedHashMap<>();

		for (Map.Entry<Item, Integer> entry : party.getInventory().getContents().entrySet()) {
			if (entry.getKey().isPlayerTradable()) {
				tradableItems.put(entry.getKey(), entry.getValue());
			}
		}

		return Map.copyOf(tradableItems);
	}

	public MarketplaceListingCreationResult createListing(
			String itemId,
			int quantity,
			int unitPrice) {
		Item item;

		try {
			item = itemCatalog.getById(itemId);
		} catch (IllegalArgumentException exception) {
			return MarketplaceListingCreationResult.INVALID_ITEM;
		}

		if (!item.isPlayerTradable()) {
			return MarketplaceListingCreationResult.ITEM_NOT_TRADABLE;
		}
		if (quantity <= 0) {
			return MarketplaceListingCreationResult.INVALID_QUANTITY;
		}
		if (unitPrice <= 0) {
			return MarketplaceListingCreationResult.INVALID_UNIT_PRICE;
		}
		if (party.getInventory().getQuantity(item) < quantity) {
			return MarketplaceListingCreationResult.INSUFFICIENT_ITEMS;
		}
		try {
			Math.multiplyExact(quantity, unitPrice);
		} catch (ArithmeticException exception) {
			return MarketplaceListingCreationResult.INVALID_UNIT_PRICE;
		}

		if (!party.getInventory().removeItem(item, quantity)) {
			return MarketplaceListingCreationResult.INSUFFICIENT_ITEMS;
		}

		try {
			apiClient.createListing(playerId, itemId, quantity, unitPrice);
			return MarketplaceListingCreationResult.SUCCESS;
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			rollbackRemovedItem(item, quantity);
			return MarketplaceListingCreationResult.API_UNAVAILABLE;
		} catch (MarketplaceApiException exception) {
			rollbackRemovedItem(item, quantity);
			return MarketplaceListingCreationResult.SERVER_REJECTED;
		} catch (IOException | IllegalArgumentException exception) {
			rollbackRemovedItem(item, quantity);
			return MarketplaceListingCreationResult.API_UNAVAILABLE;
		}
	}

	public MarketplacePurchaseResult purchase(MarketplaceListing listing) {
		if (listing == null) {
			return MarketplacePurchaseResult.INVALID_LISTING;
		}
		if (listing.status() != ListingStatus.ACTIVE) {
			return MarketplacePurchaseResult.LISTING_NOT_ACTIVE;
		}
		if (listing.sellerId().equals(playerId)) {
			return MarketplacePurchaseResult.CANNOT_BUY_OWN_LISTING;
		}

		Item item;
		try {
			item = itemCatalog.getById(listing.itemId());
		} catch (IllegalArgumentException exception) {
			return MarketplacePurchaseResult.INVALID_LISTING;
		}

		if (party.getGold() < listing.totalPrice()) {
			return MarketplacePurchaseResult.INSUFFICIENT_GOLD;
		}
		if (party.getInventory().getRemainingCapacity(item) < listing.quantity()) {
			return MarketplacePurchaseResult.INVENTORY_CAPACITY_EXCEEDED;
		}

		try {
			apiClient.purchaseListing(listing.id(), playerId);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return MarketplacePurchaseResult.API_UNAVAILABLE;
		} catch (MarketplaceApiException exception) {
			if (exception.getStatusCode() == 409) {
				return MarketplacePurchaseResult.LISTING_NOT_ACTIVE;
			}
			if (exception.getStatusCode() == 403) {
				return MarketplacePurchaseResult.CANNOT_BUY_OWN_LISTING;
			}
			if (exception.getStatusCode() == 404) {
				return MarketplacePurchaseResult.INVALID_LISTING;
			}
			return MarketplacePurchaseResult.SERVER_REJECTED;
		} catch (IOException | IllegalArgumentException exception) {
			return MarketplacePurchaseResult.API_UNAVAILABLE;
		}

		if (!party.spendGold(listing.totalPrice())) {
			throw new IllegalStateException("Party gold changed during marketplace purchase.");
		}
		if (!party.getInventory().addItem(item, listing.quantity())) {
			boolean goldRestored = party.addGold(listing.totalPrice());
			if (!goldRestored) {
				throw new IllegalStateException("Marketplace purchase rollback failed.");
			}
			throw new IllegalStateException("Inventory changed during marketplace purchase.");
		}

		return MarketplacePurchaseResult.SUCCESS;
	}

	public MarketplaceListingCancellationResult cancelListing(MarketplaceListing listing) {
		if (listing == null) {
			return MarketplaceListingCancellationResult.INVALID_LISTING;
		}
		if (!listing.sellerId().equals(playerId)) {
			return MarketplaceListingCancellationResult.NOT_LISTING_OWNER;
		}
		if (listing.status() != ListingStatus.ACTIVE) {
			return MarketplaceListingCancellationResult.LISTING_NOT_ACTIVE;
		}

		Item item;
		try {
			item = itemCatalog.getById(listing.itemId());
		} catch (IllegalArgumentException exception) {
			return MarketplaceListingCancellationResult.INVALID_LISTING;
		}

		if (party.getInventory().getRemainingCapacity(item) < listing.quantity()) {
			return MarketplaceListingCancellationResult.INVENTORY_CAPACITY_EXCEEDED;
		}

		try {
			apiClient.cancelListing(listing.id(), playerId);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return MarketplaceListingCancellationResult.API_UNAVAILABLE;
		} catch (MarketplaceApiException exception) {
			if (exception.getStatusCode() == 409) {
				return MarketplaceListingCancellationResult.LISTING_NOT_ACTIVE;
			}
			if (exception.getStatusCode() == 403) {
				return MarketplaceListingCancellationResult.NOT_LISTING_OWNER;
			}
			if (exception.getStatusCode() == 404) {
				return MarketplaceListingCancellationResult.INVALID_LISTING;
			}
			return MarketplaceListingCancellationResult.SERVER_REJECTED;
		} catch (IOException | IllegalArgumentException exception) {
			return MarketplaceListingCancellationResult.API_UNAVAILABLE;
		}

		if (!party.getInventory().addItem(item, listing.quantity())) {
			throw new IllegalStateException("Inventory changed during marketplace cancellation.");
		}

		return MarketplaceListingCancellationResult.SUCCESS;
	}

	public MarketplaceRewardClaimResult claimReward(PendingReward reward) {
		if (reward == null) {
			return MarketplaceRewardClaimResult.INVALID_REWARD;
		}
		if (!reward.sellerId().equals(playerId)) {
			return MarketplaceRewardClaimResult.NOT_REWARD_OWNER;
		}
		if (reward.status() != RewardStatus.PENDING) {
			return MarketplaceRewardClaimResult.REWARD_NOT_PENDING;
		}
		if (party.getRemainingGoldCapacity() < reward.amount()) {
			return MarketplaceRewardClaimResult.GOLD_CAPACITY_EXCEEDED;
		}

		try {
			apiClient.claimReward(reward.id(), playerId);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return MarketplaceRewardClaimResult.API_UNAVAILABLE;
		} catch (MarketplaceApiException exception) {
			if (exception.getStatusCode() == 409) {
				return MarketplaceRewardClaimResult.REWARD_NOT_PENDING;
			}
			if (exception.getStatusCode() == 403) {
				return MarketplaceRewardClaimResult.NOT_REWARD_OWNER;
			}
			if (exception.getStatusCode() == 404) {
				return MarketplaceRewardClaimResult.INVALID_REWARD;
			}
			return MarketplaceRewardClaimResult.SERVER_REJECTED;
		} catch (IOException | IllegalArgumentException exception) {
			return MarketplaceRewardClaimResult.API_UNAVAILABLE;
		}

		if (!party.addGold(reward.amount())) {
			throw new IllegalStateException("Party gold changed during marketplace reward claim.");
		}

		return MarketplaceRewardClaimResult.SUCCESS;
	}

	private void rollbackRemovedItem(Item item, int quantity) {
		if (!party.getInventory().addItem(item, quantity)) {
			throw new IllegalStateException("Marketplace listing rollback failed.");
		}
	}
}
