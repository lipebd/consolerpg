package rpg.world.shops;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores the runtime state generated for every shop.
public final class ShopStateCatalog {

	private final Map<String, ShopState> statesByShopId;

	public ShopStateCatalog() {
		statesByShopId = new LinkedHashMap<>();
	}

	public void register(ShopState shopState) {
		if (shopState == null) {
			throw new IllegalArgumentException("Shop state cannot be null.");
		}

		String shopId = shopState.getDefinition().getId();

		if (statesByShopId.containsKey(shopId)) {
			throw new IllegalArgumentException("Shop state is already registered: " + shopId);
		}

		statesByShopId.put(shopId, shopState);
	}

	public ShopState getByShopId(String shopId) {
		ShopState shopState = statesByShopId.get(shopId);

		if (shopState == null) {
			throw new IllegalArgumentException("Shop state not found: " + shopId);
		}

		return shopState;
	}

	public List<ShopState> getAll() {
		return List.copyOf(statesByShopId.values());
	}
}