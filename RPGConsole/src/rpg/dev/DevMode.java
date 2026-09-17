package rpg.dev;

import rpg.characters.Party;
import rpg.items.ItemCatalog;
import rpg.world.WorldProgress;
import rpg.world.travel.TravelProgress;
import rpg.marketplace.MarketplaceListing;
import rpg.marketplace.repository.MarketplaceRepository;

public final class DevMode {

	public static boolean ENABLED = true; // Key to turn Dev Mode on/off

	public static boolean GIVE_STARTING_GOLD = true;
	public static final int STARTING_GOLD = 1_000;

	public static boolean GIVE_TEST_ITEMS = true;
	public static final int TEST_ITEM_QUANTITY = 10;

	public static boolean UNLOCK_ALL_PROGRESS = false;
	
	public static boolean ADD_TEST_MARKETPLACE_DATA = false;

	public static void applyStartingResources(
			Party party,
			ItemCatalog itemCatalog) {

		if (!ENABLED) {
			return;
		}

		if (GIVE_STARTING_GOLD) {
			party.addGold(STARTING_GOLD);
		}

		if (GIVE_TEST_ITEMS) {
			party.getInventory().addItem(
					itemCatalog.getById("meruma"),
					TEST_ITEM_QUANTITY
					);

			party.getInventory().addItem(
					itemCatalog.getById("pelo_lupino"),
					TEST_ITEM_QUANTITY
					);
		}
	}

	public static void applyProgress(
	        WorldProgress worldProgress,
	        TravelProgress travelProgress) {

	    if (!ENABLED || !UNLOCK_ALL_PROGRESS) {
	        return;
	    }

	    worldProgress.discoverAll();
	    travelProgress.unlockAll();
	}
	
	public static void applyMarketplaceData(
	        MarketplaceRepository repository) {

	    if (!ENABLED || !ADD_TEST_MARKETPLACE_DATA) {
	        return;
	    }

	    repository.addListing(
	            new MarketplaceListing(
	                    "listing-001",
	                    "cesar",
	                    "meruma",
	                    1,
	                    5
	            )
	    );
	}

	private DevMode() {
	}
}