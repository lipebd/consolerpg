package rpg.marketplace.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rpg.marketplace.MarketplaceListing;
import rpg.marketplace.MarketplaceTransaction;
import rpg.marketplace.PendingReward;

public final class InMemoryMarketplaceRepository implements MarketplaceRepository {

	private final List<MarketplaceListing> listings;
	private final List<MarketplaceTransaction> transactions;
	private final List<PendingReward> pendingRewards;

	public InMemoryMarketplaceRepository() {
		listings = new ArrayList<>();
		transactions = new ArrayList<>();
		pendingRewards = new ArrayList<>();
	}

	@Override
	public List<MarketplaceListing> getAllListings() {
		return List.copyOf(listings);
	}

	@Override
	public List<MarketplaceTransaction> getAllTransactions() {
		return List.copyOf(transactions);
	}

	@Override
	public List<PendingReward> getAllPendingRewards() {
		return List.copyOf(pendingRewards);
	}

	@Override
	public void addListing(MarketplaceListing listing) {
		listings.add(
				Objects.requireNonNull(
						listing,
						"Listing cannot be null."
						)
				);
	}

	@Override
	public void addTransaction(
			MarketplaceTransaction transaction) {

		transactions.add(
				Objects.requireNonNull(
						transaction,
						"Transaction cannot be null."
						)
				);
	}

	@Override
	public void addPendingReward(
			PendingReward reward) {

		pendingRewards.add(
				Objects.requireNonNull(
						reward,
						"Pending reward cannot be null."
						)
				);
	}

}
