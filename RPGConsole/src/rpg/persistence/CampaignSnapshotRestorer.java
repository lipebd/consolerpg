package rpg.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

import rpg.world.shops.ShopCatalog;
import rpg.world.shops.ShopDefinition;
import rpg.world.shops.ShopOfferState;
import rpg.world.shops.ShopState;
import rpg.world.shops.ShopStateCatalog;
import rpg.world.shops.ShopStockCatalog;
import rpg.world.shops.ShopStockEntry;
import rpg.battle.rewards.PartyRewardModifierState;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.characters.PlayableCharacterLevelRules;
import rpg.classes.CharacterGrowthCalculator;
import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassDefinition;
import rpg.items.Inventory;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.items.equipment.ArmorDefinition;
import rpg.items.equipment.EquipmentCatalog;
import rpg.items.equipment.TrinketDefinition;
import rpg.items.equipment.WeaponDefinition;
import rpg.skills.SkillCatalog;
import rpg.skills.SkillDefinition;
import rpg.skills.SkillLearningResult;
import rpg.world.WorldClock;
import rpg.world.WorldProgress;
import rpg.world.inns.InnCatalog;
import rpg.world.scenarios.City;
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.travel.TravelCatalog;
import rpg.world.travel.TravelProgress;
import rpg.characters.Party;
import rpg.world.WorldClock;
import rpg.world.WorldProgress;
import rpg.world.scenarios.Scenario;
import rpg.world.shops.ShopStateCatalog;
import rpg.world.travel.TravelProgress;

// Validates campaign snapshots against current definitions and reconstructs runtime state.
public final class CampaignSnapshotRestorer {

	private final ShopCatalog shopCatalog;
	private final ShopStockCatalog shopStockCatalog;
	private final ScenarioCatalog scenarioCatalog;
	private final InnCatalog innCatalog;
	private final TravelCatalog travelCatalog;
	private final ItemCatalog itemCatalog;
	private final PlayableClassCatalog playableClassCatalog;
	private final SkillCatalog skillCatalog;
	private final EquipmentCatalog equipmentCatalog;
	private final CharacterGrowthCalculator growthCalculator;

	public CampaignSnapshotRestorer(
			PlayableClassCatalog playableClassCatalog,
			SkillCatalog skillCatalog,
			EquipmentCatalog equipmentCatalog,
			ItemCatalog itemCatalog,
			ScenarioCatalog scenarioCatalog,
			InnCatalog innCatalog,
			TravelCatalog travelCatalog,
			ShopCatalog shopCatalog,
			ShopStockCatalog shopStockCatalog,
			CharacterGrowthCalculator growthCalculator
	) {
		this.playableClassCatalog = Objects
				.requireNonNull(playableClassCatalog, "Playable class catalog cannot be null.");
		this.skillCatalog = Objects.requireNonNull(skillCatalog, "Skill catalog cannot be null.");
		this.equipmentCatalog = Objects.requireNonNull(equipmentCatalog, "Equipment catalog cannot be null.");
		this.growthCalculator = Objects.requireNonNull(growthCalculator, "Character growth calculator cannot be null.");
		this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");
		this.scenarioCatalog = Objects.requireNonNull(scenarioCatalog, "Scenario catalog cannot be null.");
		this.innCatalog = Objects.requireNonNull(innCatalog, "INN catalog cannot be null.");
		this.travelCatalog = Objects.requireNonNull(travelCatalog, "Travel catalog cannot be null.");
		this.shopCatalog = Objects.requireNonNull(shopCatalog, "Shop catalog cannot be null.");
		this.shopStockCatalog = Objects.requireNonNull(shopStockCatalog, "Shop stock catalog cannot be null.");
	}

	public RestoredCampaign restore(CampaignSnapshot snapshot) {
		Objects.requireNonNull(snapshot, "Campaign snapshot cannot be null.");

		Party party = restoreParty(snapshot.getParty());

		Scenario currentScenario = restoreCurrentScenario(snapshot.getWorld());

		WorldProgress worldProgress = restoreWorldProgress(snapshot.getWorld());

		TravelProgress travelProgress = restoreTravelProgress(snapshot.getWorld());

		WorldClock worldClock = restoreWorldClock(snapshot.getWorld());

		ShopStateCatalog shopStateCatalog = restoreShopStates(snapshot.getShops());

		return new RestoredCampaign(
				party,
				currentScenario,
				worldProgress,
				travelProgress,
				worldClock,
				shopStateCatalog
		);
	}

	ShopStateCatalog restoreShopStates(List<ShopSnapshot> snapshots) {
		Objects.requireNonNull(snapshots, "Shop snapshots cannot be null.");

		ShopStateCatalog restoredCatalog = new ShopStateCatalog();
		Set<String> restoredShopIds = new HashSet<>();

		for (ShopSnapshot snapshot : snapshots) {
			Objects.requireNonNull(snapshot, "Shop snapshot cannot be null.");

			if (!restoredShopIds.add(snapshot.getShopId())) {
				throw new IllegalArgumentException("Shop snapshot is duplicated: " + snapshot.getShopId());
			}

			ShopState shopState = restoreShopState(snapshot);

			restoredCatalog.register(shopState);
		}

		for (ShopDefinition shopDefinition : shopCatalog.getAll()) {
			if (!restoredShopIds.contains(shopDefinition.getId())) {
				throw new IllegalArgumentException("Saved campaign is missing shop state: " + shopDefinition.getId());
			}
		}

		return restoredCatalog;
	}

	private ShopState restoreShopState(ShopSnapshot snapshot) {
		ShopDefinition shopDefinition = shopCatalog.getById(snapshot.getShopId());

		if (snapshot.getActiveOffers().size() > shopDefinition.getOfferSlots()) {
			throw new IllegalArgumentException(
					"Saved active offers exceed the configured slots for shop: " + shopDefinition.getId()
			);
		}

		List<ShopOfferState> restoredOffers = new ArrayList<>();

		for (ShopOfferSnapshot offerSnapshot : snapshot.getActiveOffers()) {
			ShopStockEntry stockEntry = shopStockCatalog.getById(offerSnapshot.getStockEntryId());

			if (!stockEntry.getShopId().equals(shopDefinition.getId())) {
				throw new IllegalArgumentException("Saved offer belongs to another shop: " + stockEntry.getId());
			}

			if (offerSnapshot.getRemainingStock() > stockEntry.getInitialStock()) {
				throw new IllegalArgumentException(
						"Saved remaining stock exceeds the canonical initial stock: " + stockEntry.getId()
				);
			}

			ShopOfferState offerState = new ShopOfferState(stockEntry);

			int consumedStock = stockEntry.getInitialStock() - offerSnapshot.getRemainingStock();

			if (consumedStock > 0) {
				offerState.decreaseStock(consumedStock);
			}

			if (offerState.getRemainingStock() != offerSnapshot.getRemainingStock()) {
				throw new IllegalStateException("Restored shop stock differs from the snapshot: " + stockEntry.getId());
			}

			restoredOffers.add(offerState);
		}

		return new ShopState(shopDefinition, restoredOffers);
	}

	Scenario restoreCurrentScenario(WorldSnapshot snapshot) {
		Objects.requireNonNull(snapshot, "World snapshot cannot be null.");

		Scenario currentScenario = scenarioCatalog.getById(snapshot.getCurrentScenarioId());

		if (!(currentScenario instanceof City)) {
			throw new IllegalArgumentException(
					"Saved current scenario is not a city: " + snapshot.getCurrentScenarioId()
			);
		}

		if (innCatalog.findByScenarioId(currentScenario.getId()).isEmpty()) {
			throw new IllegalArgumentException(
					"Saved current city does not provide an INN: " + currentScenario.getId()
			);
		}

		return currentScenario;
	}

	WorldProgress restoreWorldProgress(WorldSnapshot snapshot) {
		Objects.requireNonNull(snapshot, "World snapshot cannot be null.");

		WorldProgress worldProgress = new WorldProgress(scenarioCatalog);

		for (String scenarioId : snapshot.getDiscoveredScenarioIds()) {
			worldProgress.discover(scenarioId);
		}

		if (!worldProgress.isDiscovered(snapshot.getCurrentScenarioId())) {
			throw new IllegalStateException(
					"Validated current scenario was not restored as discovered: " + snapshot.getCurrentScenarioId()
			);
		}

		return worldProgress;
	}

	TravelProgress restoreTravelProgress(WorldSnapshot snapshot) {
		Objects.requireNonNull(snapshot, "World snapshot cannot be null.");

		TravelProgress travelProgress = new TravelProgress(travelCatalog);

		for (String routeId : snapshot.getUnlockedRouteIds()) {
			travelProgress.unlock(routeId);
		}

		return travelProgress;
	}

	WorldClock restoreWorldClock(WorldSnapshot snapshot) {
		Objects.requireNonNull(snapshot, "World snapshot cannot be null.");

		return new WorldClock(snapshot.getElapsedHours());
	}

	Party restoreParty(PartySnapshot snapshot) {
		Objects.requireNonNull(snapshot, "Party snapshot cannot be null.");

		List<PlayableCharacter> restoredMembers = new ArrayList<>();

		for (PlayableCharacterSnapshot memberSnapshot : snapshot.getMembers()) {
			restoredMembers.add(restoreCharacter(memberSnapshot));
		}

		Party party = new Party(restoredMembers.get(0));

		for (int index = 1; index < restoredMembers.size(); index++) {
			PlayableCharacter member = restoredMembers.get(index);

			if (!party.addMember(member)) {
				throw new IllegalStateException("Validated party member could not be restored: " + member.getName());
			}
		}

		if (!party.addGold(snapshot.getGold())) {
			throw new IllegalStateException("Validated party GOLD could not be restored.");
		}

		restoreInventory(party.getInventory(), snapshot.getInventoryEntries());
		restoreRewardModifiers(party.getRewardModifierState(), snapshot);

		return party;
	}

	private void restoreInventory(Inventory inventory, List<InventoryEntrySnapshot> snapshots) {
		for (InventoryEntrySnapshot snapshot : snapshots) {
			Item item = itemCatalog.getById(snapshot.getItemId());

			if (!inventory.addItem(item, snapshot.getQuantity())) {
				throw new IllegalStateException(
						"Validated inventory entry could not be restored: " + snapshot.getItemId()
				);
			}
		}
	}

	private void restoreRewardModifiers(PartyRewardModifierState modifierState, PartySnapshot snapshot) {
		RewardModifierSnapshot experienceModifier = snapshot.getExperienceRewardModifier();
		RewardModifierSnapshot goldModifier = snapshot.getGoldRewardModifier();

		if (experienceModifier.isActive() && !modifierState.activateExperienceModifier(
				experienceModifier.getModifierPercent(),
				experienceModifier.getVictoriesRemaining()
		)) {
			throw new IllegalStateException("Validated experience reward modifier could not be restored.");
		}

		if (goldModifier.isActive() && !modifierState
				.activateGoldModifier(goldModifier.getModifierPercent(), goldModifier.getVictoriesRemaining())) {
			throw new IllegalStateException("Validated GOLD reward modifier could not be restored.");
		}

		validateRewardModifiers(modifierState, experienceModifier, goldModifier);
	}

	private void validateRewardModifiers(
			PartyRewardModifierState modifierState,
			RewardModifierSnapshot experienceModifier,
			RewardModifierSnapshot goldModifier
	) {
		boolean experienceDiffers = modifierState.getExperienceModifierPercent() != experienceModifier
				.getModifierPercent()
				|| modifierState.getExperienceVictoriesRemaining() != experienceModifier.getVictoriesRemaining();

		boolean goldDiffers = modifierState.getGoldModifierPercent() != goldModifier.getModifierPercent()
				|| modifierState.getGoldVictoriesRemaining() != goldModifier.getVictoriesRemaining();

		if (experienceDiffers || goldDiffers) {
			throw new IllegalStateException("Restored reward modifiers differ from the snapshot.");
		}
	}

	PlayableCharacter restoreCharacter(PlayableCharacterSnapshot snapshot) {
		Objects.requireNonNull(snapshot, "Playable character snapshot cannot be null.");

		validateProgression(snapshot);

		PlayableClassDefinition classDefinition = playableClassCatalog.getById(snapshot.getClassId());

		PlayableCharacter character = new PlayableCharacter(
				snapshot.getName(),
				classDefinition,
				skillCatalog.getByClassId(classDefinition.getId()),
				growthCalculator,
				snapshot.getLevel()
		);

		restoreExperience(character, snapshot);
		restoreLearnedSkills(character, snapshot);
		restoreEquipment(character, snapshot.getEquipment());
		restoreResources(character, snapshot);

		return character;
	}

	private void validateProgression(PlayableCharacterSnapshot snapshot) {
		int level = snapshot.getLevel();
		int experience = snapshot.getExperience();

		PlayableCharacterLevelRules.validateLevel(level);

		if (PlayableCharacterLevelRules.isMaximumLevel(level)) {
			if (experience != 0) {
				throw new IllegalArgumentException(
						"Maximum-level character experience must be zero: " + snapshot.getName()
				);
			}

			return;
		}

		int requiredExperience = PlayableCharacterLevelRules.getExperienceRequiredForNextLevel(level);

		if (experience >= requiredExperience) {
			throw new IllegalArgumentException(
					"Character experience reaches or exceeds the next-level requirement: " + snapshot.getName()
			);
		}
	}

	private void restoreExperience(PlayableCharacter character, PlayableCharacterSnapshot snapshot) {
		if (snapshot.getExperience() > 0) {
			int acceptedExperience = character.gainExperience(snapshot.getExperience());

			if (acceptedExperience != snapshot.getExperience()) {
				throw new IllegalArgumentException(
						"Character experience could not be restored completely: " + snapshot.getName()
				);
			}
		}

		if (character.getLevel() != snapshot.getLevel() || character.getExperience() != snapshot.getExperience()) {
			throw new IllegalArgumentException(
					"Restored character progression differs from the snapshot: " + snapshot.getName()
			);
		}
	}

	private void restoreLearnedSkills(PlayableCharacter character, PlayableCharacterSnapshot snapshot) {
		for (String skillId : snapshot.getLearnedSkillIds()) {
			SkillDefinition skillDefinition = skillCatalog.getById(skillId);

			if (!skillDefinition.isScrollSkill()) {
				throw new IllegalArgumentException("Saved learned skill is not a scroll skill: " + skillId);
			}

			SkillLearningResult result = character.learnSkill(skillDefinition);

			if (result != SkillLearningResult.LEARNED) {
				throw new IllegalArgumentException(
						"Saved skill could not be restored: " + skillId + " (" + result + ")"
				);
			}
		}
	}

	private void restoreEquipment(PlayableCharacter character, EquipmentSnapshot snapshot) {
		WeaponDefinition weapon = snapshot.getWeaponItemId().map(equipmentCatalog::getWeaponByItemId).orElse(null);

		ArmorDefinition armor = snapshot.getArmorItemId().map(equipmentCatalog::getArmorByItemId).orElse(null);

		TrinketDefinition trinket = snapshot.getTrinketItemId().map(equipmentCatalog::getTrinketByItemId).orElse(null);

		validateEquipmentCompatibility(character, weapon, armor, trinket);

		if (weapon != null) {
			character.equipWeapon(weapon);
		}

		if (armor != null) {
			character.equipArmor(armor);
		}

		if (trinket != null) {
			character.equipTrinket(trinket);
		}
	}

	private void validateEquipmentCompatibility(
			PlayableCharacter character,
			WeaponDefinition weapon,
			ArmorDefinition armor,
			TrinketDefinition trinket
	) {
		if (weapon != null && !character.canEquip(weapon)) {
			throw new IllegalArgumentException("Saved weapon is incompatible with character: " + weapon.getItemId());
		}

		if (armor != null && !character.canEquip(armor)) {
			throw new IllegalArgumentException("Saved armor is incompatible with character: " + armor.getItemId());
		}

		if (trinket != null && !character.canEquip(trinket)) {
			throw new IllegalArgumentException("Saved trinket is incompatible with character: " + trinket.getItemId());
		}
	}

	private void restoreResources(PlayableCharacter character, PlayableCharacterSnapshot snapshot) {
		validateResource(snapshot.getCurrentHp(), character.getMaxHp(), "HP", snapshot.getName());
		validateResource(snapshot.getCurrentMp(), character.getMaxMp(), "MP", snapshot.getName());
		validateResource(snapshot.getCurrentSp(), character.getMaxSp(), "SP", snapshot.getName());

		if (!character.restoreAllResources()) {
			throw new IllegalStateException(
					"Newly reconstructed character could not restore resources: " + snapshot.getName()
			);
		}

		int spentMp = character.getMaxMp() - snapshot.getCurrentMp();
		int spentSp = character.getMaxSp() - snapshot.getCurrentSp();
		int lostHp = character.getMaxHp() - snapshot.getCurrentHp();

		if (spentMp > 0 && !character.spendMp(spentMp)) {
			throw new IllegalStateException("Character MP restoration failed: " + snapshot.getName());
		}

		if (spentSp > 0 && !character.spendSp(spentSp)) {
			throw new IllegalStateException("Character SP restoration failed: " + snapshot.getName());
		}

		if (lostHp > 0 && character.takeDamage(lostHp) != lostHp) {
			throw new IllegalStateException("Character HP restoration failed: " + snapshot.getName());
		}
	}

	private void validateResource(int currentValue, int maximumValue, String resourceName, String characterName) {
		if (currentValue > maximumValue) {
			throw new IllegalArgumentException(
					"Saved " + resourceName + " exceeds the character maximum: " + characterName
			);
		}
	}
}