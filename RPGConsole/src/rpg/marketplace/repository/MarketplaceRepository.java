package rpg.marketplace.repository;

import java.util.List;

import rpg.marketplace.MarketplaceListing;
import rpg.marketplace.MarketplaceTransaction;
import rpg.marketplace.PendingReward;

public interface MarketplaceRepository {
	
	List<MarketplaceListing> getAllListings();

    List<MarketplaceTransaction> getAllTransactions();

    List<PendingReward> getAllPendingRewards();

    void addListing(MarketplaceListing listing);

    void addTransaction(MarketplaceTransaction transaction);

    void addPendingReward(PendingReward reward);

}
