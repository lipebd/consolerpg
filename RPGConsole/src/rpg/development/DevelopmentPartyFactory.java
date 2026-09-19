package rpg.development;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.classes.CharacterGrowthCalculator;
import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassDefinition;
import rpg.skills.SkillCatalog;
import rpg.battle.rewards.PartyRewardModifierState;
import rpg.characters.PlayableCharacterLevelRules;

// Creates configurable temporary parties for development runs.
public final class DevelopmentPartyFactory {

	private static final List<MemberConfiguration> PARTY_CONFIGURATION = List.of(
			new MemberConfiguration("Flagelinho Silva", "flagelo_iniciante", 1),
			new MemberConfiguration("La Metamorfica", "metamorfo", 1),
			new MemberConfiguration("Suzana Vieira", "atirador", 1),
			new MemberConfiguration("Michael Jackson", "druida", 1)
	);

	private static final int INITIAL_GOLD = 5000;
	private static final boolean TEST_MODIFIERS = false;
	private static final int EXP_MODIFIER_PERCENTAGE = 50;
	private static final int EXP_MODIFIER_USES = 3;
	private static final int GOLD_MODIFIER_PERCENTAGE = 25;
	private static final int GOLD_MODIFIER_USES = 4;

	private final CharacterGrowthCalculator growthCalculator;
	private final PlayableClassCatalog playableClassCatalog;
	private final SkillCatalog skillCatalog;

	public DevelopmentPartyFactory(
			PlayableClassCatalog playableClassCatalog,
			SkillCatalog skillCatalog,
			CharacterGrowthCalculator growthCalculator
	) {
		this.playableClassCatalog = Objects
				.requireNonNull(playableClassCatalog, "Playable class catalog cannot be null.");

		this.skillCatalog = Objects.requireNonNull(skillCatalog, "Skill catalog cannot be null.");

		this.growthCalculator = Objects.requireNonNull(growthCalculator, "Character growth calculator cannot be null.");
	}

	public Party create() {
		validateConfiguration();

		PlayableCharacter initialMember = createCharacter(PARTY_CONFIGURATION.get(0));

		Party party = new Party(initialMember);

		for (int index = 1; index < PARTY_CONFIGURATION.size(); index++) {
			PlayableCharacter member = createCharacter(PARTY_CONFIGURATION.get(index));

			if (!party.addMember(member)) {
				throw new IllegalStateException(
						"Configured development-party member could not be added: " + member.getName()
				);
			}
		}

		if (!party.addGold(INITIAL_GOLD)) {
			throw new IllegalStateException(
					"Development-party initial GOLD exceeds the supported maximum: " + INITIAL_GOLD
			);
		}

		if (TEST_MODIFIERS) {
			applyModifiers(
					party,
					EXP_MODIFIER_PERCENTAGE,
					EXP_MODIFIER_USES,
					GOLD_MODIFIER_PERCENTAGE,
					GOLD_MODIFIER_USES
			);
		}

		return party;
	}

	private void applyModifiers(
			Party party,
			int expModifierPercentage,
			int expModifierUses,
			int goldModifierPercentage,
			int goldModifierUses
	) {
		PartyRewardModifierState rewardModifierState = party.getRewardModifierState();
		rewardModifierState.activateExperienceModifier(expModifierPercentage, expModifierUses);
		rewardModifierState.activateGoldModifier(goldModifierPercentage, goldModifierUses);
	}

	private void validateConfiguration() {
		if (PARTY_CONFIGURATION.size() < Party.MINIMUM_SIZE || PARTY_CONFIGURATION.size() > Party.MAXIMUM_SIZE) {
			throw new IllegalStateException(
					"Development party must contain between " + Party.MINIMUM_SIZE + " and " + Party.MAXIMUM_SIZE
							+ " members."
			);
		}

		Set<String> configuredClassIds = new HashSet<>();

		for (MemberConfiguration configuration : PARTY_CONFIGURATION) {
			if (configuration.level < PlayableCharacterLevelRules.MINIMUM_LEVEL
					|| configuration.level > PlayableCharacterLevelRules.MAXIMUM_LEVEL) {
				throw new IllegalStateException(
						"Development character level must be between " + PlayableCharacterLevelRules.MINIMUM_LEVEL
								+ " and " + PlayableCharacterLevelRules.MAXIMUM_LEVEL + ": " + configuration.name
				);
			}

			PlayableClassDefinition classDefinition = playableClassCatalog.getById(configuration.classId);

			if (!configuredClassIds.add(classDefinition.getId())) {
				throw new IllegalStateException(
						"Development party cannot contain repeated classes: " + classDefinition.getId()
				);
			}
		}
	}

	private PlayableCharacter createCharacter(MemberConfiguration configuration) {
		PlayableClassDefinition classDefinition = playableClassCatalog.getById(configuration.classId);

		return new PlayableCharacter(
				configuration.name,
				classDefinition,
				skillCatalog.getByClassId(classDefinition.getId()),
				growthCalculator,
				configuration.level
		);
	}

	private static final class MemberConfiguration {

		private final String name;
		private final String classId;
		private final int level;

		private MemberConfiguration(String name, String classId, int level) {
			this.name = name;
			this.classId = classId;
			this.level = level;
		}
	}
}