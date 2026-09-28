package rpg.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

// Serializes a campaign snapshot into the decrypted property format.
public final class CampaignPropertiesSerializer {

    private CampaignPropertiesSerializer() {}

	public static Properties serialize(CampaignSnapshot campaignSnapshot) {
		Objects.requireNonNull(campaignSnapshot, "Campaign snapshot cannot be null.");

		Properties properties = new Properties();

		properties.setProperty("save.formatVersion", Integer.toString(SaveFormat.CURRENT_VERSION));

		serializeParty(properties, campaignSnapshot.getParty());
		serializeWorld(properties, campaignSnapshot.getWorld());
        serializeShops(properties, campaignSnapshot.getShops());
        serializeQuestProgress(properties, campaignSnapshot.getQuestProgress());
        serializeCampaignFlags(properties, campaignSnapshot.getCampaignFlags());
        serializeRecruitmentCandidates(properties, campaignSnapshot.getRecruitmentCandidates());
		serializeBestiary(properties, campaignSnapshot.getBestiary());

		return properties;
	}

	private static void serializeBestiary(Properties properties, BestiarySnapshot snapshot) {
		List<String> discoveredEnemyIds = snapshot.getDiscoveredEnemyIds();

		properties.setProperty(
				"bestiary.discovered.count",
				Integer.toString(discoveredEnemyIds.size())
		);

		for (int index = 0; index < discoveredEnemyIds.size(); index++) {
			properties.setProperty(
					"bestiary.discovered." + index + ".id",
					discoveredEnemyIds.get(index)
			);
		}
	}

	private static void serializeParty(Properties properties, PartySnapshot partySnapshot) {
		properties.setProperty("party.gold", Integer.toString(partySnapshot.getGold()));

		serializeRewardModifier(
				properties,
				"party.rewardModifier.experience",
                partySnapshot.getExperienceRewardModifier());

        serializeRewardModifier(
                properties, "party.rewardModifier.gold", partySnapshot.getGoldRewardModifier());

		List<PlayableCharacterSnapshot> members = partySnapshot.getMembers();

		properties.setProperty("party.member.count", Integer.toString(members.size()));

		for (int memberIndex = 0; memberIndex < members.size(); memberIndex++) {
			serializeMember(properties, memberIndex, members.get(memberIndex));
		}

		List<InventoryEntrySnapshot> inventoryEntries = partySnapshot.getInventoryEntries();

		properties.setProperty("party.inventory.count", Integer.toString(inventoryEntries.size()));

		for (int inventoryIndex = 0; inventoryIndex < inventoryEntries.size(); inventoryIndex++) {
			InventoryEntrySnapshot entry = inventoryEntries.get(inventoryIndex);

			String prefix = "party.inventory." + inventoryIndex;

			properties.setProperty(prefix + ".itemId", entry.getItemId());
			properties.setProperty(prefix + ".quantity", Integer.toString(entry.getQuantity()));
		}
	}

    private static void serializeRewardModifier(
            Properties properties, String prefix, RewardModifierSnapshot modifierSnapshot) {
        properties.setProperty(
                prefix + ".percent", Integer.toString(modifierSnapshot.getModifierPercent()));

		properties.setProperty(
				prefix + ".remainingVictories",
                Integer.toString(modifierSnapshot.getVictoriesRemaining()));
	}

    private static void serializeMember(
            Properties properties, int memberIndex, PlayableCharacterSnapshot member) {
		String prefix = "party.member." + memberIndex;

		properties.setProperty(prefix + ".name", member.getName());
		properties.setProperty(prefix + ".classId", member.getClassId());
		properties.setProperty(prefix + ".level", Integer.toString(member.getLevel()));
		properties.setProperty(prefix + ".experience", Integer.toString(member.getExperience()));
		properties.setProperty(prefix + ".currentHp", Integer.toString(member.getCurrentHp()));
		properties.setProperty(prefix + ".currentMp", Integer.toString(member.getCurrentMp()));
		properties.setProperty(prefix + ".currentSp", Integer.toString(member.getCurrentSp()));

		EquipmentSnapshot equipment = member.getEquipment();

        properties.setProperty(
                prefix + ".equipment.weaponItemId", equipment.getWeaponItemId().orElse(""));

        properties.setProperty(
                prefix + ".equipment.armorItemId", equipment.getArmorItemId().orElse(""));

        properties.setProperty(
                prefix + ".equipment.trinketItemId", equipment.getTrinketItemId().orElse(""));

		List<String> learnedSkillIds = member.getLearnedSkillIds();

        properties.setProperty(
                prefix + ".learnedSkill.count", Integer.toString(learnedSkillIds.size()));

		for (int skillIndex = 0; skillIndex < learnedSkillIds.size(); skillIndex++) {
            properties.setProperty(
                    prefix + ".learnedSkill." + skillIndex + ".id",
                    learnedSkillIds.get(skillIndex));
		}
	}

	private static void serializeWorld(Properties properties, WorldSnapshot worldSnapshot) {
		properties.setProperty("world.currentScenarioId", worldSnapshot.getCurrentScenarioId());
        properties.setProperty(
                "world.elapsedHours", Long.toString(worldSnapshot.getElapsedHours()));

		List<String> discoveredScenarioIds = worldSnapshot.getDiscoveredScenarioIds();

        properties.setProperty(
                "world.discoveredScenario.count", Integer.toString(discoveredScenarioIds.size()));

		for (int index = 0; index < discoveredScenarioIds.size(); index++) {
            properties.setProperty(
                    "world.discoveredScenario." + index + ".id", discoveredScenarioIds.get(index));
		}

		List<String> unlockedRouteIds = worldSnapshot.getUnlockedRouteIds();

        properties.setProperty(
                "travel.unlockedRoute.count", Integer.toString(unlockedRouteIds.size()));

		for (int index = 0; index < unlockedRouteIds.size(); index++) {
            properties.setProperty(
                    "travel.unlockedRoute." + index + ".id", unlockedRouteIds.get(index));
        }

        List<DungeonProgressSnapshot> dungeonProgressSnapshots =
                worldSnapshot.getDungeonProgressSnapshots();

        properties.setProperty(
                "world.dungeon.count", Integer.toString(dungeonProgressSnapshots.size()));

        for (int index = 0; index < dungeonProgressSnapshots.size(); index++) {
            DungeonProgressSnapshot snapshot = dungeonProgressSnapshots.get(index);
            String prefix = "world.dungeon." + index;

            properties.setProperty(prefix + ".id", snapshot.getDungeonId());

            properties.setProperty(
                    prefix + ".narrativeBossDefeated",
                    Boolean.toString(snapshot.isNarrativeBossDefeated()));

            properties.setProperty(
                    prefix + ".recurringBossAvailableAtHour",
                    Long.toString(snapshot.getRecurringBossAvailableAtHour()));

            properties.setProperty(
                    prefix + ".epicAvailableAtHour",
                    Long.toString(snapshot.getEpicAvailableAtHour()));
		}
	}

	private static void serializeShops(Properties properties, List<ShopSnapshot> shops) {
		properties.setProperty("shop.count", Integer.toString(shops.size()));

		for (int shopIndex = 0; shopIndex < shops.size(); shopIndex++) {
			ShopSnapshot shop = shops.get(shopIndex);
			String shopPrefix = "shop." + shopIndex;

			properties.setProperty(shopPrefix + ".id", shop.getShopId());

			List<ShopOfferSnapshot> offers = shop.getActiveOffers();

			properties.setProperty(shopPrefix + ".offer.count", Integer.toString(offers.size()));

			for (int offerIndex = 0; offerIndex < offers.size(); offerIndex++) {
				ShopOfferSnapshot offer = offers.get(offerIndex);
				String offerPrefix = shopPrefix + ".offer." + offerIndex;

				properties.setProperty(offerPrefix + ".stockEntryId", offer.getStockEntryId());

                properties.setProperty(
                        offerPrefix + ".remainingStock",
                        Integer.toString(offer.getRemainingStock()));
			}
		}
	}

	private static void serializeQuestProgress(
			Properties properties,
			QuestProgressSnapshot questProgressSnapshot
	) {
		List<ActiveQuestSnapshot> activeQuests =
				questProgressSnapshot.getActiveQuests();

		properties.setProperty(
				"quest.active.count",
				Integer.toString(activeQuests.size())
		);

		for (int questIndex = 0;
				questIndex < activeQuests.size();
				questIndex++) {
			ActiveQuestSnapshot quest =
					activeQuests.get(questIndex);

			String questPrefix =
					"quest.active." + questIndex;

			properties.setProperty(
					questPrefix + ".id",
					quest.getQuestId()
			);

			properties.setProperty(
					questPrefix + ".currentStep",
					Integer.toString(quest.getCurrentStep())
			);

			List<QuestObjectiveProgressSnapshot> objectives =
					quest.getObjectiveProgressSnapshots();

			properties.setProperty(
					questPrefix + ".objective.count",
					Integer.toString(objectives.size())
			);

			for (int objectiveIndex = 0;
					objectiveIndex < objectives.size();
					objectiveIndex++) {
				QuestObjectiveProgressSnapshot objective =
						objectives.get(objectiveIndex);

				String objectivePrefix =
						questPrefix
								+ ".objective."
								+ objectiveIndex;

				properties.setProperty(
						objectivePrefix + ".id",
						objective.getObjectiveId()
				);

				properties.setProperty(
						objectivePrefix + ".currentQuantity",
						Integer.toString(
								objective.getCurrentQuantity()
						)
				);
			}
		}

		List<String> completedQuestIds =
				questProgressSnapshot.getCompletedQuestIds();

		properties.setProperty(
				"quest.completed.count",
				Integer.toString(completedQuestIds.size())
		);

		for (int index = 0;
				index < completedQuestIds.size();
				index++) {
			properties.setProperty(
					"quest.completed." + index + ".id",
					completedQuestIds.get(index)
			);
		}
	}

	private static void serializeCampaignFlags(
			Properties properties,
			CampaignFlagsSnapshot campaignFlagsSnapshot
	) {
		List<String> setFlagIds =
				campaignFlagsSnapshot.getSetFlagIds();

		properties.setProperty(
				"flag.count",
				Integer.toString(setFlagIds.size())
		);

		for (int index = 0;
				index < setFlagIds.size();
				index++) {
			properties.setProperty(
					"flag." + index + ".id",
					setFlagIds.get(index)
			);
		}
	}

	private static void serializeRecruitmentCandidates(
			Properties properties,
			List<RecruitmentCandidateSnapshot> candidates
	) {
		properties.setProperty(
				"recruitment.candidate.count",
				Integer.toString(candidates.size())
		);

		for (int index = 0;
				index < candidates.size();
				index++) {
			RecruitmentCandidateSnapshot candidate =
					candidates.get(index);

			String prefix =
					"recruitment.candidate." + index;

			properties.setProperty(
					prefix + ".id",
					candidate.getCandidateId()
			);

			properties.setProperty(
					prefix + ".name",
					candidate.getName()
			);

			properties.setProperty(
					prefix + ".recruited",
					Boolean.toString(
							candidate.isRecruited()
					)
			);
		}
	}
}
