package rpg.world.travel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Stores and provides access to travel route definitions.
public final class TravelCatalog {

    private final Map<String, TravelRouteDefinition> routesById;
    private final Map<String, List<TravelRouteDefinition>> routesBySourceScenarioId;

    public TravelCatalog() {
        routesById = new LinkedHashMap<>();
        routesBySourceScenarioId = new LinkedHashMap<>();
    }

    public void register(TravelRouteDefinition routeDefinition) {
        if (routeDefinition == null) {
            throw new IllegalArgumentException("Travel route definition cannot be null.");
        }

        String routeId = routeDefinition.getId();

        if (routesById.containsKey(routeId)) {
            throw new IllegalArgumentException("Travel route ID is already registered: " + routeId);
        }

        String sourceScenarioId = routeDefinition.getSourceScenarioId();

        List<TravelRouteDefinition> sourceRoutes =
                routesBySourceScenarioId.computeIfAbsent(sourceScenarioId, id -> new ArrayList<>());

        for (TravelRouteDefinition existingRoute : sourceRoutes) {
            if (existingRoute
                    .getDestinationScenarioId()
                    .equals(routeDefinition.getDestinationScenarioId())) {
                throw new IllegalArgumentException(
                        "Travel destination is already registered from scenario: "
                                + sourceScenarioId);
            }
        }

        routesById.put(routeId, routeDefinition);
        sourceRoutes.add(routeDefinition);
    }

    public boolean containsId(String routeId) {
        return routesById.containsKey(routeId);
    }

    public TravelRouteDefinition getById(String routeId) {
        TravelRouteDefinition routeDefinition = routesById.get(routeId);

        if (routeDefinition == null) {
            throw new IllegalArgumentException("Travel route not found: " + routeId);
        }

        return routeDefinition;
    }

    public List<TravelRouteDefinition> getRoutesFrom(String sourceScenarioId) {
        List<TravelRouteDefinition> routes =
                routesBySourceScenarioId.getOrDefault(sourceScenarioId, List.of());

        return List.copyOf(routes);
    }

    public List<TravelRouteDefinition> getAll() {
        return List.copyOf(routesById.values());
    }
}
