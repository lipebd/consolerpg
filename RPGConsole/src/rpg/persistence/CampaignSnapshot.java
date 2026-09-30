package rpg.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

// Stores one complete immutable campaign snapshot.
public final class CampaignSnapshot {

	private final PartySnapshot party;
	private final WorldSnapshot world;
	private final List<ShopSnapshot> shops;

	public CampaignSnapshot(PartySnapshot party, WorldSnapshot world, List<ShopSnapshot> shops) {
		this.party = Objects.requireNonNull(party, "Party snapshot cannot be null.");
		this.world = Objects.requireNonNull(world, "World snapshot cannot be null.");

		Objects.requireNonNull(shops, "Shop snapshots cannot be null.");

		Set<String> uniqueShopIds = new HashSet<>();

		for (ShopSnapshot shop : shops) {
			Objects.requireNonNull(shop, "Shop snapshot cannot be null.");

			if (!uniqueShopIds.add(shop.getShopId())) {
				throw new IllegalArgumentException("Shop ID is duplicated: " + shop.getShopId());
			}
		}

		this.shops = List.copyOf(shops);
	}

	public PartySnapshot getParty() {
		return party;
	}

	public WorldSnapshot getWorld() {
		return world;
	}

	public List<ShopSnapshot> getShops() {
		return shops;
	}
}