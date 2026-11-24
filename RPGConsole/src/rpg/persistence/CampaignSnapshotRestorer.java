package rpg.persistence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import rpg.battle.rewards.PartyRewardModifierState;
import rpg.characters.CharacterNameRules;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.characters.PlayableCharacterLevelRules;
import rpg.classes.CharacterGrowthCalculator;
import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassDefinition;
import rpg.classes.paths.ClassPathRules;
import rpg.enemies.EnemyCatalog;
import rpg.flags.CampaignFlags;
import rpg.flags.FlagCatalog;
import rpg.items.Inventory;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.items.equipment.ArmorDefinition;
import rpg.items.equipment.EquipmentCatalog;
import rpg.items.equipment.TrinketDefinition;
import rpg.items.equipment.WeaponDefinition;
import rpg.quests.QuestCatalog;
import rpg.quests.QuestProgress;
import rpg.quests.QuestProgressRestorer;
import rpg.quests.QuestStatus;
import rpg.recruitment.QuestRecruitmentCatalog;
import rpg.recruitment.QuestRecruitmentDefinition;
import rpg.recruitment.RecruitmentCandidateState;
import rpg.recruitment.RecruitmentCandidateStateCatalog;
import rpg.recruitment.RecruitmentCandidateStateRestorer;
import rpg.skills.SkillCatalog;
import rpg.skills.SkillDefinition;
import rpg.skills.SkillLearningResult;
import rpg.world.WorldClock;
import rpg.world.WorldProgress;
import rpg.world.bestiary.BestiaryStateCatalog;
import rpg.world.branches.ScenarioBranchCatalog;
import rpg.world.inns.InnCatalog;
import rpg.world.scenarios.City;
import rpg.world.scenarios.DungeonProgress;
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.shops.ShopCatalog;
import rpg.world.shops.ShopDefinition;
import rpg.world.shops.ShopOfferState;
import rpg.world.shops.ShopState;
import rpg.world.shops.ShopStateCatalog;
import rpg.world.shops.ShopStockCatalog;
import rpg.world.shops.ShopStockEntry;
import rpg.world.travel.TravelCatalog;
import rpg.world.travel.TravelProgress;
import rpg.world.shops.ShopStateGenerator;

// Validates campaign snapshots against current definitions and reconstructs runtime state.
public final class CampaignSnapshotRestorer {

	private final ScenarioBranchCatalog scenarioBranchCatalog;
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
	private final QuestCatalog questCatalog;
	private final FlagCatalog flagCatalog;
	private final QuestRecruitmentCatalog recruitmentCatalog;
	private final ShopStateGenerator shopStateGenerator;
	private final EnemyCatalog enemyCatalog;

	public CampaignSnapshotRestorer(
			PlayableClassCatalog playableClassCatalog,
			SkillCatalog skillCatalog,
			EquipmentCatalog equipmentCatalog,
			ItemCatalog itemCatalog,
			ScenarioCatalog scenarioCatalog,
			InnCatalog innCatalog,
			TravelCatalog travelCatalog,
			ScenarioBranchCatalog scenarioBranchCatalog,
			ShopCatalog shopCatalog,
			ShopStockCatalog shopStockCatalog,
			ShopStateGenerator shopStateGenerator,
            CharacterGrowthCalculator growthCalculator,
            QuestCatalog questCatalog,
            FlagCatalog flagCatalog,
            QuestRecruitmentCatalog recruitmentCatalog,
			EnemyCatalog enemyCatalog) {
        this.playableClassCatalog =
                Objects.requireNonNull(
                        playableClassCatalog, "Playable class catalog cannot be null.");
		this.skillCatalog = Objects.requireNonNull(skillCatalog, "Skill catalog cannot be null.");
        this.equipmentCatalog =
                Objects.requireNonNull(equipmentCatalog, "Equipment catalog cannot be null.");
        this.growthCalculator =
                Objects.requireNonNull(
                        growthCalculator, "Character growth calculator cannot be null.");
		this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");
        this.scenarioCatalog =
                Objects.requireNonNull(scenarioCatalog, "Scenario catalog cannot be null.");
		this.innCatalog = Objects.requireNonNull(innCatalog, "INN catalog cannot be null.");
        this.travelCatalog =
                Objects.requireNonNull(travelCatalog, "Travel catalog cannot be null.");
		this.shopCatalog = Objects.requireNonNull(shopCatalog, "Shop catalog cannot be null.");
        this.shopStockCatalog =
                Objects.requireNonNull(shopStockCatalog, "Shop stock catalog cannot be null.");
        this.questCatalog =
                Objects.requireNonNull(questCatalog, "Quest catalog cannot be null.");
        this.flagCatalog =
                Objects.requireNonNull(flagCatalog, "Flag catalog cannot be null.");
        this.recruitmentCatalog =
                Objects.requireNonNull(
                        recruitmentCatalog, "Quest recruitment catalog cannot be null.");
        this.scenarioBranchCatalog =
                Objects.requireNonNull(
                        scenarioBranchCatalog, "Scenario branch catalog cannot be null.");
        this.shopStateGenerator =
                Objects.requireNonNull(shopStateGenerator, "Shop state generator cannot be null.");
		this.enemyCatalog = Objects.requireNonNull(enemyCatalog, "Enemy catalog cannot be null.");
	}

	public RestoredCampaign restore(CampaignSnapshot snapshot) {
		Objects.requireNonNull(snapshot, "Campaign snapshot cannot be null.");

		Party party = restoreParty(snapshot.getParty());

		Scenario currentScenario = restoreCurrentScenario(snapshot.getWorld());

		WorldProgress worldProgress = restoreWorldProgress(snapshot.getWorld());

		TravelProgress travelProgress = restoreTravelProgress(snapshot.getWorld());

        DungeonProgress dungeonProgress = restoreDungeonProgress(snapshot.getWorld());

		WorldClock worldClock = restoreWorldClock(snapshot.getWorld());

		ShopStateCatalog shopStateCatalog = restoreShopStates(snapshot.getShops());

		QuestProgress questProgress =
		        restoreQuestProgress(snapshot.getQuestProgress());

		CampaignFlags campaignFlags =
		        restoreCampaignFlags(snapshot.getCampaignFlags());

		RecruitmentCandidateStateCatalog candidateStateCatalog =
		        restoreRecruitmentCandidates(snapshot.getRecruitmentCandidates());

		BestiaryStateCatalog bestiaryStateCatalog = restoreBestiary(snapshot.getBestiary());

		validateNarrativeCoherence(
		        party,
		        questProgress,
		        campaignFlags,
		        candidateStateCatalog);

		return new RestoredCampaign(
		        snapshot.getCampaignId(),
		        party,
		        currentScenario,
		        worldProgress,
		        travelProgress,
		        dungeonProgress,
		        worldClock,
		        shopStateCatalog,
		        questProgress,
		        campaignFlags,
		        candidateStateCatalog,
				bestiaryStateCatalog);
	}

	private BestiaryStateCatalog restoreBestiary(BestiarySnapshot snapshot) {
		Objects.requireNonNull(snapshot, "Bestiary snapshot cannot be null.");

		for (String enemyId : snapshot.getDiscoveredEnemyIds()) {
			enemyCatalog.getById(enemyId);
		}

		return new BestiaryStateCatalog(snapshot.getDiscoveredEnemyIds());
	}

	private QuestProgress restoreQuestProgress(QuestProgressSnapshot snapshot) {
	    QuestProgressRestorer restorer = new QuestProgressRestorer(questCatalog);

	    for (ActiveQuestSnapshot activeQuest : snapshot.getActiveQuests()) {
	        Map<String, Integer> objectiveProgressById = new LinkedHashMap<>();

	        for (QuestObjectiveProgressSnapshot objective :
	                activeQuest.getObjectiveProgressSnapshots()) {
	            objectiveProgressById.put(
	                    objective.getObjectiveId(),
	                    objective.getCurrentQuantity());
	        }

	        restorer.restoreActiveQuest(
	                activeQuest.getQuestId(),
	                activeQuest.getCurrentStep(),
	                objectiveProgressById);
	    }

	    for (String completedQuestId : snapshot.getCompletedQuestIds()) {
	        restorer.restoreCompletedQuest(completedQuestId);
	    }

	    return restorer.finish();
	}

	private CampaignFlags restoreCampaignFlags(CampaignFlagsSnapshot snapshot) {
	    CampaignFlags campaignFlags = new CampaignFlags(flagCatalog);

	    for (String flagId : snapshot.getSetFlagIds()) {
	        if (!campaignFlags.set(flagId)) {
	            throw new IllegalArgumentException(
	                    "Saved campaign flag is duplicated: " + flagId);
	        }
	    }

	    return campaignFlags;
	}

	private RecruitmentCandidateStateCatalog restoreRecruitmentCandidates(
	        List<RecruitmentCandidateSnapshot> snapshots) {
	    RecruitmentCandidateStateRestorer restorer =
	            new RecruitmentCandidateStateRestorer(recruitmentCatalog);

	    for (RecruitmentCandidateSnapshot snapshot : snapshots) {
	        restorer.restoreCandidate(
	                snapshot.getCandidateId(),
	                snapshot.getName(),
	                snapshot.isRecruited());
	    }

	    return restorer.finish();
	}

	ShopStateCatalog restoreShopStates(List<ShopSnapshot> snapshots) {
	    Objects.requireNonNull(snapshots, "Shop snapshots cannot be null.");

	    Map<String, ShopSnapshot> snapshotsByShopId = new LinkedHashMap<>();

	    for (ShopSnapshot snapshot : snapshots) {
	        Objects.requireNonNull(snapshot, "Shop snapshot cannot be null.");

	        if (snapshotsByShopId.putIfAbsent(snapshot.getShopId(), snapshot) != null) {
	            throw new IllegalArgumentException(
	                    "Shop snapshot is duplicated: " + snapshot.getShopId());
	        }
	    }

	    ShopStateCatalog restoredCatalog = new ShopStateCatalog();

	    for (ShopDefinition shopDefinition : shopCatalog.getAll()) {
	        ShopSnapshot savedShop = snapshotsByShopId.get(shopDefinition.getId());

	        restoredCatalog.register(
	                savedShop == null
	                        ? shopStateGenerator.generateForShop(shopDefinition)
	                        : restoreShopState(shopDefinition, savedShop));
	    }

	    return restoredCatalog;
	}

	private ShopState restoreShopState(ShopDefinition shopDefinition, ShopSnapshot snapshot) {
		List<ShopOfferState> restoredOffers = new ArrayList<>();
		Set<String> savedOfferIds = new HashSet<>();

		for (ShopOfferSnapshot offerSnapshot : snapshot.getActiveOffers()) {
			String entryId = offerSnapshot.getStockEntryId();

			if (!savedOfferIds.add(entryId)) {
				throw new IllegalArgumentException("Saved shop offer is duplicated: " + entryId);
			}

			ShopStockEntry stockEntry = shopStockCatalog.getAll().stream()
					.filter(entry -> entry.getId().equals(entryId)).findFirst().orElse(null);

			// The offer was removed from the current definitions.
			if (stockEntry == null) {
				continue;
			}

			// A saved offer cannot silently move to a different merchant.
			if (!stockEntry.getShopId().equals(shopDefinition.getId())) {
				throw new IllegalArgumentException("Saved offer belongs to another shop: " + entryId);
			}

			// If the shop now has fewer slots, preserve the earliest surviving offers.
			if (restoredOffers.size() >= shopDefinition.getOfferSlots()) {
				continue;
			}

			int remainingStock = Math.min(offerSnapshot.getRemainingStock(), stockEntry.getInitialStock());

			ShopOfferState offerState = new ShopOfferState(stockEntry);
			int consumedStock = stockEntry.getInitialStock() - remainingStock;

			if (consumedStock > 0) {
				offerState.decreaseStock(consumedStock);
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
                    "Saved current scenario is not a city: " + snapshot.getCurrentScenarioId());
		}

		if (innCatalog.findByScenarioId(currentScenario.getId()).isEmpty()) {
			throw new IllegalArgumentException(
                    "Saved current city does not provide an INN: " + currentScenario.getId());
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
                    "Validated current scenario was not restored as discovered: "
                            + snapshot.getCurrentScenarioId());
		}

		return worldProgress;
	}

	TravelProgress restoreTravelProgress(WorldSnapshot snapshot) {
		Objects.requireNonNull(snapshot, "World snapshot cannot be null.");

        TravelProgress travelProgress = new TravelProgress(travelCatalog, scenarioBranchCatalog);

		for (String routeId : snapshot.getUnlockedRouteIds()) {
			travelProgress.unlock(routeId);
		}

		return travelProgress;
	}

    DungeonProgress restoreDungeonProgress(WorldSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "World snapshot cannot be null.");

        DungeonProgress dungeonProgress = new DungeonProgress(scenarioCatalog);
        Set<String> restoredDungeonIds = new HashSet<>();

        for (DungeonProgressSnapshot dungeonSnapshot : snapshot.getDungeonProgressSnapshots()) {
            String dungeonId = dungeonSnapshot.getDungeonId();

            if (!restoredDungeonIds.add(dungeonId)) {
                throw new IllegalArgumentException(
                        "Dungeon progress snapshot is duplicated: " + dungeonId);
            }

            dungeonProgress.restoreState(
                    dungeonId,
                    dungeonSnapshot.isNarrativeBossDefeated(),
                    dungeonSnapshot.getRecurringBossAvailableAtHour(),
                    dungeonSnapshot.getEpicAvailableAtHour());
        }

        for (String dungeonId : dungeonProgress.getDungeonIds()) {
            if (!restoredDungeonIds.contains(dungeonId)) {
                throw new IllegalArgumentException(
                        "Saved campaign is missing dungeon progress: " + dungeonId);
            }
        }

        return dungeonProgress;
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
                throw new IllegalStateException(
                        "Validated party member could not be restored: " + member.getName());
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
                        "Validated inventory entry could not be restored: " + snapshot.getItemId());
			}
		}
	}

    private void restoreRewardModifiers(
            PartyRewardModifierState modifierState, PartySnapshot snapshot) {
		RewardModifierSnapshot experienceModifier = snapshot.getExperienceRewardModifier();
		RewardModifierSnapshot goldModifier = snapshot.getGoldRewardModifier();

        if (experienceModifier.isActive()
                && !modifierState.activateExperienceModifier(
				experienceModifier.getModifierPercent(),
                        experienceModifier.getVictoriesRemaining())) {
            throw new IllegalStateException(
                    "Validated experience reward modifier could not be restored.");
		}

        if (goldModifier.isActive()
                && !modifierState.activateGoldModifier(
                        goldModifier.getModifierPercent(), goldModifier.getVictoriesRemaining())) {
            throw new IllegalStateException(
                    "Validated GOLD reward modifier could not be restored.");
		}

		validateRewardModifiers(modifierState, experienceModifier, goldModifier);
	}

	private void validateRewardModifiers(
			PartyRewardModifierState modifierState,
			RewardModifierSnapshot experienceModifier,
            RewardModifierSnapshot goldModifier) {
        boolean experienceDiffers =
                modifierState.getExperienceModifierPercent()
                                != experienceModifier.getModifierPercent()
                        || modifierState.getExperienceVictoriesRemaining()
                                != experienceModifier.getVictoriesRemaining();

        boolean goldDiffers =
                modifierState.getGoldModifierPercent() != goldModifier.getModifierPercent()
                        || modifierState.getGoldVictoriesRemaining()
                                != goldModifier.getVictoriesRemaining();

		if (experienceDiffers || goldDiffers) {
			throw new IllegalStateException("Restored reward modifiers differ from the snapshot.");
		}
	}

	PlayableCharacter restoreCharacter(PlayableCharacterSnapshot snapshot) {
		Objects.requireNonNull(snapshot, "Playable character snapshot cannot be null.");

		validateProgression(snapshot);

        PlayableClassDefinition classDefinition =
                playableClassCatalog.getById(snapshot.getClassId());

        PlayableCharacter character =
                new PlayableCharacter(
				snapshot.getName(),
				classDefinition,
				skillCatalog.getByClassId(classDefinition.getId()),
				growthCalculator,
                        snapshot.getLevel());

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
                        "Maximum-level character experience must be zero: " + snapshot.getName());
			}

			return;
		}

        int requiredExperience =
                PlayableCharacterLevelRules.getExperienceRequiredForNextLevel(level);

		if (experience >= requiredExperience) {
			throw new IllegalArgumentException(
                    "Character experience reaches or exceeds the next-level requirement: "
                            + snapshot.getName());
		}
	}

    private void restoreExperience(
            PlayableCharacter character, PlayableCharacterSnapshot snapshot) {
		if (snapshot.getExperience() > 0) {
			int acceptedExperience = character.gainExperience(snapshot.getExperience());

			if (acceptedExperience != snapshot.getExperience()) {
				throw new IllegalArgumentException(
                        "Character experience could not be restored completely: "
                                + snapshot.getName());
			}
		}

        if (character.getLevel() != snapshot.getLevel()
                || character.getExperience() != snapshot.getExperience()) {
			throw new IllegalArgumentException(
                    "Restored character progression differs from the snapshot: "
                            + snapshot.getName());
		}
	}

    private void restoreLearnedSkills(
            PlayableCharacter character, PlayableCharacterSnapshot snapshot) {
		for (String skillId : snapshot.getLearnedSkillIds()) {
			SkillDefinition skillDefinition = skillCatalog.getById(skillId);

			if (!skillDefinition.isScrollSkill()) {
                throw new IllegalArgumentException(
                        "Saved learned skill is not a scroll skill: " + skillId);
			}

			SkillLearningResult result = character.learnSkill(skillDefinition);

			if (result != SkillLearningResult.LEARNED) {
				throw new IllegalArgumentException(
                        "Saved skill could not be restored: " + skillId + " (" + result + ")");
			}
		}
	}

	private void restoreEquipment(PlayableCharacter character, EquipmentSnapshot snapshot) {
        WeaponDefinition weapon =
                snapshot.getWeaponItemId().map(equipmentCatalog::getWeaponByItemId).orElse(null);

        ArmorDefinition armor =
                snapshot.getArmorItemId().map(equipmentCatalog::getArmorByItemId).orElse(null);

        TrinketDefinition trinket =
                snapshot.getTrinketItemId().map(equipmentCatalog::getTrinketByItemId).orElse(null);

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
            TrinketDefinition trinket) {
		if (weapon != null && !character.canEquip(weapon)) {
            throw new IllegalArgumentException(
                    "Saved weapon is incompatible with character: " + weapon.getItemId());
		}

		if (armor != null && !character.canEquip(armor)) {
            throw new IllegalArgumentException(
                    "Saved armor is incompatible with character: " + armor.getItemId());
		}

		if (trinket != null && !character.canEquip(trinket)) {
            throw new IllegalArgumentException(
                    "Saved trinket is incompatible with character: " + trinket.getItemId());
		}
	}

	private void restoreResources(PlayableCharacter character, PlayableCharacterSnapshot snapshot) {
		validateResource(snapshot.getCurrentHp(), character.getMaxHp(), "HP", snapshot.getName());
		validateResource(snapshot.getCurrentMp(), character.getMaxMp(), "MP", snapshot.getName());
		validateResource(snapshot.getCurrentSp(), character.getMaxSp(), "SP", snapshot.getName());

		if (!character.restoreAllResources()) {
			throw new IllegalStateException(
                    "Newly reconstructed character could not restore resources: "
                            + snapshot.getName());
		}

		int spentMp = character.getMaxMp() - snapshot.getCurrentMp();
		int spentSp = character.getMaxSp() - snapshot.getCurrentSp();
		int lostHp = character.getMaxHp() - snapshot.getCurrentHp();

		if (spentMp > 0 && !character.spendMp(spentMp)) {
            throw new IllegalStateException(
                    "Character MP restoration failed: " + snapshot.getName());
		}

		if (spentSp > 0 && !character.spendSp(spentSp)) {
            throw new IllegalStateException(
                    "Character SP restoration failed: " + snapshot.getName());
		}

		if (lostHp > 0 && character.takeDamage(lostHp) != lostHp) {
            throw new IllegalStateException(
                    "Character HP restoration failed: " + snapshot.getName());
		}
	}

    private void validateResource(
            int currentValue, int maximumValue, String resourceName, String characterName) {
		if (currentValue > maximumValue) {
			throw new IllegalArgumentException(
                    "Saved " + resourceName + " exceeds the character maximum: " + characterName);
		}
	}

    private void validateNarrativeCoherence(
            Party party,
            QuestProgress questProgress,
            CampaignFlags campaignFlags,
            RecruitmentCandidateStateCatalog candidateStateCatalog) {
        validateDistinctPartyClasses(party);
        validateCandidatePartyMatches(party, candidateStateCatalog);
        validateRecruitmentQuestState(questProgress, candidateStateCatalog);
        validateFirstRecruitmentCeremony(
                party,
                questProgress,
                campaignFlags,
                candidateStateCatalog);
    }

    private void validateDistinctPartyClasses(Party party) {
        Set<String> classIds = new HashSet<>();

        for (PlayableCharacter member : party.getMembers()) {
            String classId = member.getPlayableClassDefinition().getId();

            if (!classIds.add(classId)) {
                throw new IllegalArgumentException(
                        "Saved party contains duplicated playable classes: " + classId);
            }
        }
    }

    private void validateCandidatePartyMatches(
            Party party,
            RecruitmentCandidateStateCatalog candidateStateCatalog) {
        for (RecruitmentCandidateState candidateState :
                candidateStateCatalog.getAll()) {
            List<PlayableCharacter> matchingMembers =
                    party.getMembers().stream()
                            .filter(
                                    member ->
                                            CharacterNameRules.areEquivalent(
                                                    member.getName(),
                                                    candidateState.getName()))
                            .toList();

            if (!candidateState.isRecruited() && !matchingMembers.isEmpty()) {
                throw new IllegalArgumentException(
                        "Pending recruitment candidate is already present in the party: "
                                + candidateState.getCandidateId());
            }

            if (!candidateState.isRecruited()) {
                continue;
            }

            if (matchingMembers.size() != 1
                    || matchingMembers.get(0) == party.getLeader()) {
                throw new IllegalArgumentException(
                        "Recruited candidate must match exactly one non-leader party member: "
                                + candidateState.getCandidateId());
            }

            PlayableCharacter matchingAlly = matchingMembers.get(0);

            QuestRecruitmentDefinition definition =
                    recruitmentCatalog.getByCandidateId(
                            candidateState.getCandidateId());

            if (matchingAlly.getLevel() < definition.getAllyLevel()) {
                throw new IllegalArgumentException(
                        "Recruited ally level is lower than the configured entry level: "
                                + candidateState.getCandidateId());
            }

            String allyClassId =
                    matchingAlly.getPlayableClassDefinition().getId();

            if (allyClassId.equals(ClassPathRules.BEGINNER_CLASS_ID)
                    || allyClassId.equals(ClassPathRules.FINAL_FLAGELO_CLASS_ID)) {
                throw new IllegalArgumentException(
                        "Recruited ally uses an ineligible class: " + allyClassId);
            }
        }
    }

    private void validateRecruitmentQuestState(
            QuestProgress questProgress,
            RecruitmentCandidateStateCatalog candidateStateCatalog) {
        for (QuestRecruitmentDefinition definition :
                recruitmentCatalog.getAll()) {
            boolean recruitmentStepCompleted =
                    hasCompletedRecruitmentStep(questProgress, definition);

            RecruitmentCandidateState candidateState =
                    candidateStateCatalog
                            .findByCandidateId(definition.getCandidateId())
                            .orElse(null);

            boolean candidateRecruited =
                    candidateState != null && candidateState.isRecruited();

            if (recruitmentStepCompleted != candidateRecruited) {
                throw new IllegalArgumentException(
                        "Recruitment quest state and candidate state are inconsistent: "
                                + definition.getCandidateId());
            }
        }
    }

    private boolean hasCompletedRecruitmentStep(
            QuestProgress questProgress,
            QuestRecruitmentDefinition definition) {
        QuestStatus status =
                questProgress.getStatus(definition.getQuestId());

        if (status == QuestStatus.COMPLETED) {
            return true;
        }

        if (status != QuestStatus.ACTIVE) {
            return false;
        }

        return questProgress
                .getCurrentStep(definition.getQuestId())
                .orElseThrow()
                .getStep()
                > definition.getStep();
    }

    private void validateFirstRecruitmentCeremony(
            Party party,
            QuestProgress questProgress,
            CampaignFlags campaignFlags,
            RecruitmentCandidateStateCatalog candidateStateCatalog) {
        if (recruitmentCatalog.getAll().isEmpty()) {
            return;
        }

        QuestRecruitmentDefinition firstRecruitment =
                recruitmentCatalog.getAll().get(0);

        boolean questCompleted =
                questProgress.getStatus(firstRecruitment.getQuestId())
                        == QuestStatus.COMPLETED;

        boolean candidateRecruited =
                candidateStateCatalog
                        .findByCandidateId(firstRecruitment.getCandidateId())
                        .map(RecruitmentCandidateState::isRecruited)
                        .orElse(false);

        boolean ceremonyFlagSet =
                campaignFlags.isSet(ClassPathRules.COMPLETION_FLAG_ID);

        boolean leaderLeftBeginnerClass =
                !party.getLeader()
                        .getPlayableClassDefinition()
                        .getId()
                        .equals(ClassPathRules.BEGINNER_CLASS_ID);

        if (questCompleted != candidateRecruited
                || questCompleted != ceremonyFlagSet
                || questCompleted != leaderLeftBeginnerClass) {
            throw new IllegalArgumentException(
                    "The first recruitment ceremony consequences are inconsistent.");
        }

        if (candidateRecruited
                && (party.size() < 2
                        || !CharacterNameRules.areEquivalent(
                                party.getMembers().get(1).getName(),
                                candidateStateCatalog
                                        .getByCandidateId(
                                                firstRecruitment.getCandidateId())
                                        .getName()))) {
            throw new IllegalArgumentException(
                    "The first recruited candidate must be the second party member.");
        }
    }
}
