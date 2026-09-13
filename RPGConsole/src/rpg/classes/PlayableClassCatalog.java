package rpg.classes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Stores and provides access to all playable-class definitions.
public final class PlayableClassCatalog {

	private final Map<String, PlayableClassDefinition> classesById;

	public PlayableClassCatalog() {
		classesById = new LinkedHashMap<>();
	}

	public void register(PlayableClassDefinition classDefinition) {
		Objects.requireNonNull(classDefinition, "Playable class definition cannot be null.");

		String classId = classDefinition.getId();

		if (classesById.containsKey(classId)) {
			throw new IllegalArgumentException("Playable class definition ID is already registered: " + classId);
		}

		classesById.put(classId, classDefinition);
	}

	public PlayableClassDefinition getById(String id) {
		PlayableClassDefinition classDefinition = classesById.get(id);

		if (classDefinition == null) {
			throw new IllegalArgumentException("Playable class not found: " + id);
		}

		return classDefinition;
	}

	public List<PlayableClassDefinition> getAll() {
		return List.copyOf(classesById.values());
	}
}