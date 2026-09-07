package rpg.world.shops;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to shop definitions.
public final class ShopCatalog {

	private final Map<String, ShopDefinition> shopsById;
	private final Map<String, List<ShopDefinition>> shopsByScenarioId;

	public ShopCatalog() {
		shopsById = new LinkedHashMap<>();
		shopsByScenarioId = new LinkedHashMap<>();
	}

	public void register(ShopDefinition shopDefinition) {
		if (shopDefinition == null) {
			throw new IllegalArgumentException("Shop definition cannot be null.");
		}

		String shopId = shopDefinition.getId();

		if (shopsById.containsKey(shopId)) {
			throw new IllegalArgumentException(
					"Shop ID is already registered: " + shopId
			);
		}

		shopsById.put(shopId, shopDefinition);
		shopsByScenarioId
				.computeIfAbsent(shopDefinition.getScenarioId(), ignored -> new ArrayList<>())
				.add(shopDefinition);
	}

	public ShopDefinition getById(String shopId) {
		ShopDefinition shopDefinition = shopsById.get(shopId);

		if (shopDefinition == null) {
			throw new IllegalArgumentException("Shop not found: " + shopId);
		}

		return shopDefinition;
	}

	public List<ShopDefinition> getByScenarioId(String scenarioId) {
		List<ShopDefinition> shopDefinitions = shopsByScenarioId.get(scenarioId);

		if (shopDefinitions == null) {
			return List.of();
		}

		return List.copyOf(shopDefinitions);
	}

	public List<ShopDefinition> getAll() {
		return List.copyOf(shopsById.values());
	}
}