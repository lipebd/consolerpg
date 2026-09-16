package rpg.world;

import java.util.LinkedHashSet;
import java.util.Set;

import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;

// Tracks scenarios discovered during the current game.
public final class WorldProgress {

        private final ScenarioCatalog scenarioCatalog;
        private final Set<String> discoveredScenarioIds;

        public WorldProgress(ScenarioCatalog scenarioCatalog) {
                if (scenarioCatalog == null) {
                        throw new IllegalArgumentException("Scenario catalog cannot be null.");
                }

                this.scenarioCatalog = scenarioCatalog;
                this.discoveredScenarioIds = new LinkedHashSet<>();
        }

        public void discover(String scenarioId) {
                scenarioCatalog.getById(scenarioId);
                discoveredScenarioIds.add(scenarioId);
        }
        
        public void discoverAll() {
            for (Scenario scenario : scenarioCatalog.getAll()) {
                discover(scenario.getId());
            }
        }

        public boolean isDiscovered(String scenarioId) {
                return discoveredScenarioIds.contains(scenarioId);
        }

        public Set<String> getDiscoveredScenarioIds() {
                return Set.copyOf(discoveredScenarioIds);
        }
}