package rpg.persistence;

import java.util.Objects;

import rpg.characters.Party;
import rpg.world.WorldClock;
import rpg.world.WorldProgress;
import rpg.world.scenarios.Scenario;
import rpg.world.shops.ShopStateCatalog;
import rpg.world.travel.TravelProgress;

// Holds one completely reconstructed campaign before gameplay activation.
public final class RestoredCampaign {

	private final Party party;
	private final Scenario currentScenario;
	private final WorldProgress worldProgress;
	private final TravelProgress travelProgress;
	private final WorldClock worldClock;
	private final ShopStateCatalog shopStateCatalog;

	public RestoredCampaign(
			Party party,
			Scenario currentScenario,
			WorldProgress worldProgress,
			TravelProgress travelProgress,
			WorldClock worldClock,
			ShopStateCatalog shopStateCatalog
	) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.currentScenario = Objects.requireNonNull(currentScenario, "Current scenario cannot be null.");
		this.worldProgress = Objects.requireNonNull(worldProgress, "World progress cannot be null.");
		this.travelProgress = Objects.requireNonNull(travelProgress, "Travel progress cannot be null.");
		this.worldClock = Objects.requireNonNull(worldClock, "World clock cannot be null.");
		this.shopStateCatalog = Objects.requireNonNull(shopStateCatalog, "Shop state catalog cannot be null.");
	}

	public Party getParty() {
		return party;
	}

	public Scenario getCurrentScenario() {
		return currentScenario;
	}

	public WorldProgress getWorldProgress() {
		return worldProgress;
	}

	public TravelProgress getTravelProgress() {
		return travelProgress;
	}

	public WorldClock getWorldClock() {
		return worldClock;
	}

	public ShopStateCatalog getShopStateCatalog() {
		return shopStateCatalog;
	}
}