package rpg;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.Optional;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.characters.CharacterNameCatalog;
import rpg.characters.CharacterNameLoader;
import rpg.characters.CharacterNameSelector;
import rpg.classes.CharacterGrowthCalculator;
import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassLoader;
import rpg.classes.PlayableClassDefinition;
import rpg.development.DevelopmentPartyFactory;
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
import rpg.items.equipment.EquipmentCatalog;
import rpg.items.equipment.EquipmentLoader;
import rpg.items.equipment.EquipmentService;
import rpg.items.SkillScrollDefinitionCatalog;
import rpg.items.SkillScrollLoader;
import rpg.persistence.CampaignSaveService;
import rpg.skills.SkillCatalog;
import rpg.skills.SkillLoader;
import rpg.ui.ConsoleInput;
import rpg.ui.ConsoleUI;
import rpg.ui.TitleScreen;
import rpg.ui.CharacterCreationMenu;
import rpg.world.World;
import rpg.world.WorldProgress;
import rpg.world.branches.ScenarioBranchCatalog;
import rpg.world.branches.ScenarioBranchLoader;
import rpg.world.crafting.CraftingRecipeCatalog;
import rpg.world.crafting.CraftingRecipeLoader;
import rpg.world.crafting.CrafterCatalog;
import rpg.world.crafting.CrafterLoader;
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
import rpg.world.WorldClock;
import rpg.persistence.CampaignLoadResult;
import rpg.persistence.CampaignLoadService;
import rpg.persistence.CampaignSnapshotRestorer;
import rpg.persistence.RestoredCampaign;
import rpg.ui.CampaignSelectionMenu;
import rpg.world.scenarios.DungeonProgress;

// Starts the game and controls its initial execution.
public class Main {
    private static final String DEVELOPMENT_PARTY_ARGUMENT = "-dev-party";

	public static void main(String[] args) {
		try {
			ShopStateGenerator shopStateGenerator;
			CharacterNameCatalog characterNameCatalog;
			CrafterCatalog crafterCatalog;
			CraftingRecipeCatalog craftingRecipeCatalog;
			ItemCatalog itemCatalog;
			ItemDefinitionCatalog itemDefinitionCatalog;
			RecoveryItemDefinitionCatalog recoveryItemDefinitionCatalog;
			SkillScrollDefinitionCatalog skillScrollDefinitionCatalog;
			EquipmentCatalog equipmentCatalog;
			EquipmentService equipmentService;
			SkillCatalog skillCatalog;
			ShopStateCatalog shopStateCatalog;
			ShopCatalog shopCatalog;
			ShopStockCatalog shopStockCatalog;
			CityTransportCatalog cityTransportCatalog;
			TravelCatalog travelCatalog;
			TravelProgress travelProgress;
			InnCatalog innCatalog;
			ExplorationRewardCatalog explorationRewardCatalog;
			EnemyCatalog enemyCatalog;
			EncounterCatalog encounterCatalog;
			EnemyDropCatalog enemyDropCatalog;
			ScenarioCatalog scenarioCatalog;
			ScenarioBranchCatalog scenarioBranchCatalog;
			PlayableClassCatalog playableClassCatalog;
			Scenario currentScenario;
			WorldProgress worldProgress;
            DungeonProgress dungeonProgress;
			ConsoleInput consoleInput = new ConsoleInput();

			try {
				CharacterNameLoader characterNameLoader = new CharacterNameLoader();
                characterNameCatalog =
                        characterNameLoader.load(Path.of("data", "character_names.txt"));
				PlayableClassLoader playableClassLoader = new PlayableClassLoader();
                playableClassCatalog =
                        playableClassLoader.load(Path.of("data", "playable_classes.txt"));
				SkillLoader skillLoader = new SkillLoader();
                skillCatalog =
                        skillLoader.load(
						Path.of("data", "skills.txt"),
						Path.of("data", "skill_effects.txt"),
                                playableClassCatalog);
				EnemyLoader enemyLoader = new EnemyLoader();
				enemyCatalog = enemyLoader.load(Path.of("data", "enemies.txt"));
				ItemLoader itemLoader = new ItemLoader();
				itemDefinitionCatalog = itemLoader.load(Path.of("data", "items.txt"));
				CraftingRecipeLoader craftingRecipeLoader = new CraftingRecipeLoader();
                craftingRecipeCatalog =
                        craftingRecipeLoader.load(
                                Path.of("data", "crafting_recipes.txt"), itemDefinitionCatalog);
				EquipmentLoader equipmentLoader = new EquipmentLoader();

                equipmentCatalog =
                        equipmentLoader.load(
						Path.of("data", "weapons.txt"),
						Path.of("data", "armors.txt"),
						Path.of("data", "trinkets.txt"),
                                itemDefinitionCatalog);
				RecoveryItemLoader recoveryItemLoader = new RecoveryItemLoader();
                recoveryItemDefinitionCatalog =
                        recoveryItemLoader.load(
                                Path.of("data", "recovery_items.txt"), itemDefinitionCatalog);
				SkillScrollLoader skillScrollLoader = new SkillScrollLoader();
                skillScrollDefinitionCatalog =
                        skillScrollLoader.load(
						Path.of("data", "skill_scrolls.txt"),
						itemDefinitionCatalog,
						skillCatalog,
                                playableClassCatalog);
				itemCatalog = new ItemCatalog();
				ItemFactory itemFactory = new ItemFactory();
				for (ItemDefinition definition : itemDefinitionCatalog.getAll()) {
                    Item item =
                            itemFactory.create(
							definition,
							recoveryItemDefinitionCatalog,
							skillScrollDefinitionCatalog,
                                    skillCatalog);
					itemCatalog.register(item);
				}
				equipmentService = new EquipmentService(equipmentCatalog, itemCatalog);
				EnemyDropLoader enemyDropLoader = new EnemyDropLoader();
                enemyDropCatalog =
                        enemyDropLoader.load(
                                Path.of("data", "enemy_drops.txt"), enemyCatalog, itemCatalog);
				EncounterLoader encounterLoader = new EncounterLoader();
                encounterCatalog =
                        encounterLoader.load(Path.of("data", "encounters.txt"), enemyCatalog);
				ScenarioLoader scenarioLoader = new ScenarioLoader();
                ScenarioDefinitionCatalog scenarioDefinitionCatalog =
                        scenarioLoader.load(Path.of("data", "scenarios.txt"));
				CrafterLoader crafterLoader = new CrafterLoader();
                crafterCatalog =
                        crafterLoader.load(
                                Path.of("data", "crafters.txt"),
                                scenarioDefinitionCatalog,
                                craftingRecipeCatalog);
				CityTransportLoader cityTransportLoader = new CityTransportLoader();
                cityTransportCatalog =
                        cityTransportLoader.load(
                                Path.of("data", "city_transport.txt"), scenarioDefinitionCatalog);
				InnLoader innLoader = new InnLoader();
				innCatalog = innLoader.load(Path.of("data", "inns.txt"), scenarioDefinitionCatalog);
				CombatScenarioLoader combatScenarioLoader = new CombatScenarioLoader();
                CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog =
                        combatScenarioLoader.load(
                                Path.of("data", "combat_scenarios.txt"),
                                scenarioDefinitionCatalog,
                                innCatalog);
				ShopLoader shopLoader = new ShopLoader();
                shopCatalog =
                        shopLoader.load(
                                Path.of("data", "shops.txt"),
                                scenarioDefinitionCatalog,
                                combatScenarioDefinitionCatalog);
				ShopStockLoader shopStockLoader = new ShopStockLoader();
                shopStockCatalog =
                        shopStockLoader.load(
                                Path.of("data", "shop_stock.txt"), shopCatalog, itemCatalog);
                shopStateGenerator =
                        new ShopStateGenerator(shopCatalog, shopStockCatalog, new Random());
				ExplorationRewardLoader explorationRewardLoader = new ExplorationRewardLoader();
                explorationRewardCatalog =
                        explorationRewardLoader.load(
                                Path.of("data", "exploration_rewards.txt"),
                                combatScenarioDefinitionCatalog,
                                itemCatalog);
				DungeonLoader dungeonLoader = new DungeonLoader();
                DungeonDefinitionCatalog dungeonDefinitionCatalog =
                        dungeonLoader.load(
						Path.of("data", "dungeons.txt"),
						scenarioDefinitionCatalog,
						combatScenarioDefinitionCatalog,
                                enemyCatalog);
				ScenarioBranchLoader scenarioBranchLoader = new ScenarioBranchLoader();
                scenarioBranchCatalog =
                        scenarioBranchLoader.load(
						Path.of("data", "scenario_branches.txt"),
						scenarioDefinitionCatalog,
                                combatScenarioDefinitionCatalog);
				scenarioCatalog = new ScenarioCatalog();
				ScenarioFactory scenarioFactory = new ScenarioFactory();
				for (ScenarioDefinition definition : scenarioDefinitionCatalog.getAll()) {
                    Scenario scenario =
                            scenarioFactory.create(
                                    definition,
                                    combatScenarioDefinitionCatalog,
                                    dungeonDefinitionCatalog);
					scenarioCatalog.register(scenario);
				}

				worldProgress = new WorldProgress(scenarioCatalog);
				TravelRouteLoader travelRouteLoader = new TravelRouteLoader();
                travelCatalog =
                        travelRouteLoader.load(
						Path.of("data", "travel_routes.txt"),
						scenarioDefinitionCatalog,
                                combatScenarioDefinitionCatalog);

			} catch (IOException | IllegalArgumentException exception) {
				System.out.println("Não foi possível carregar os dados do jogo.");
				System.out.println(exception.getMessage());
				return;
			}

			CharacterGrowthCalculator characterGrowthCalculator = new CharacterGrowthCalculator();

			Party party;
			WorldClock worldClock;

			if (usesDevelopmentParty(args)) {
                DevelopmentPartyFactory developmentPartyFactory =
                        new DevelopmentPartyFactory(
                                playableClassCatalog, skillCatalog, characterGrowthCalculator);

				party = developmentPartyFactory.create();
				currentScenario = scenarioCatalog.getById("bree");
				worldProgress = new WorldProgress(scenarioCatalog);
                dungeonProgress = new DungeonProgress(scenarioCatalog);
				travelProgress = new TravelProgress(travelCatalog);
				worldClock = new WorldClock();
				shopStateCatalog = shopStateGenerator.generate();
			} else {
				TitleScreen.show(consoleInput);
				ConsoleUI.clearScreen();

                CampaignSnapshotRestorer snapshotRestorer =
                        new CampaignSnapshotRestorer(
						playableClassCatalog,
						skillCatalog,
						equipmentCatalog,
						itemCatalog,
						scenarioCatalog,
						innCatalog,
						travelCatalog,
						shopCatalog,
						shopStockCatalog,
                                characterGrowthCalculator);

				CampaignLoadService loadService = new CampaignLoadService(snapshotRestorer);
				CampaignLoadResult loadResult = loadService.load();

                CampaignSelectionMenu campaignSelectionMenu =
                        new CampaignSelectionMenu(consoleInput);

				while (true) {
                    CampaignSelectionMenu.Selection selection =
                            campaignSelectionMenu.select(loadResult);

					ConsoleUI.clearScreen();

					if (selection == CampaignSelectionMenu.Selection.CONTINUE) {
                        RestoredCampaign restoredCampaign =
                                loadResult.getRestoredCampaign().orElseThrow();

						party = restoredCampaign.getParty();
						currentScenario = restoredCampaign.getCurrentScenario();
						worldProgress = restoredCampaign.getWorldProgress();
                        dungeonProgress = restoredCampaign.getDungeonProgress();
						travelProgress = restoredCampaign.getTravelProgress();
						worldClock = restoredCampaign.getWorldClock();
						shopStateCatalog = restoredCampaign.getShopStateCatalog();

						break;
					}

                    Optional<Party> createdParty =
                            createNewGameParty(
							consoleInput,
							characterNameCatalog,
							playableClassCatalog,
							skillCatalog,
                                    characterGrowthCalculator);

					if (createdParty.isEmpty()) {
						ConsoleUI.clearScreen();
						continue;
					}

					party = createdParty.get();
					currentScenario = scenarioCatalog.getById("bree");
					worldProgress = new WorldProgress(scenarioCatalog);
                    dungeonProgress = new DungeonProgress(scenarioCatalog);
					travelProgress = new TravelProgress(travelCatalog);
					worldClock = new WorldClock();
					shopStateCatalog = shopStateGenerator.generate();

					ConsoleUI.clearScreen();
					break;
				}
			}

			CampaignSaveService campaignSaveService = new CampaignSaveService();

            World world =
                    new World(
					party,
					currentScenario,
					consoleInput,
					itemCatalog,
					craftingRecipeCatalog,
					crafterCatalog,
					equipmentCatalog,
					equipmentService,
					shopCatalog,
					shopStateCatalog,
					shopStateGenerator,
					campaignSaveService,
					enemyCatalog,
					enemyDropCatalog,
					encounterCatalog,
					explorationRewardCatalog,
					scenarioCatalog,
					worldProgress,
                            dungeonProgress,
					worldClock,
					cityTransportCatalog,
					innCatalog,
					scenarioBranchCatalog,
                            travelProgress);
			world.start();
		} catch (ConsoleInput.EndOfInputException exception) {

			System.out.println();
			System.out.println("A entrada do console foi encerrada.");
			System.out.println("Até a próxima!");
		} catch (UncheckedIOException exception) {
			System.out.println();
			System.out.println("Não foi possível ler a entrada do console.");
		}
	}

	private static Optional<Party> createNewGameParty(
			ConsoleInput consoleInput,
			CharacterNameCatalog characterNameCatalog,
			PlayableClassCatalog playableClassCatalog,
			SkillCatalog skillCatalog,
            CharacterGrowthCalculator characterGrowthCalculator) {
        CharacterNameSelector characterNameSelector =
                new CharacterNameSelector(characterNameCatalog, new Random());

        CharacterCreationMenu characterCreationMenu =
                new CharacterCreationMenu(consoleInput, characterNameSelector);

		PlayableClassDefinition flageloDefinition = playableClassCatalog.getById("flagelo");

		Optional<String> protagonistName = characterCreationMenu.open(flageloDefinition);

		if (protagonistName.isEmpty()) {
			return Optional.empty();
		}

        PlayableCharacter protagonist =
                new PlayableCharacter(
				protagonistName.get(),
				flageloDefinition,
				skillCatalog.getByClassId(flageloDefinition.getId()),
                        characterGrowthCalculator);

		return Optional.of(new Party(protagonist));
	}

	private static boolean usesDevelopmentParty(String[] arguments) {
		for (String argument : arguments) {
			if (DEVELOPMENT_PARTY_ARGUMENT.equals(argument)) {
				return true;
			}
		}

		return false;
	}
}
