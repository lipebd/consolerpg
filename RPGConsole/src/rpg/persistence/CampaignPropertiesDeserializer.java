package rpg.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

import rpg.characters.Party;

// Deserializes the decrypted property format into a campaign snapshot.
public final class CampaignPropertiesDeserializer {

	private CampaignPropertiesDeserializer() {}

	public static CampaignSnapshot deserialize(Properties properties) {
		Objects.requireNonNull(properties, "Properties cannot be null.");

		StrictPropertyReader reader = new StrictPropertyReader(properties);

		int formatVersion = reader.readInt("save.formatVersion");

		if (formatVersion == 2) {
			throw new IllegalArgumentException(
					"Save format version 2 is not supported and cannot be migrated."
					);
		}

		if (formatVersion != SaveFormat.CURRENT_VERSION && formatVersion != SaveFormat.PREVIOUS_VERSION) {
			throw new IllegalArgumentException(
					"Unsupported save format version: " + formatVersion
					);
		}

		UUID campaignId = formatVersion == SaveFormat.PREVIOUS_VERSION
				? UUID.randomUUID()
				: readCampaignId(reader);

		PartySnapshot partySnapshot = deserializeParty(reader);
		WorldSnapshot worldSnapshot = deserializeWorld(reader);
		List<ShopSnapshot> shopSnapshots = deserializeShops(reader);
		QuestProgressSnapshot questProgressSnapshot = deserializeQuestProgress(reader);
		CampaignFlagsSnapshot campaignFlagsSnapshot = deserializeCampaignFlags(reader);
		List<RecruitmentCandidateSnapshot> recruitmentCandidates =
				deserializeRecruitmentCandidates(reader);
		BestiarySnapshot bestiarySnapshot = deserializeBestiary(reader);

		reader.rejectUnreadProperties();

		return new CampaignSnapshot(
				campaignId,
				partySnapshot,
				worldSnapshot,
				shopSnapshots,
				questProgressSnapshot,
				campaignFlagsSnapshot,
				recruitmentCandidates,
				bestiarySnapshot
				);
	}

	private static UUID readCampaignId(StrictPropertyReader reader) {
		String value = reader.readRequired("campaign.id");
		UUID campaignId = UUID.fromString(value);
		if (!campaignId.toString().equals(value)) {
			throw new IllegalArgumentException("Campaign ID must be a canonical UUID.");
		}
		return campaignId;
	}

	private static BestiarySnapshot deserializeBestiary(StrictPropertyReader reader) {
		int discoveredEnemyCount = reader.readCount("bestiary.discovered.count");
		List<String> discoveredEnemyIds = new ArrayList<>();

		for (int index = 0; index < discoveredEnemyCount; index++) {
			discoveredEnemyIds.add(
					reader.readRequired("bestiary.discovered." + index + ".id")
			);
		}

		return new BestiarySnapshot(discoveredEnemyIds);
	}

	private static PartySnapshot deserializeParty(StrictPropertyReader reader) {
		int gold = reader.readInt("party.gold");

		RewardModifierSnapshot experienceModifier =
				deserializeRewardModifier(reader, "party.rewardModifier.experience");

		RewardModifierSnapshot goldModifier =
				deserializeRewardModifier(reader, "party.rewardModifier.gold");

		int memberCount = reader.readCount("party.member.count");

		if (memberCount < Party.MINIMUM_SIZE || memberCount > Party.MAXIMUM_SIZE) {
			throw new IllegalArgumentException(
					"Party member count must be between "
							+ Party.MINIMUM_SIZE
							+ " and "
							+ Party.MAXIMUM_SIZE
							+ ".");
		}

		List<PlayableCharacterSnapshot> members = new ArrayList<>();

		for (int memberIndex = 0; memberIndex < memberCount; memberIndex++) {
			members.add(deserializeMember(reader, memberIndex));
		}

		int inventoryCount = reader.readCount("party.inventory.count");

		List<InventoryEntrySnapshot> inventoryEntries = new ArrayList<>();

		for (int inventoryIndex = 0; inventoryIndex < inventoryCount; inventoryIndex++) {
			String prefix = "party.inventory." + inventoryIndex;

			inventoryEntries.add(
					new InventoryEntrySnapshot(
							reader.readRequired(prefix + ".itemId"),
							reader.readInt(prefix + ".quantity")));
		}

		return new PartySnapshot(gold, members, inventoryEntries, experienceModifier, goldModifier);
	}

	private static RewardModifierSnapshot deserializeRewardModifier(
			StrictPropertyReader reader, String prefix) {
		return new RewardModifierSnapshot(
				reader.readInt(prefix + ".percent"),
				reader.readInt(prefix + ".remainingVictories"));
	}

	private static PlayableCharacterSnapshot deserializeMember(
			StrictPropertyReader reader, int memberIndex) {
		String prefix = "party.member." + memberIndex;

		String name = reader.readRequired(prefix + ".name");
		String classId = reader.readRequired(prefix + ".classId");
		int level = reader.readInt(prefix + ".level");
		int experience = reader.readInt(prefix + ".experience");
		int currentHp = reader.readInt(prefix + ".currentHp");
		int currentMp = reader.readInt(prefix + ".currentMp");
		int currentSp = reader.readInt(prefix + ".currentSp");

		EquipmentSnapshot equipment =
				new EquipmentSnapshot(
						reader.readNullableId(prefix + ".equipment.weaponItemId"),
						reader.readNullableId(prefix + ".equipment.armorItemId"),
						reader.readNullableId(prefix + ".equipment.trinketItemId"));

		int learnedSkillCount = reader.readCount(prefix + ".learnedSkill.count");

		List<String> learnedSkillIds = new ArrayList<>();

		for (int skillIndex = 0; skillIndex < learnedSkillCount; skillIndex++) {
			learnedSkillIds.add(
					reader.readRequired(prefix + ".learnedSkill." + skillIndex + ".id"));
		}

		return new PlayableCharacterSnapshot(
				name,
				classId,
				level,
				experience,
				currentHp,
				currentMp,
				currentSp,
				equipment,
				learnedSkillIds);
	}

	private static WorldSnapshot deserializeWorld(StrictPropertyReader reader) {
		String currentScenarioId = reader.readRequired("world.currentScenarioId");
		long elapsedHours = reader.readLong("world.elapsedHours");

		int discoveredScenarioCount = reader.readCount("world.discoveredScenario.count");

		List<String> discoveredScenarioIds = new ArrayList<>();

		for (int index = 0; index < discoveredScenarioCount; index++) {
			discoveredScenarioIds.add(
					reader.readRequired("world.discoveredScenario." + index + ".id"));
		}

		int unlockedRouteCount = reader.readCount("travel.unlockedRoute.count");

		List<String> unlockedRouteIds = new ArrayList<>();

		for (int index = 0; index < unlockedRouteCount; index++) {
			unlockedRouteIds.add(reader.readRequired("travel.unlockedRoute." + index + ".id"));
		}

		int dungeonProgressCount = reader.readCount("world.dungeon.count");

		List<DungeonProgressSnapshot> dungeonProgressSnapshots = new ArrayList<>();

		for (int index = 0; index < dungeonProgressCount; index++) {
			String prefix = "world.dungeon." + index;

			dungeonProgressSnapshots.add(
					new DungeonProgressSnapshot(
							reader.readRequired(prefix + ".id"),
							reader.readBoolean(prefix + ".narrativeBossDefeated"),
							reader.readLong(prefix + ".recurringBossAvailableAtHour"),
							reader.readLong(prefix + ".epicAvailableAtHour")));
		}

		return new WorldSnapshot(
				currentScenarioId,
				elapsedHours,
				discoveredScenarioIds,
				unlockedRouteIds,
				dungeonProgressSnapshots);
	}

	private static List<ShopSnapshot> deserializeShops(StrictPropertyReader reader) {
		int shopCount = reader.readCount("shop.count");

		List<ShopSnapshot> shops = new ArrayList<>();

		for (int shopIndex = 0; shopIndex < shopCount; shopIndex++) {
			String shopPrefix = "shop." + shopIndex;

			String shopId = reader.readRequired(shopPrefix + ".id");

			int offerCount = reader.readCount(shopPrefix + ".offer.count");

			List<ShopOfferSnapshot> offers = new ArrayList<>();

			for (int offerIndex = 0; offerIndex < offerCount; offerIndex++) {
				String offerPrefix = shopPrefix + ".offer." + offerIndex;

				offers.add(
						new ShopOfferSnapshot(
								reader.readRequired(offerPrefix + ".stockEntryId"),
								reader.readInt(offerPrefix + ".remainingStock")));
			}

			shops.add(new ShopSnapshot(shopId, offers));
		}

		return List.copyOf(shops);
	}

	private static QuestProgressSnapshot
	deserializeQuestProgress(
			StrictPropertyReader reader
			) {
		int activeQuestCount =
				reader.readCount("quest.active.count");

		List<ActiveQuestSnapshot> activeQuests =
				new ArrayList<>();

		for (int questIndex = 0;
				questIndex < activeQuestCount;
				questIndex++) {
			String questPrefix =
					"quest.active." + questIndex;

			String questId =
					reader.readRequired(
							questPrefix + ".id"
							);

			int currentStep =
					reader.readInt(
							questPrefix + ".currentStep"
							);

			int objectiveCount =
					reader.readCount(
							questPrefix + ".objective.count"
							);

			List<QuestObjectiveProgressSnapshot> objectives =
					new ArrayList<>();

			for (int objectiveIndex = 0;
					objectiveIndex < objectiveCount;
					objectiveIndex++) {
				String objectivePrefix =
						questPrefix
						+ ".objective."
						+ objectiveIndex;

				objectives.add(
						new QuestObjectiveProgressSnapshot(
								reader.readRequired(
										objectivePrefix + ".id"
										),
								reader.readInt(
										objectivePrefix
										+ ".currentQuantity"
										)
								)
						);
			}

			activeQuests.add(
					new ActiveQuestSnapshot(
							questId,
							currentStep,
							objectives
							)
					);
		}

		int completedQuestCount =
				reader.readCount("quest.completed.count");

		List<String> completedQuestIds =
				new ArrayList<>();

		for (int index = 0;
				index < completedQuestCount;
				index++) {
			completedQuestIds.add(
					reader.readRequired(
							"quest.completed."
									+ index
									+ ".id"
							)
					);
		}

		return new QuestProgressSnapshot(
				activeQuests,
				completedQuestIds
				);
	}

	private static CampaignFlagsSnapshot
	deserializeCampaignFlags(
			StrictPropertyReader reader
			) {
		int flagCount =
				reader.readCount("flag.count");

		List<String> setFlagIds =
				new ArrayList<>();

		for (int index = 0;
				index < flagCount;
				index++) {
			setFlagIds.add(
					reader.readRequired(
							"flag." + index + ".id"
							)
					);
		}

		return new CampaignFlagsSnapshot(setFlagIds);
	}

	private static List<RecruitmentCandidateSnapshot>
	deserializeRecruitmentCandidates(
			StrictPropertyReader reader
			) {
		int candidateCount =
				reader.readCount(
						"recruitment.candidate.count"
						);

		List<RecruitmentCandidateSnapshot> candidates =
				new ArrayList<>();

		for (int index = 0;
				index < candidateCount;
				index++) {
			String prefix =
					"recruitment.candidate." + index;

			candidates.add(
					new RecruitmentCandidateSnapshot(
							reader.readRequired(
									prefix + ".id"
									),
							reader.readRequired(
									prefix + ".name"
									),
							reader.readBoolean(
									prefix + ".recruited"
									)
							)
					);
		}

		return List.copyOf(candidates);
	}
}
