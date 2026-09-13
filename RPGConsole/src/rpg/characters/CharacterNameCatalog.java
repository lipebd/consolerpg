package rpg.characters;

import java.util.ArrayList;
import java.util.List;

// Stores the validated names available for random character naming.
public final class CharacterNameCatalog {

	private final List<String> names;

	public CharacterNameCatalog() {
		names = new ArrayList<>();
	}

	public void register(String name) {
		String validName = CharacterNameRules.requireValid(name);

		boolean alreadyRegistered = names.stream()
				.anyMatch(registeredName -> CharacterNameRules.areEquivalent(registeredName, validName));

		if (alreadyRegistered) {
			throw new IllegalArgumentException("Character name is already registered: " + validName);
		}

		names.add(validName);
	}

	public List<String> getAll() {
		return List.copyOf(names);
	}

	public int size() {
		return names.size();
	}
}