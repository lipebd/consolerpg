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
import rpg.flags.FlagCatalog;
import rpg.flags.FlagLoader;
import rpg.flags.CampaignFlags;
import rpg.quests.QuestProgress;
import rpg.quests.QuestCatalog;
import rpg.quests.QuestLoader;
import rpg.quests.effects.QuestEffectCatalog;
import rpg.quests.effects.QuestEffectLoader;
import rpg.quests.rewards.QuestRewardCatalog;
import rpg.quests.rewards.QuestRewardLoader;
import rpg.requirements.QuestAvailabilityValidator;
import rpg.requirements.RequirementCatalog;
import rpg.requirements.RequirementLoader;
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
import rpg.narrative.DialogueCatalog;
import rpg.narrative.DialogueLoader;
import rpg.narrative.NarrativeLocationCatalog;
import rpg.narrative.NarrativeLocationLoader;
import rpg.narrative.NarrativeReferenceValidator;
import rpg.narrative.NpcCatalog;
import rpg.narrative.NpcLoader;
import rpg.narrative.NpcPlacementCatalog;
import rpg.narrative.NpcPlacementLoader;
import rpg.narrative.QuestDialogueCatalog;
import rpg.narrative.QuestDialogueLoader;
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

			ConsoleInput consoleInput = new ConsoleInput();

			CharacterNameCatalog characterNameCatalog;
			PlayableClassCatalog playableClassCatalog;
			SkillCatalog skillCatalog;

			ItemCatalog itemCatalog;
			ItemDefinitionCatalog itemDefinitionCatalog;
			RecoveryItemDefinitionCatalog recoveryItemDefinitionCatalog;
			SkillScrollDefinitionCatalog skillScrollDefinitionCatalog;
			EquipmentCatalog equipmentCatalog;
			EquipmentService equipmentService;

			CrafterCatalog crafterCatalog;
			CraftingRecipeCatalog craftingRecipeCatalog;

			ShopCatalog shopCatalog;
			ShopStockCatalog shopStockCatalog;
			ShopStateCatalog shopStateCatalog;
			ShopStateGenerator shopStateGenerator;

			ScenarioCatalog scenarioCatalog;
			ScenarioBranchCatalog scenarioBranchCatalog;
			Scenario currentScenario;

			CityTransportCatalog cityTransportCatalog;
			TravelCatalog travelCatalog;
			TravelProgress travelProgress;

			InnCatalog innCatalog;
			ExplorationRewardCatalog explorationRewardCatalog;

			EnemyCatalog enemyCatalog;
			EncounterCatalog encounterCatalog;
			EnemyDropCatalog enemyDropCatalog;

			QuestCatalog questCatalog;
			QuestRewardCatalog questRewardCatalog;
			QuestEffectCatalog questEffectCatalog;
			FlagCatalog flagCatalog;
			RequirementCatalog requirementCatalog;
			NarrativeLocationCatalog narrativeLocationCatalog;
			NpcCatalog npcCatalog;
			NpcPlacementCatalog npcPlacementCatalog;
			DialogueCatalog dialogueCatalog;
			QuestDialogueCatalog questDialogueCatalog;

			WorldProgress worldProgress;
			DungeonProgress dungeonProgress;

			try {
				// Character definitions and skills
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

				// Enemy definitions
				EnemyLoader enemyLoader = new EnemyLoader();
				enemyCatalog =
						enemyLoader.load(Path.of("data", "enemies.txt"));

				// Item definitions and specialized item types
				ItemLoader itemLoader = new ItemLoader();
				itemDefinitionCatalog =
						itemLoader.load(Path.of("data", "items.txt"));

				RecoveryItemLoader recoveryItemLoader = new RecoveryItemLoader();
				recoveryItemDefinitionCatalog =
						recoveryItemLoader.load(
								Path.of("data", "recovery_items.txt"),
								itemDefinitionCatalog);

				SkillScrollLoader skillScrollLoader = new SkillScrollLoader();
				skillScrollDefinitionCatalog =
						skillScrollLoader.load(
								Path.of("data", "skill_scrolls.txt"),
								itemDefinitionCatalog,
								skillCatalog,
								playableClassCatalog);

				EquipmentLoader equipmentLoader = new EquipmentLoader();
				equipmentCatalog =
						equipmentLoader.load(
								Path.of("data", "weapons.txt"),
								Path.of("data", "armors.txt"),
								Path.of("data", "trinkets.txt"),
								itemDefinitionCatalog);

				// Runtime item catalog and equipment service
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

				equipmentService =
						new EquipmentService(equipmentCatalog, itemCatalog);

				// Enemy drops and encounters
				EnemyDropLoader enemyDropLoader = new EnemyDropLoader();
				enemyDropCatalog =
						enemyDropLoader.load(
								Path.of("data", "enemy_drops.txt"),
								enemyCatalog,
								itemCatalog);

				EncounterLoader encounterLoader = new EncounterLoader();
				encounterCatalog =
						encounterLoader.load(
								Path.of("data", "encounters.txt"),
								enemyCatalog);

				// Quests, flags, requirements, and availability validation
				QuestLoader questLoader = new QuestLoader();
				questCatalog =
						questLoader.load(
								Path.of("data", "quests.txt"),
								Path.of("data", "quest_steps.txt"),
								Path.of("data", "quest_objectives.txt"));

				QuestRewardLoader questRewardLoader =
				        new QuestRewardLoader();

				questRewardCatalog =
				        questRewardLoader.load(
				                Path.of("data", "quest_rewards.txt"),
				                questCatalog,
				                itemCatalog);

				FlagLoader flagLoader = new FlagLoader();
				flagCatalog =
						flagLoader.load(Path.of("data", "flags.txt"));

				RequirementLoader requirementLoader = new RequirementLoader();
				requirementCatalog =
						requirementLoader.load(
								Path.of("data", "requirements.txt"),
								questCatalog,
								flagCatalog,
								itemDefinitionCatalog);

				QuestAvailabilityValidator.validate(
						questCatalog,
						requirementCatalog);

				// Scenario definitions and world construction
				ScenarioLoader scenarioLoader = new ScenarioLoader();
				ScenarioDefinitionCatalog scenarioDefinitionCatalog =
						scenarioLoader.load(Path.of("data", "scenarios.txt"));

				// Narrative locations, NPCs, and dialogues
				NarrativeLocationLoader narrativeLocationLoader =
				        new NarrativeLocationLoader();
				narrativeLocationCatalog =
				        narrativeLocationLoader.load(
				                Path.of("data", "narrative_locations.txt"),
				                scenarioDefinitionCatalog,
				                requirementCatalog);

				NpcLoader npcLoader = new NpcLoader();
				npcCatalog =
				        npcLoader.load(Path.of("data", "npcs.txt"));

				NpcPlacementLoader npcPlacementLoader =
				        new NpcPlacementLoader();
				npcPlacementCatalog =
				        npcPlacementLoader.load(
				                Path.of("data", "npc_placements.txt"),
				                npcCatalog,
				                narrativeLocationCatalog,
				                requirementCatalog);

				DialogueLoader dialogueLoader = new DialogueLoader();
				dialogueCatalog =
				        dialogueLoader.load(
				                Path.of("data", "dialogues.txt"),
				                npcCatalog,
				                enemyCatalog);

				QuestDialogueLoader questDialogueLoader =
				        new QuestDialogueLoader();
				questDialogueCatalog =
				        questDialogueLoader.load(
				                Path.of("data", "quest_dialogues.txt"),
				                questCatalog,
				                dialogueCatalog);

				NarrativeReferenceValidator.validate(
				        npcCatalog,
				        dialogueCatalog,
				        questCatalog);

				InnLoader innLoader = new InnLoader();
				innCatalog =
						innLoader.load(
								Path.of("data", "inns.txt"),
								scenarioDefinitionCatalog);

				CombatScenarioLoader combatScenarioLoader = new CombatScenarioLoader();
				CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog =
						combatScenarioLoader.load(
								Path.of("data", "combat_scenarios.txt"),
								scenarioDefinitionCatalog,
								innCatalog);

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

				// World progression
				worldProgress = new WorldProgress(scenarioCatalog);

				// Travel routes and city transportation
				TravelRouteLoader travelRouteLoader = new TravelRouteLoader();
				travelCatalog =
						travelRouteLoader.load(
								Path.of("data", "travel_routes.txt"),
								scenarioDefinitionCatalog,
								combatScenarioDefinitionCatalog);

				// Travel-dependent quest effects
				QuestEffectLoader questEffectLoader =
				        new QuestEffectLoader();

				questEffectCatalog =
				        questEffectLoader.load(
				                Path.of("data", "quest_effects.txt"),
				                questCatalog,
				                flagCatalog,
				                travelCatalog);

				CityTransportLoader cityTransportLoader = new CityTransportLoader();
				cityTransportCatalog =
						cityTransportLoader.load(
								Path.of("data", "city_transport.txt"),
								scenarioDefinitionCatalog);

				// Crafting recipes and crafters
				CraftingRecipeLoader craftingRecipeLoader = new CraftingRecipeLoader();
				craftingRecipeCatalog =
						craftingRecipeLoader.load(
								Path.of("data", "crafting_recipes.txt"),
								itemDefinitionCatalog);

				CrafterLoader crafterLoader = new CrafterLoader();
				crafterCatalog =
						crafterLoader.load(
								Path.of("data", "crafters.txt"),
								scenarioDefinitionCatalog,
								craftingRecipeCatalog);

				// Shops, stock, and runtime state generation
				ShopLoader shopLoader = new ShopLoader();
				shopCatalog =
						shopLoader.load(
								Path.of("data", "shops.txt"),
								scenarioDefinitionCatalog,
								combatScenarioDefinitionCatalog);

				ShopStockLoader shopStockLoader = new ShopStockLoader();
				shopStockCatalog =
						shopStockLoader.load(
								Path.of("data", "shop_stock.txt"),
								shopCatalog,
								itemCatalog);

				shopStateGenerator =
						new ShopStateGenerator(
								shopCatalog,
								shopStockCatalog,
								new Random());

				// Exploration rewards
				ExplorationRewardLoader explorationRewardLoader = new ExplorationRewardLoader();
				explorationRewardCatalog =
						explorationRewardLoader.load(
								Path.of("data", "exploration_rewards.txt"),
								combatScenarioDefinitionCatalog,
								itemCatalog);

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

			// Temporary V6 campaign state until save integration is implemented
			QuestProgress questProgress =
			        new QuestProgress(questCatalog);

			CampaignFlags campaignFlags =
			        new CampaignFlags(flagCatalog);

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
                    travelProgress,
                    narrativeLocationCatalog,
                    npcCatalog,
                    npcPlacementCatalog,
                    dialogueCatalog,
                    requirementCatalog,
                    questProgress,
                    campaignFlags,
                    questCatalog,
                    questRewardCatalog,
                    questEffectCatalog,
                    questDialogueCatalog);

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

		PlayableClassDefinition flageloDefinition = playableClassCatalog.getById("flagelo_iniciante");

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
