package rpg.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import rpg.characters.CharacterNameRules;

// Stores one complete immutable campaign snapshot.
public final class CampaignSnapshot {

	private final PartySnapshot party;
	private final WorldSnapshot world;
	private final List<ShopSnapshot> shops;
	private final QuestProgressSnapshot questProgress;
	private final CampaignFlagsSnapshot campaignFlags;
	private final List<RecruitmentCandidateSnapshot> recruitmentCandidates;
	private final BestiarySnapshot bestiary;

	public CampaignSnapshot(
			PartySnapshot party,
			WorldSnapshot world,
			List<ShopSnapshot> shops,
			QuestProgressSnapshot questProgress,
			CampaignFlagsSnapshot campaignFlags,
			List<RecruitmentCandidateSnapshot> recruitmentCandidates,
			BestiarySnapshot bestiary
	) {
		this.party =
				Objects.requireNonNull(
						party,
						"Party snapshot cannot be null."
				);

		this.world =
				Objects.requireNonNull(
						world,
						"World snapshot cannot be null."
				);

		Objects.requireNonNull(
				shops,
				"Shop snapshots cannot be null."
		);

		Set<String> uniqueShopIds = new HashSet<>();

		for (ShopSnapshot shop : shops) {
			Objects.requireNonNull(
					shop,
					"Shop snapshot cannot be null."
			);

			if (!uniqueShopIds.add(shop.getShopId())) {
				throw new IllegalArgumentException(
						"Shop ID is duplicated: "
								+ shop.getShopId()
				);
			}
		}

		this.shops = List.copyOf(shops);

		this.questProgress =
				Objects.requireNonNull(
						questProgress,
						"Quest progress snapshot cannot be null."
				);

		this.campaignFlags =
				Objects.requireNonNull(
						campaignFlags,
						"Campaign flags snapshot cannot be null."
				);

		Objects.requireNonNull(
				recruitmentCandidates,
				"Recruitment candidate snapshots cannot be null."
		);

		validateRecruitmentCandidates(recruitmentCandidates);

		this.recruitmentCandidates =
				List.copyOf(recruitmentCandidates);

		this.bestiary = Objects.requireNonNull(bestiary, "Bestiary snapshot cannot be null.");
	}

	private static void validateRecruitmentCandidates(
			List<RecruitmentCandidateSnapshot> recruitmentCandidates
	) {
		Set<String> uniqueCandidateIds = new HashSet<>();

		for (int index = 0;
				index < recruitmentCandidates.size();
				index++) {
			RecruitmentCandidateSnapshot candidate =
					Objects.requireNonNull(
							recruitmentCandidates.get(index),
							"Recruitment candidate snapshot cannot be null."
					);

			if (!uniqueCandidateIds.add(
					candidate.getCandidateId())) {
				throw new IllegalArgumentException(
						"Recruitment candidate ID is duplicated: "
								+ candidate.getCandidateId()
				);
			}

			for (int comparedIndex = 0;
					comparedIndex < index;
					comparedIndex++) {
				String previousName =
						recruitmentCandidates
								.get(comparedIndex)
								.getName();

				if (CharacterNameRules.areEquivalent(
						previousName,
						candidate.getName())) {
					throw new IllegalArgumentException(
							"Recruitment candidate name is duplicated: "
									+ candidate.getName()
					);
				}
			}
		}
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

	public QuestProgressSnapshot getQuestProgress() {
		return questProgress;
	}

	public CampaignFlagsSnapshot getCampaignFlags() {
		return campaignFlags;
	}

	public List<RecruitmentCandidateSnapshot>
			getRecruitmentCandidates() {
		return recruitmentCandidates;
	}

	public BestiarySnapshot getBestiary() {
		return bestiary;
	}
}
