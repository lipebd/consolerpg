package rpg;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;

import rpg.characters.CharacterStats;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassLoader;
import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyDropCatalog;
import rpg.enemies.EnemyDropLoader;
import rpg.enemies.EnemyLoader;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.items.ItemDefinition;
import rpg.items.ItemDefinitionCatalog;
import rpg.items.ItemFactory;
import rpg.items.ItemLoader;
import rpg.items.RecoveryItemDefinitionCatalog;
import rpg.items.RecoveryItemLoader;
import rpg.skills.SkillCatalog;
import rpg.skills.SkillLoader;
import rpg.ui.ConsoleUI;
import rpg.ui.TitleScreen;
import rpg.world.World;
import rpg.world.WorldProgress;
import rpg.world.branches.ScenarioBranchCatalog;
import rpg.world.branches.ScenarioBranchLoader;
import rpg.world.encounters.EncounterCatalog;
import rpg.world.encounters.EncounterLoader;
import rpg.world.inns.InnCatalog;
import rpg.world.inns.InnLoader;
import rpg.world.rewards.ExplorationRewardCatalog;
import rpg.world.rewards.ExplorationRewardLoader;
import rpg.world.scenarios.CombatScenarioDefinitionCatalog;
import rpg.world.scenarios.CombatScenarioLoader;
import rpg.world.scenarios.DungeonDefinitionCatalog;
import rpg.world.scenarios.DungeonLoader;
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.scenarios.ScenarioDefinition;
import rpg.world.scenarios.ScenarioDefinitionCatalog;
import rpg.world.scenarios.ScenarioFactory;
import rpg.world.scenarios.ScenarioLoader;
import rpg.world.shops.ShopCatalog;
import rpg.world.shops.ShopLoader;
import rpg.world.shops.ShopStateCatalog;
import rpg.world.shops.ShopStateGenerator;
import rpg.world.shops.ShopStockCatalog;
import rpg.world.shops.ShopStockLoader;
import rpg.world.transport.CityTransportCatalog;
import rpg.world.transport.CityTransportLoader;
import rpg.world.travel.TravelCatalog;
import rpg.world.travel.TravelProgress;
import rpg.world.travel.TravelRouteLoader;

// Starts the game and controls its initial execution.
public class Main {

	public static void main(String[] args) {

		SkillCatalog skillCatalog;
		ShopStateCatalog shopStateCatalog;
		ShopCatalog shopCatalog;
		ShopStockCatalog shopStockCatalog;
		CityTransportCatalog cityTransportCatalog;
		InnCatalog innCatalog;
		ExplorationRewardCatalog explorationRewardCatalog;
		ItemCatalog itemCatalog;
		RecoveryItemDefinitionCatalog recoveryItemDefinitionCatalog;
		ItemDefinitionCatalog itemDefinitionCatalog;
		EnemyCatalog enemyCatalog;
		EncounterCatalog encounterCatalog;
		EnemyDropCatalog enemyDropCatalog;
		ScenarioCatalog scenarioCatalog;
		ScenarioBranchCatalog scenarioBranchCatalog;
		PlayableClassCatalog playableClassCatalog;
		Scenario currentScenario;
		TravelProgress travelProgress;
		WorldProgress worldProgress;
		Scanner scanner = new Scanner(System.in);

		try {
			PlayableClassLoader playableClassLoader = new PlayableClassLoader();
			playableClassCatalog = playableClassLoader.load(Path.of("data", "playable_classes.txt"));
			SkillLoader skillLoader = new SkillLoader();
			skillCatalog = skillLoader
					.load(Path.of("data", "skills.txt"), Path.of("data", "skill_effects.txt"), playableClassCatalog);
			EnemyLoader enemyLoader = new EnemyLoader();
			enemyCatalog = enemyLoader.load(Path.of("data", "enemies.txt"));
			ItemLoader itemLoader = new ItemLoader();
			itemDefinitionCatalog = itemLoader.load(Path.of("data", "items.txt"));
			RecoveryItemLoader recoveryItemLoader = new RecoveryItemLoader();
			recoveryItemDefinitionCatalog = recoveryItemLoader
					.load(Path.of("data", "recovery_items.txt"), itemDefinitionCatalog);
			itemCatalog = new ItemCatalog();
			ItemFactory itemFactory = new ItemFactory();
			for (ItemDefinition definition : itemDefinitionCatalog.getAll()) {
				Item item = itemFactory.create(definition, recoveryItemDefinitionCatalog);
				itemCatalog.register(item);
			}
			EnemyDropLoader enemyDropLoader = new EnemyDropLoader();
			enemyDropCatalog = enemyDropLoader.load(Path.of("data", "enemy_drops.txt"), enemyCatalog, itemCatalog);
			EncounterLoader encounterLoader = new EncounterLoader();
			encounterCatalog = encounterLoader.load(Path.of("data", "encounters.txt"), enemyCatalog);
			ScenarioLoader scenarioLoader = new ScenarioLoader();
			ScenarioDefinitionCatalog scenarioDefinitionCatalog = scenarioLoader.load(Path.of("data", "scenarios.txt"));
			CityTransportLoader cityTransportLoader = new CityTransportLoader();
			cityTransportCatalog = cityTransportLoader
					.load(Path.of("data", "city_transport.txt"), scenarioDefinitionCatalog);
			InnLoader innLoader = new InnLoader();
			innCatalog = innLoader.load(Path.of("data", "inns.txt"), scenarioDefinitionCatalog);
			CombatScenarioLoader combatScenarioLoader = new CombatScenarioLoader();
			CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog = combatScenarioLoader
					.load(Path.of("data", "combat_scenarios.txt"), scenarioDefinitionCatalog);
			ShopLoader shopLoader = new ShopLoader();
			shopCatalog = shopLoader
					.load(Path.of("data", "shops.txt"), scenarioDefinitionCatalog, combatScenarioDefinitionCatalog);
			ShopStockLoader shopStockLoader = new ShopStockLoader();
			shopStockCatalog = shopStockLoader.load(Path.of("data", "shop_stock.txt"), shopCatalog, itemCatalog);
			ShopStateGenerator shopStateGenerator = new ShopStateGenerator(shopCatalog, shopStockCatalog, new Random());
			shopStateCatalog = shopStateGenerator.generate();
			ExplorationRewardLoader explorationRewardLoader = new ExplorationRewardLoader();
			explorationRewardCatalog = explorationRewardLoader
					.load(Path.of("data", "exploration_rewards.txt"), combatScenarioDefinitionCatalog, itemCatalog);
			DungeonLoader dungeonLoader = new DungeonLoader();
			DungeonDefinitionCatalog dungeonDefinitionCatalog = dungeonLoader.load(
					Path.of("data", "dungeons.txt"),
					scenarioDefinitionCatalog,
					combatScenarioDefinitionCatalog,
					enemyCatalog
			);
			ScenarioBranchLoader scenarioBranchLoader = new ScenarioBranchLoader();
			scenarioBranchCatalog = scenarioBranchLoader.load(
					Path.of("data", "scenario_branches.txt"),
					scenarioDefinitionCatalog,
					combatScenarioDefinitionCatalog
			);
			scenarioCatalog = new ScenarioCatalog();
			ScenarioFactory scenarioFactory = new ScenarioFactory();
			for (ScenarioDefinition definition : scenarioDefinitionCatalog.getAll()) {
				Scenario scenario = scenarioFactory
						.create(definition, combatScenarioDefinitionCatalog, dungeonDefinitionCatalog);
				scenarioCatalog.register(scenario);
			}

			currentScenario = scenarioCatalog.getById("bree");
			worldProgress = new WorldProgress(scenarioCatalog);
			TravelRouteLoader travelRouteLoader = new TravelRouteLoader();
			TravelCatalog travelCatalog = travelRouteLoader.load(
					Path.of("data", "travel_routes.txt"),
					scenarioDefinitionCatalog,
					combatScenarioDefinitionCatalog
			);
			travelProgress = new TravelProgress(travelCatalog);

		} catch (IOException | IllegalArgumentException exception) {
			System.out.println("Não foi possível carregar os dados do jogo.");
			System.out.println(exception.getMessage());
			return;
		}

		TitleScreen.show(scanner);
		ConsoleUI.clearScreen();

		CharacterStats flageloStats = new CharacterStats(
				120, // HP
				50, // MP
				110, // SP
				15, // ATK
				14, // DEF
				9, // MAG
				12, // RES
				12, // VEL
				10 // LCK
		);

		PlayableCharacter flagelo = new PlayableCharacter(
				"Flagelo",
				flageloStats,
				skillCatalog.getByClassId("flagelo")
		);

		Party party = new Party(flagelo);

		CharacterStats bardoStats = new CharacterStats(
				80, // HP
				80, // MP
				60, // SP
				8, // ATK
				8, // DEF
				12, // MAG
				10, // RES
				15, // VEL
				14 // LCK
		);

		PlayableCharacter bardo = new PlayableCharacter("Bardo", bardoStats, skillCatalog.getByClassId("bardo"));

		party.addMember(bardo);

		World world = new World(
				party,
				currentScenario,
				scanner,
				itemCatalog,
				shopCatalog,
				shopStateCatalog,
				enemyCatalog,
				enemyDropCatalog,
				encounterCatalog,
				explorationRewardCatalog,
				scenarioCatalog,
				worldProgress,
				cityTransportCatalog,
				innCatalog,
				scenarioBranchCatalog,
				travelProgress
		);

		world.start();

		scanner.close();

	}
}
