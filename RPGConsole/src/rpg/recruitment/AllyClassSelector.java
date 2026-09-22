package rpg.recruitment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassDefinition;
import rpg.classes.paths.ClassPathCatalog;
import rpg.classes.paths.ClassPathDefinition;
import rpg.classes.paths.ClassPathRules;

// Selects an eligible final class for a recruited ally.
public final class AllyClassSelector {

	private final ClassPathCatalog classPathCatalog;
	private final PlayableClassCatalog playableClassCatalog;
	private final Random random;

	public AllyClassSelector(
			ClassPathCatalog classPathCatalog,
			PlayableClassCatalog playableClassCatalog,
			Random random
	) {
		this.classPathCatalog =
				Objects.requireNonNull(
						classPathCatalog,
						"Class path catalog cannot be null.");

		this.playableClassCatalog =
				Objects.requireNonNull(
						playableClassCatalog,
						"Playable class catalog cannot be null.");

		this.random =
				Objects.requireNonNull(
						random,
						"Random cannot be null.");
	}

	public PlayableClassDefinition selectFor(Party party) {
		return selectFor(party, List.of());
	}

	public PlayableClassDefinition selectFor(
			Party party,
			Collection<String> additionalExcludedClassIds
	) {
		Objects.requireNonNull(
				party,
				"Party cannot be null.");

		Objects.requireNonNull(
				additionalExcludedClassIds,
				"Additional excluded class IDs cannot be null.");

		Set<String> excludedClassIds =
				collectPartyClassIds(party);

		for (String classId : additionalExcludedClassIds) {
			if (classId == null || classId.isBlank()) {
				throw new IllegalArgumentException(
						"Excluded playable class ID cannot be empty."
				);
			}

			PlayableClassDefinition classDefinition =
					playableClassCatalog.getById(
							classId.trim());

			excludedClassIds.add(
					classDefinition.getId());
		}

		List<PlayableClassDefinition> eligibleClasses =
				new ArrayList<>();

		for (ClassPathDefinition pathDefinition
				: classPathCatalog.getAll()) {
			String classId = pathDefinition.getClassId();

			if (classId.equals(
						ClassPathRules.BEGINNER_CLASS_ID)
					|| classId.equals(
							ClassPathRules.FINAL_FLAGELO_CLASS_ID)
					|| excludedClassIds.contains(classId)) {
				continue;
			}

			eligibleClasses.add(
					playableClassCatalog.getById(classId));
		}

		if (eligibleClasses.isEmpty()) {
			throw new IllegalStateException(
					"No eligible ally class is available."
			);
		}

		return eligibleClasses.get(
				random.nextInt(eligibleClasses.size()));
	}

	private static Set<String> collectPartyClassIds(
			Party party
	) {
		Set<String> classIds = new LinkedHashSet<>();

		for (PlayableCharacter member : party.getMembers()) {
			classIds.add(
					member
							.getPlayableClassDefinition()
							.getId());
		}

		return classIds;
	}
}