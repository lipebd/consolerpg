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

// Creates configurable temporary parties for development runs.
public final class DevelopmentPartyFactory {

	private static final List<MemberConfiguration> PARTY_CONFIGURATION = List.of(
			new MemberConfiguration("Maximiliano Dias", "flagelo", 7),
			new MemberConfiguration("Jurema Souza", "espectro", 7),
			new MemberConfiguration("Ivete Sangalo", "bardo", 7),
			new MemberConfiguration("Lady Voldemort", "metamorfo", 7)
	);

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

		return party;
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
			if (configuration.level < 1) {
				throw new IllegalStateException("Development character level must be positive: " + configuration.name);
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

		PlayableCharacter character = new PlayableCharacter(
				configuration.name,
				classDefinition,
				skillCatalog.getByClassId(classDefinition.getId()),
				growthCalculator
		);

		while (character.getLevel() < configuration.level) {
			character.gainExperience(character.getExperienceRequiredForNextLevel());
		}

		character.restoreAllResources();

		return character;
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