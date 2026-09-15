package rpg.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import rpg.characters.Party;

// Deserializes the decrypted property format into a campaign snapshot.
public final class CampaignPropertiesDeserializer {

	private CampaignPropertiesDeserializer() {
	}

	public static CampaignSnapshot deserialize(Properties properties) {
		Objects.requireNonNull(properties, "Properties cannot be null.");

		StrictPropertyReader reader = new StrictPropertyReader(properties);

		int formatVersion = reader.readInt("save.formatVersion");

		if (formatVersion != SaveFormat.CURRENT_VERSION) {
			throw new IllegalArgumentException("Unsupported save format version: " + formatVersion);
		}

		PartySnapshot partySnapshot = deserializeParty(reader);
		WorldSnapshot worldSnapshot = deserializeWorld(reader);
		List<ShopSnapshot> shopSnapshots = deserializeShops(reader);

		reader.rejectUnreadProperties();

		return new CampaignSnapshot(partySnapshot, worldSnapshot, shopSnapshots);
	}

	private static PartySnapshot deserializeParty(StrictPropertyReader reader) {
		int gold = reader.readInt("party.gold");

		RewardModifierSnapshot experienceModifier = deserializeRewardModifier(
				reader,
				"party.rewardModifier.experience"
		);

		RewardModifierSnapshot goldModifier = deserializeRewardModifier(reader, "party.rewardModifier.gold");

		int memberCount = reader.readCount("party.member.count");

		if (memberCount < Party.MINIMUM_SIZE || memberCount > Party.MAXIMUM_SIZE) {
			throw new IllegalArgumentException(
					"Party member count must be between " + Party.MINIMUM_SIZE + " and " + Party.MAXIMUM_SIZE + "."
			);
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
							reader.readInt(prefix + ".quantity")
					)
			);
		}

		return new PartySnapshot(gold, members, inventoryEntries, experienceModifier, goldModifier);
	}

	private static RewardModifierSnapshot deserializeRewardModifier(StrictPropertyReader reader, String prefix) {
		return new RewardModifierSnapshot(
				reader.readInt(prefix + ".percent"),
				reader.readInt(prefix + ".remainingVictories")
		);
	}

	private static PlayableCharacterSnapshot deserializeMember(StrictPropertyReader reader, int memberIndex) {
		String prefix = "party.member." + memberIndex;

		String name = reader.readRequired(prefix + ".name");
		String classId = reader.readRequired(prefix + ".classId");
		int level = reader.readInt(prefix + ".level");
		int experience = reader.readInt(prefix + ".experience");
		int currentHp = reader.readInt(prefix + ".currentHp");
		int currentMp = reader.readInt(prefix + ".currentMp");
		int currentSp = reader.readInt(prefix + ".currentSp");

		EquipmentSnapshot equipment = new EquipmentSnapshot(
				reader.readNullableId(prefix + ".equipment.weaponItemId"),
				reader.readNullableId(prefix + ".equipment.armorItemId"),
				reader.readNullableId(prefix + ".equipment.trinketItemId")
		);

		int learnedSkillCount = reader.readCount(prefix + ".learnedSkill.count");

		List<String> learnedSkillIds = new ArrayList<>();

		for (int skillIndex = 0; skillIndex < learnedSkillCount; skillIndex++) {
			learnedSkillIds.add(reader.readRequired(prefix + ".learnedSkill." + skillIndex + ".id"));
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
				learnedSkillIds
		);
	}

	private static WorldSnapshot deserializeWorld(StrictPropertyReader reader) {
		String currentScenarioId = reader.readRequired("world.currentScenarioId");
		long elapsedHours = reader.readLong("world.elapsedHours");

		int discoveredScenarioCount = reader.readCount("world.discoveredScenario.count");

		List<String> discoveredScenarioIds = new ArrayList<>();

		for (int index = 0; index < discoveredScenarioCount; index++) {
			discoveredScenarioIds.add(reader.readRequired("world.discoveredScenario." + index + ".id"));
		}

		int unlockedRouteCount = reader.readCount("travel.unlockedRoute.count");

		List<String> unlockedRouteIds = new ArrayList<>();

		for (int index = 0; index < unlockedRouteCount; index++) {
			unlockedRouteIds.add(reader.readRequired("travel.unlockedRoute." + index + ".id"));
		}

		return new WorldSnapshot(currentScenarioId, elapsedHours, discoveredScenarioIds, unlockedRouteIds);
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
								reader.readInt(offerPrefix + ".remainingStock")
						)
				);
			}

			shops.add(new ShopSnapshot(shopId, offers));
		}

		return List.copyOf(shops);
	}
}