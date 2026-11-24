package rpg.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import rpg.battle.rewards.PartyRewardModifierState;
import rpg.characters.CharacterEquipment;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.flags.CampaignFlags;
import rpg.items.Item;
import rpg.quests.QuestDefinition;
import rpg.quests.QuestObjectiveDefinition;
import rpg.quests.QuestProgress;
import rpg.quests.QuestStepDefinition;
import rpg.recruitment.RecruitmentCandidateState;
import rpg.recruitment.RecruitmentCandidateStateCatalog;
import rpg.skills.SkillDefinition;
import rpg.world.WorldClock;
import rpg.world.WorldProgress;
import rpg.world.bestiary.BestiaryStateCatalog;
import rpg.world.scenarios.DungeonProgress;
import rpg.world.scenarios.Scenario;
import rpg.world.shops.ShopOfferState;
import rpg.world.shops.ShopState;
import rpg.world.shops.ShopStateCatalog;
import rpg.world.travel.TravelProgress;

// Creates an immutable campaign snapshot from the current runtime state.
public final class CampaignSnapshotFactory {

	private CampaignSnapshotFactory() {}

	public static CampaignSnapshot create(
			UUID campaignId,
			Party party,
			Scenario currentScenario,
			WorldProgress worldProgress,
			TravelProgress travelProgress,
			DungeonProgress dungeonProgress,
			WorldClock worldClock,
			ShopStateCatalog shopStateCatalog,
			QuestProgress questProgress,
			CampaignFlags campaignFlags,
			RecruitmentCandidateStateCatalog candidateStateCatalog,
			BestiaryStateCatalog bestiaryStateCatalog
			) {
		Objects.requireNonNull(party, "Party cannot be null.");
		Objects.requireNonNull(currentScenario, "Current scenario cannot be null.");
		Objects.requireNonNull(worldProgress, "World progress cannot be null.");
		Objects.requireNonNull(travelProgress, "Travel progress cannot be null.");
		Objects.requireNonNull(dungeonProgress, "Dungeon progress cannot be null.");
		Objects.requireNonNull(worldClock, "World clock cannot be null.");
		Objects.requireNonNull(shopStateCatalog, "Shop state catalog cannot be null.");
		Objects.requireNonNull(questProgress, "Quest progress cannot be null.");
		Objects.requireNonNull(campaignFlags, "Campaign flags cannot be null.");
		Objects.requireNonNull(candidateStateCatalog, "Candidate state catalog cannot be null.");
		Objects.requireNonNull(bestiaryStateCatalog, "Bestiary state catalog cannot be null.");

		PartySnapshot partySnapshot = createPartySnapshot(party);

		WorldSnapshot worldSnapshot =
				createWorldSnapshot(
						currentScenario,
						worldProgress,
						travelProgress,
						dungeonProgress,
						worldClock);

		List<ShopSnapshot> shopSnapshots = createShopSnapshots(shopStateCatalog);

		QuestProgressSnapshot questProgressSnapshot =
				createQuestProgressSnapshot(questProgress);

		CampaignFlagsSnapshot campaignFlagsSnapshot =
				createCampaignFlagsSnapshot(campaignFlags);

		List<RecruitmentCandidateSnapshot> candidateSnapshots =
				createRecruitmentCandidateSnapshots(
						candidateStateCatalog
						);

		BestiarySnapshot bestiarySnapshot =
				new BestiarySnapshot(bestiaryStateCatalog.getDiscoveredEnemyIds());

		return new CampaignSnapshot(
				campaignId,
				partySnapshot,
				worldSnapshot,
				shopSnapshots,
				questProgressSnapshot,
				campaignFlagsSnapshot,
				candidateSnapshots,
				bestiarySnapshot
				);
	}

	private static PartySnapshot createPartySnapshot(Party party) {
		List<PlayableCharacterSnapshot> memberSnapshots = new ArrayList<>();

		for (PlayableCharacter member : party.getMembers()) {
			memberSnapshots.add(createPlayableCharacterSnapshot(member));
		}

		List<InventoryEntrySnapshot> inventoryEntries = new ArrayList<>();

		for (Map.Entry<Item, Integer> entry : party.getInventory().getContents().entrySet()) {
			inventoryEntries.add(
					new InventoryEntrySnapshot(entry.getKey().getId(), entry.getValue()));
		}

		PartyRewardModifierState modifierState = party.getRewardModifierState();

		RewardModifierSnapshot experienceModifier =
				new RewardModifierSnapshot(
						modifierState.getExperienceModifierPercent(),
						modifierState.getExperienceVictoriesRemaining());

		RewardModifierSnapshot goldModifier =
				new RewardModifierSnapshot(
						modifierState.getGoldModifierPercent(),
						modifierState.getGoldVictoriesRemaining());

		return new PartySnapshot(
				party.getGold(),
				memberSnapshots,
				inventoryEntries,
				experienceModifier,
				goldModifier);
	}

	private static PlayableCharacterSnapshot createPlayableCharacterSnapshot(
			PlayableCharacter character) {
		CharacterEquipment equipment = character.getEquipment();

		EquipmentSnapshot equipmentSnapshot =
				new EquipmentSnapshot(
						equipment
						.getWeapon()
						.map(definition -> definition.getItemId())
						.orElse(null),
						equipment.getArmor().map(definition -> definition.getItemId()).orElse(null),
						equipment
						.getTrinket()
						.map(definition -> definition.getItemId())
						.orElse(null));

		List<String> learnedSkillIds =
				character.getLearnedScrollSkillDefinitions().stream()
				.map(SkillDefinition::getId)
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
				learnedSkillIds);
	}

	private static WorldSnapshot createWorldSnapshot(
			Scenario currentScenario,
			WorldProgress worldProgress,
			TravelProgress travelProgress,
			DungeonProgress dungeonProgress,
			WorldClock worldClock) {
		List<String> discoveredScenarioIds =
				worldProgress.getDiscoveredScenarioIds().stream().sorted().toList();

		List<String> unlockedRouteIds =
				travelProgress.getUnlockedRouteIds().stream().sorted().toList();

		List<String> dungeonIds = dungeonProgress.getDungeonIds().stream().sorted().toList();

		List<DungeonProgressSnapshot> dungeonProgressSnapshots = new ArrayList<>();

		for (String dungeonId : dungeonIds) {
			dungeonProgressSnapshots.add(
					new DungeonProgressSnapshot(
							dungeonId,
							dungeonProgress.isNarrativeBossDefeated(dungeonId),
							dungeonProgress.getRecurringBossAvailableAtHour(dungeonId),
							dungeonProgress.getEpicAvailableAtHour(dungeonId)));
		}
		return new WorldSnapshot(
				currentScenario.getId(),
				worldClock.getElapsedHours(),
				discoveredScenarioIds,
				unlockedRouteIds,
				dungeonProgressSnapshots);
	}

	private static List<ShopSnapshot> createShopSnapshots(ShopStateCatalog shopStateCatalog) {
		List<ShopSnapshot> shopSnapshots = new ArrayList<>();

		for (ShopState shopState : shopStateCatalog.getAll()) {
			List<ShopOfferSnapshot> offerSnapshots = new ArrayList<>();

			for (ShopOfferState offerState : shopState.getActiveOffers()) {
				offerSnapshots.add(
						new ShopOfferSnapshot(
								offerState.getStockEntry().getId(),
								offerState.getRemainingStock()));
			}

			shopSnapshots.add(new ShopSnapshot(shopState.getDefinition().getId(), offerSnapshots));
		}

		return List.copyOf(shopSnapshots);
	}

	private static QuestProgressSnapshot
	createQuestProgressSnapshot(
			QuestProgress questProgress
			) {
		List<ActiveQuestSnapshot> activeQuestSnapshots =
				new ArrayList<>();

		for (QuestDefinition questDefinition
				: questProgress.getActiveQuests()) {
			QuestStepDefinition currentStep =
					questProgress
					.getCurrentStep(
							questDefinition.getId()
							)
					.orElseThrow(
							() ->
							new IllegalStateException(
									"Active quest has no current step: "
											+ questDefinition
											.getId()
									)
							);

			List<QuestObjectiveProgressSnapshot>
			objectiveSnapshots =
			new ArrayList<>();

			for (QuestObjectiveDefinition objective
					: currentStep.getObjectives()) {
				objectiveSnapshots.add(
						new QuestObjectiveProgressSnapshot(
								objective.getId(),
								questProgress
								.getObjectiveProgress(
										questDefinition
										.getId(),
										objective.getId()
										)
								)
						);
			}

			activeQuestSnapshots.add(
					new ActiveQuestSnapshot(
							questDefinition.getId(),
							currentStep.getStep(),
							objectiveSnapshots
							)
					);
		}

		List<String> completedQuestIds =
				questProgress
				.getCompletedQuests()
				.stream()
				.map(QuestDefinition::getId)
				.toList();

		return new QuestProgressSnapshot(
				activeQuestSnapshots,
				completedQuestIds
				);
	}

	private static CampaignFlagsSnapshot
	createCampaignFlagsSnapshot(
			CampaignFlags campaignFlags
			) {
		List<String> setFlagIds =
				campaignFlags
				.getSetFlags()
				.stream()
				.map(flag -> flag.getId())
				.toList();

		return new CampaignFlagsSnapshot(setFlagIds);
	}

	private static List<RecruitmentCandidateSnapshot>
	createRecruitmentCandidateSnapshots(
			RecruitmentCandidateStateCatalog
			candidateStateCatalog
			) {
		List<RecruitmentCandidateSnapshot> snapshots =
				new ArrayList<>();

		for (RecruitmentCandidateState candidateState
				: candidateStateCatalog.getAll()) {
			snapshots.add(
					new RecruitmentCandidateSnapshot(
							candidateState.getCandidateId(),
							candidateState.getName(),
							candidateState.isRecruited()
							)
					);
		}

		return List.copyOf(snapshots);
	}
}
