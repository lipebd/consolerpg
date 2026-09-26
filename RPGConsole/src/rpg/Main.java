package rpg;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import rpg.characters.CharacterNameCatalog;
import rpg.characters.CharacterNameLoader;
import rpg.characters.CharacterNameSelector;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.classes.CharacterGrowthCalculator;
import rpg.classes.PlayableClassCatalog;
import rpg.classes.PlayableClassDefinition;
import rpg.classes.PlayableClassLoader;
import rpg.classes.paths.ClassPathCatalog;
import rpg.classes.paths.ClassPathLoader;
import rpg.classes.paths.ClassPathQuestionnaireCatalog;
import rpg.classes.paths.ClassPathQuestionnaireLoader;
import rpg.classes.paths.ClassPathRecommendationService;
import rpg.classes.paths.ClassPathRequiredClassValidator;
import rpg.classes.paths.ClassPathTransitionService;
import rpg.development.DevelopmentPartyFactory;
import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyDropCatalog;
import rpg.enemies.EnemyDropLoader;
import rpg.enemies.EnemyLoader;
import rpg.flags.CampaignFlags;
import rpg.flags.FlagCatalog;
import rpg.flags.FlagLoader;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.items.ItemDefinition;
import rpg.items.ItemDefinitionCatalog;
import rpg.items.ItemFactory;
import rpg.items.ItemLoader;
import rpg.items.RecoveryItemDefinitionCatalog;
import rpg.items.RecoveryItemLoader;
import rpg.items.RewardModifierItemDefinition;
import rpg.items.RewardModifierItemLoader;
import rpg.items.SkillScrollDefinitionCatalog;
import rpg.items.SkillScrollLoader;
import rpg.items.equipment.EquipmentCatalog;
import rpg.items.equipment.EquipmentLoader;
import rpg.items.equipment.EquipmentService;
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
import rpg.persistence.CampaignLoadResult;
import rpg.persistence.CampaignLoadService;
import rpg.persistence.CampaignSaveService;
import rpg.persistence.CampaignSnapshotRestorer;
import rpg.persistence.RestoredCampaign;
import rpg.quests.effects.QuestTransportState;
import rpg.quests.QuestCatalog;
import rpg.quests.QuestLoader;
import rpg.quests.QuestProgress;
import rpg.quests.QuestProgressService;
import rpg.quests.effects.QuestEffectCatalog;
import rpg.quests.effects.QuestEffectLoader;
import rpg.quests.rewards.QuestRewardCatalog;
import rpg.quests.rewards.QuestRewardLoader;
import rpg.recruitment.AllyClassSelector;
import rpg.recruitment.AllyRecruitmentService;
import rpg.recruitment.ClassPathRecruitmentService;
import rpg.recruitment.QuestRecruitmentCatalog;
import rpg.recruitment.QuestRecruitmentLoader;
import rpg.recruitment.RecruitmentAllyFactory;
import rpg.recruitment.RecruitmentCandidateIntroductionService;
import rpg.recruitment.RecruitmentCandidateStateCatalog;
import rpg.requirements.QuestAvailabilityValidator;
import rpg.requirements.RequirementCatalog;
import rpg.requirements.RequirementEvaluator;
import rpg.requirements.RequirementLoader;
import rpg.skills.SkillCatalog;
import rpg.skills.SkillLoader;
import rpg.ui.AllyRecruitmentMenu;
import rpg.ui.CampaignSelectionMenu;
import rpg.ui.CharacterCreationMenu;
import rpg.ui.ClassPathChoiceCoordinator;
import rpg.ui.ClassPathConfirmationMenu;
import rpg.ui.ClassPathQuestionnaireMenu;
import rpg.ui.ClassPathRecommendationMenu;
import rpg.ui.ClassPathSelectionMenu;
import rpg.ui.ClassPathTieMenu;
import rpg.ui.ClassPathTransitionFailurePresenter;
import rpg.ui.ConsoleInput;
import rpg.ui.ConsoleUI;
import rpg.ui.TitleScreen;
import rpg.world.World;
import rpg.world.WorldClock;
import rpg.world.WorldProgress;
import rpg.world.branches.ScenarioBranchCatalog;
import rpg.world.branches.ScenarioBranchLoader;
import rpg.world.crafting.CrafterCatalog;
import rpg.world.crafting.CrafterLoader;
import rpg.world.crafting.CraftingRecipeCatalog;
import rpg.world.crafting.CraftingRecipeLoader;
import rpg.world.encounters.EncounterCatalog;
import rpg.world.encounters.EncounterLoader;
import rpg.world.inns.InnCatalog;
import rpg.world.inns.InnLoader;
import rpg.world.rewards.ExplorationRewardCatalog;
import rpg.world.rewards.ExplorationRewardLoader;
import rpg.world.scenarios.CombatScenarioDefinitionCatalog;
import rpg.world.scenarios.CombatScenarioLoader;
import rpg.world.scenarios.DungeonBossAccessResolver;
import rpg.world.scenarios.DungeonBossGateCatalog;
import rpg.world.scenarios.DungeonBossGateLoader;
import rpg.world.scenarios.CombatScenarioDialogueCatalog;
import rpg.world.scenarios.CombatScenarioDialogueLoader;
import rpg.world.scenarios.DungeonDefinitionCatalog;
import rpg.world.scenarios.DungeonLoader;
import rpg.world.scenarios.DungeonProgress;
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
    private static final String DEVELOPMENT_PARTY_ARGUMENT = "--dev-party";

	public static void main(String[] args) {
		try {

			ConsoleInput consoleInput = new ConsoleInput();

			CharacterNameCatalog characterNameCatalog;
			PlayableClassCatalog playableClassCatalog;
			ClassPathCatalog classPathCatalog;
			SkillCatalog skillCatalog;
			ClassPathQuestionnaireCatalog classPathQuestionnaireCatalog;

			ItemCatalog itemCatalog;
			ItemDefinitionCatalog itemDefinitionCatalog;
			RecoveryItemDefinitionCatalog recoveryItemDefinitionCatalog;
			SkillScrollDefinitionCatalog skillScrollDefinitionCatalog;
			EquipmentCatalog equipmentCatalog;
			EquipmentService equipmentService;
			Map<String, RewardModifierItemDefinition> rewardModifierDefinitions;

			CrafterCatalog crafterCatalog;
			CraftingRecipeCatalog craftingRecipeCatalog;

			ShopCatalog shopCatalog;
			ShopStockCatalog shopStockCatalog;
			ShopStateCatalog shopStateCatalog;
			ShopStateGenerator shopStateGenerator;

			ScenarioCatalog scenarioCatalog;
			ScenarioBranchCatalog scenarioBranchCatalog;
			Scenario currentScenario;
			DungeonBossGateCatalog dungeonBossGateCatalog;

			CityTransportCatalog cityTransportCatalog;
			TravelCatalog travelCatalog;
			TravelProgress travelProgress;

			InnCatalog innCatalog;
			ExplorationRewardCatalog explorationRewardCatalog;

			EnemyCatalog enemyCatalog;
			EncounterCatalog encounterCatalog;
			EnemyDropCatalog enemyDropCatalog;

			QuestCatalog questCatalog;
			QuestRecruitmentCatalog questRecruitmentCatalog;
			QuestRewardCatalog questRewardCatalog;
			QuestEffectCatalog questEffectCatalog;
			FlagCatalog flagCatalog;
			RequirementCatalog requirementCatalog;
			NarrativeLocationCatalog narrativeLocationCatalog;
			NpcCatalog npcCatalog;
			NpcPlacementCatalog npcPlacementCatalog;
			DialogueCatalog dialogueCatalog;
			QuestDialogueCatalog questDialogueCatalog;
			QuestProgress questProgress;
			CampaignFlags campaignFlags;
			RecruitmentCandidateStateCatalog candidateStateCatalog;
			CombatScenarioDialogueCatalog combatScenarioDialogueCatalog;
			WorldProgress worldProgress;
			DungeonProgress dungeonProgress;

			try {
				// Character definitions and skills
				CharacterNameLoader characterNameLoader = new CharacterNameLoader();
				characterNameCatalog =
						characterNameLoader.load(Path.of("data", "character_names.txt"));

				PlayableClassLoader playableClassLoader = new PlayableClassLoader();

				playableClassCatalog =
				        playableClassLoader.load(
				                Path.of("data", "playable_classes.txt"));

				ClassPathRequiredClassValidator.validate(playableClassCatalog);

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

				RewardModifierItemLoader rewardModifierItemLoader =
				        new RewardModifierItemLoader();

				rewardModifierDefinitions = rewardModifierItemLoader.load(
				        Path.of("data", "reward_modifier_items.txt"),
				        itemDefinitionCatalog
				);

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

				ClassPathLoader classPathLoader = new ClassPathLoader();
                classPathCatalog =
                        classPathLoader.load(
                                Path.of("data", "class_paths.txt"),
                                playableClassCatalog,
                                equipmentCatalog);

                ClassPathQuestionnaireLoader classPathQuestionnaireLoader =
                        new ClassPathQuestionnaireLoader();

                classPathQuestionnaireCatalog =
                        classPathQuestionnaireLoader.load(
                                Path.of("data", "class_path_questions.txt"),
                                Path.of("data", "class_path_answers.txt"),
                                classPathCatalog);

				// Runtime item catalog and equipment service
				itemCatalog = new ItemCatalog();
				ItemFactory itemFactory = new ItemFactory();
				for (ItemDefinition definition : itemDefinitionCatalog.getAll()) {
					Item item =
							itemFactory.create(
									definition,
									recoveryItemDefinitionCatalog,
									rewardModifierDefinitions,
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

				QuestRecruitmentLoader questRecruitmentLoader =
						new QuestRecruitmentLoader();

				questRecruitmentCatalog =
						questRecruitmentLoader.load(
								Path.of("data", "quest_recruitments.txt"),
								questCatalog);

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
								enemyCatalog,
								questRecruitmentCatalog);

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

				DungeonBossGateLoader dungeonBossGateLoader =
				        new DungeonBossGateLoader();

				dungeonBossGateCatalog =
				        dungeonBossGateLoader.load(
				                Path.of("data", "dungeon_boss_gates.txt"),
				                scenarioDefinitionCatalog,
				                dungeonDefinitionCatalog,
				                requirementCatalog,
				                dialogueCatalog);
				CombatScenarioDialogueLoader combatScenarioDialogueLoader =
				        new CombatScenarioDialogueLoader();

				combatScenarioDialogueCatalog =
				        combatScenarioDialogueLoader.load(
				                Path.of("data", "combat_scenario_dialogues.txt"),
				                dungeonDefinitionCatalog,
				                questCatalog,
				                dialogueCatalog);

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
				                itemCatalog,
				                travelCatalog,
				                scenarioBranchCatalog,
				                scenarioDefinitionCatalog);

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
				                playableClassCatalog,
				                skillCatalog,
				                itemCatalog,
				                characterGrowthCalculator
				        );

				party = developmentPartyFactory.create();
				currentScenario = scenarioCatalog.getById("bree");
				worldProgress = new WorldProgress(scenarioCatalog);
                dungeonProgress = new DungeonProgress(scenarioCatalog);
                travelProgress = new TravelProgress(travelCatalog, scenarioBranchCatalog);
                worldClock = new WorldClock();
				shopStateCatalog = shopStateGenerator.generate();
				questProgress = new QuestProgress(questCatalog);
				campaignFlags = new CampaignFlags(flagCatalog);
				candidateStateCatalog = new RecruitmentCandidateStateCatalog(questRecruitmentCatalog);
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
						scenarioBranchCatalog,
						shopCatalog,
						shopStockCatalog,
						shopStateGenerator,
                        characterGrowthCalculator,
                        questCatalog,
                        flagCatalog,
                        questRecruitmentCatalog);

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
						questProgress = restoredCampaign.getQuestProgress();
						campaignFlags = restoredCampaign.getCampaignFlags();
						candidateStateCatalog = restoredCampaign.getCandidateStateCatalog();

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
                    travelProgress = new TravelProgress(travelCatalog, scenarioBranchCatalog);
                    worldClock = new WorldClock();
					shopStateCatalog = shopStateGenerator.generate();
					questProgress = new QuestProgress(questCatalog);
					campaignFlags = new CampaignFlags(flagCatalog);
					candidateStateCatalog =
					        new RecruitmentCandidateStateCatalog(questRecruitmentCatalog);

					ConsoleUI.clearScreen();
					break;
				}
			}

			CharacterNameSelector recruitmentNameSelector =
					new CharacterNameSelector(characterNameCatalog, new Random());

			RecruitmentCandidateIntroductionService candidateIntroductionService =
					new RecruitmentCandidateIntroductionService(
							recruitmentNameSelector, candidateStateCatalog, party);

			QuestTransportState questTransportState = new QuestTransportState();

			QuestProgressService questProgressService =
			        new QuestProgressService(
			                questProgress,
			                questRewardCatalog,
			                questEffectCatalog,
			                party,
			                itemCatalog,
			                campaignFlags,
			                travelProgress,
			                questTransportState);

			ClassPathTransitionService classPathTransitionService =
					new ClassPathTransitionService(
							classPathCatalog,
							playableClassCatalog,
							skillCatalog,
							itemCatalog,
							equipmentService,
							campaignFlags);

			AllyClassSelector allyClassSelector =
					new AllyClassSelector(
							classPathCatalog,
							playableClassCatalog,
							new Random());

			RecruitmentAllyFactory recruitmentAllyFactory =
					new RecruitmentAllyFactory(
							classPathCatalog,
							playableClassCatalog,
							skillCatalog,
							equipmentCatalog,
							characterGrowthCalculator);

			AllyRecruitmentService allyRecruitmentService =
					new AllyRecruitmentService(
							party,
							questRecruitmentCatalog,
							candidateStateCatalog,
							allyClassSelector,
							recruitmentAllyFactory);

			ClassPathRecruitmentService classPathRecruitmentService =
					new ClassPathRecruitmentService(
							party,
							questProgress,
							questRecruitmentCatalog,
							classPathTransitionService,
							allyRecruitmentService,
							questProgressService);

			ClassPathRecommendationService classPathRecommendationService =
					new ClassPathRecommendationService(
							classPathQuestionnaireCatalog,
							classPathCatalog);

			ClassPathChoiceCoordinator classPathChoiceCoordinator =
					new ClassPathChoiceCoordinator(
							party,
							playableClassCatalog,
							classPathQuestionnaireCatalog,
							classPathRecommendationService,
							new ClassPathQuestionnaireMenu(consoleInput),
							new ClassPathTieMenu(consoleInput),
							new ClassPathRecommendationMenu(consoleInput),
							new ClassPathSelectionMenu(
									consoleInput,
									classPathCatalog,
									playableClassCatalog),
							new ClassPathConfirmationMenu(consoleInput));

			AllyRecruitmentMenu allyRecruitmentMenu =
					new AllyRecruitmentMenu(consoleInput);

			ClassPathTransitionFailurePresenter classPathTransitionFailurePresenter =
					new ClassPathTransitionFailurePresenter(
							consoleInput,
							itemCatalog);

			CampaignSaveService campaignSaveService = new CampaignSaveService();

			DungeonBossAccessResolver dungeonBossAccessResolver =
			        new DungeonBossAccessResolver(
			                dungeonBossGateCatalog,
			                combatScenarioDialogueCatalog,
			                dungeonProgress,
			                questProgress,
			                requirementCatalog,
			                new RequirementEvaluator(itemCatalog),
			                party,
			                campaignFlags);

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
					dungeonBossAccessResolver,
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
                    questTransportState,
                    campaignFlags,
					candidateStateCatalog,
					candidateIntroductionService,
					questProgressService,
					classPathChoiceCoordinator,
					classPathRecruitmentService,
					allyRecruitmentMenu,
					classPathTransitionFailurePresenter,
                    questCatalog,
                    questRewardCatalog,
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
