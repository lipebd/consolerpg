package rpg.world.scenarios;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Stores dungeon boss gates indexed by dungeon ID.
public final class DungeonBossGateCatalog {

	private final Map<String, DungeonBossGateDefinition> gatesByDungeonId;

	public DungeonBossGateCatalog() {
		this.gatesByDungeonId = new LinkedHashMap<>();
	}

	public void register(DungeonBossGateDefinition definition) {
		if (definition == null) {
			throw new IllegalArgumentException("Dungeon boss gate definition cannot be null.");
		}

		String dungeonId = definition.getDungeonId();

		if (gatesByDungeonId.containsKey(dungeonId)) {
			throw new IllegalArgumentException("Dungeon boss gate is already registered: " + dungeonId);
		}

		gatesByDungeonId.put(dungeonId, definition);
	}

	public Optional<DungeonBossGateDefinition> findByDungeonId(String dungeonId) {
		if (dungeonId == null || dungeonId.isBlank()) {
			throw new IllegalArgumentException("Dungeon ID cannot be empty.");
		}

		return Optional.ofNullable(gatesByDungeonId.get(dungeonId.trim()));
	}

	public List<DungeonBossGateDefinition> getAll() {
		return List.copyOf(new ArrayList<>(gatesByDungeonId.values()));
	}
}