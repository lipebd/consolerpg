package rpg.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rpg.battle.rewards.PartyRewardModifierState;
import rpg.characters.CharacterEquipment;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.Item;
import rpg.skills.SkillDefinition;
import rpg.world.WorldClock;
import rpg.world.WorldProgress;
import rpg.world.scenarios.Scenario;
import rpg.world.shops.ShopOfferState;
import rpg.world.shops.ShopState;
import rpg.world.shops.ShopStateCatalog;
import rpg.world.travel.TravelProgress;

// Creates an immutable campaign snapshot from the current runtime state.
public final class CampaignSnapshotFactory {

	private CampaignSnapshotFactory() {
	}

	public static CampaignSnapshot create(
			Party party,
			Scenario currentScenario,
			WorldProgress worldProgress,
			TravelProgress travelProgress,
			WorldClock worldClock,
			ShopStateCatalog shopStateCatalog
	) {
		Objects.requireNonNull(party, "Party cannot be null.");
		Objects.requireNonNull(currentScenario, "Current scenario cannot be null.");
		Objects.requireNonNull(worldProgress, "World progress cannot be null.");
		Objects.requireNonNull(travelProgress, "Travel progress cannot be null.");
		Objects.requireNonNull(worldClock, "World clock cannot be null.");
		Objects.requireNonNull(shopStateCatalog, "Shop state catalog cannot be null.");

		PartySnapshot partySnapshot = createPartySnapshot(party);

		WorldSnapshot worldSnapshot = createWorldSnapshot(currentScenario, worldProgress, travelProgress, worldClock);

		List<ShopSnapshot> shopSnapshots = createShopSnapshots(shopStateCatalog);

		return new CampaignSnapshot(partySnapshot, worldSnapshot, shopSnapshots);
	}

	private static PartySnapshot createPartySnapshot(Party party) {
		List<PlayableCharacterSnapshot> memberSnapshots = new ArrayList<>();

		for (PlayableCharacter member : party.getMembers()) {
			memberSnapshots.add(createPlayableCharacterSnapshot(member));
		}

		List<InventoryEntrySnapshot> inventoryEntries = new ArrayList<>();

		for (Map.Entry<Item, Integer> entry : party.getInventory().getContents().entrySet()) {
			inventoryEntries.add(new InventoryEntrySnapshot(entry.getKey().getId(), entry.getValue()));
		}

		PartyRewardModifierState modifierState = party.getRewardModifierState();

		RewardModifierSnapshot experienceModifier = new RewardModifierSnapshot(
				modifierState.getExperienceModifierPercent(),
				modifierState.getExperienceVictoriesRemaining()
		);

		RewardModifierSnapshot goldModifier = new RewardModifierSnapshot(
				modifierState.getGoldModifierPercent(),
				modifierState.getGoldVictoriesRemaining()
		);

		return new PartySnapshot(party.getGold(), memberSnapshots, inventoryEntries, experienceModifier, goldModifier);
	}

	private static PlayableCharacterSnapshot createPlayableCharacterSnapshot(PlayableCharacter character) {
		CharacterEquipment equipment = character.getEquipment();

		EquipmentSnapshot equipmentSnapshot = new EquipmentSnapshot(
				equipment.getWeapon().map(definition -> definition.getItemId()).orElse(null),
				equipment.getArmor().map(definition -> definition.getItemId()).orElse(null),
				equipment.getTrinket().map(definition -> definition.getItemId()).orElse(null)
		);

		List<String> learnedSkillIds = character.getLearnedScrollSkillDefinitions().stream().map(SkillDefinition::getId)
				.toList();

		return new PlayableCharacterSnapshot(
				character.getName(),
				character.getPlayableClassDefinition().getId(),
				character.getLevel(),
				character.getExperience(),
				character.getCurrentHp(),
				character.getCurrentMp(),
				character.getCurrentSp(),
				equipmentSnapshot,
				learnedSkillIds
		);
	}

	private static WorldSnapshot createWorldSnapshot(
			Scenario currentScenario,
			WorldProgress worldProgress,
			TravelProgress travelProgress,
			WorldClock worldClock
	) {
		List<String> discoveredScenarioIds = worldProgress.getDiscoveredScenarioIds().stream().sorted().toList();

		List<String> unlockedRouteIds = travelProgress.getUnlockedRouteIds().stream().sorted().toList();

		return new WorldSnapshot(
				currentScenario.getId(),
				worldClock.getElapsedHours(),
				discoveredScenarioIds,
				unlockedRouteIds
		);
	}

	private static List<ShopSnapshot> createShopSnapshots(ShopStateCatalog shopStateCatalog) {
		List<ShopSnapshot> shopSnapshots = new ArrayList<>();

		for (ShopState shopState : shopStateCatalog.getAll()) {
			List<ShopOfferSnapshot> offerSnapshots = new ArrayList<>();

			for (ShopOfferState offerState : shopState.getActiveOffers()) {
				offerSnapshots
						.add(new ShopOfferSnapshot(offerState.getStockEntry().getId(), offerState.getRemainingStock()));
			}

			shopSnapshots.add(new ShopSnapshot(shopState.getDefinition().getId(), offerSnapshots));
		}

		return List.copyOf(shopSnapshots);
	}
}