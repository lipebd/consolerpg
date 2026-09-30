package rpg.characters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;

// Selects random character names while excluding names already in use.
public final class CharacterNameSelector {

	private final CharacterNameCatalog catalog;
	private final Random random;

	public CharacterNameSelector(CharacterNameCatalog catalog, Random random) {
		this.catalog = Objects.requireNonNull(catalog, "Character-name catalog cannot be null.");

		this.random = Objects.requireNonNull(random, "Random cannot be null.");
	}

	public String selectExcluding(Collection<String> excludedNames) {
		Objects.requireNonNull(excludedNames, "Excluded character names cannot be null.");

		List<String> availableNames = new ArrayList<>();

		for (String candidateName : catalog.getAll()) {
			boolean excluded = excludedNames.stream()
					.anyMatch(excludedName -> CharacterNameRules.areEquivalent(candidateName, excludedName));

			if (!excluded) {
				availableNames.add(candidateName);
			}
		}

		if (availableNames.isEmpty()) {
			throw new IllegalStateException("No character name is available for random selection.");
		}

		return availableNames.get(random.nextInt(availableNames.size()));
	}
}