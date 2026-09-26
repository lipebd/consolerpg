package rpg.world;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;

import rpg.GameCurrency;
import rpg.battle.BattleCoordinator;
import rpg.battle.BattleType;
import rpg.battle.BattleResult;
import rpg.battle.rewards.BattleRewardService;
import rpg.characters.Party;
import rpg.classes.PlayableClassDefinition;
import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyDefinition;
import rpg.enemies.EnemyDropCatalog;
import rpg.flags.CampaignFlags;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.items.equipment.EquipmentCatalog;
import rpg.items.equipment.EquipmentService;
import rpg.narrative.DialogueCatalog;
import rpg.narrative.DialogueDefinition;
import rpg.narrative.DialogueLineDefinition;
import rpg.narrative.DialogueSpeakerType;
import rpg.narrative.NarrativeLocationCatalog;
import rpg.narrative.NarrativeLocationDefinition;
import rpg.narrative.NarrativeTextProcessor;
import rpg.narrative.NpcCatalog;
import rpg.narrative.NpcDefinition;
import rpg.narrative.NpcPlacementCatalog;
import rpg.narrative.NpcPlacementDefinition;
import rpg.narrative.QuestDialogueCatalog;
import rpg.narrative.QuestDialogueContext;
import rpg.narrative.QuestDialogueDefinition;
import rpg.persistence.CampaignSaveResult;
import rpg.persistence.CampaignSaveService;
import rpg.persistence.CampaignSnapshot;
import rpg.persistence.CampaignSnapshotFactory;
import rpg.quests.QuestCatalog;
import rpg.quests.QuestCompletionFailure;
import rpg.quests.QuestCompletionFailureType;
import rpg.quests.QuestDefinition;
import rpg.quests.QuestNpcInteractionResolver;
import rpg.quests.QuestObjectiveDefinition;
import rpg.quests.QuestObjectiveType;
import rpg.quests.QuestProgress;
import rpg.quests.QuestProgressEvent;
import rpg.quests.QuestProgressEventType;
import rpg.quests.QuestProgressResult;
import rpg.quests.QuestProgressService;
import rpg.quests.QuestStatus;
import rpg.quests.QuestStepDefinition;
import rpg.quests.effects.QuestTransportState;
import rpg.quests.rewards.QuestRewardCatalog;
import rpg.recruitment.ClassPathRecruitmentPlan;
import rpg.recruitment.ClassPathRecruitmentPreparation;
import rpg.recruitment.ClassPathRecruitmentService;
import rpg.recruitment.RecruitmentCandidateIntroductionService;
import rpg.recruitment.RecruitmentCandidateStateCatalog;
import rpg.requirements.RequirementCatalog;
import rpg.requirements.RequirementEvaluator;
import rpg.requirements.RequirementSetDefinition;
import rpg.ui.AllyRecruitmentMenu;
import rpg.ui.BattleRewardScreen;
import rpg.ui.CityLocationMenu;
import rpg.ui.CityMenu;
import rpg.ui.CityTransportMenu;
import rpg.ui.ClassPathChoiceCoordinator;
import rpg.ui.ClassPathTransitionFailurePresenter;
import rpg.ui.CombatScenarioMenu;
import rpg.ui.ConsoleInput;
import rpg.ui.ConsoleUI;
import rpg.ui.CraftingMenu;
import rpg.ui.DialoguePresenter;
import rpg.ui.EpicEncounterMenu;
import rpg.ui.EquipmentMenu;
import rpg.ui.ExitConfirmationMenu;
import rpg.ui.InnMenu;
import rpg.ui.InventoryMenu;
import rpg.ui.NarrativeLocationMenu;
import rpg.ui.NpcInteractionMenu;
import rpg.ui.PartyRescueScreen;
import rpg.ui.PartyStatusMenu;
import rpg.ui.QuestMenu;
import rpg.ui.QuestOfferMenu;
import rpg.ui.QuestProgressCoordinator;
import rpg.ui.QuestProgressPresenter;
import rpg.ui.RouteMenu;
import rpg.ui.ScenarioBranchMenu;
import rpg.ui.ShopMenu;
import rpg.world.branches.ScenarioBranchCatalog;
import rpg.world.branches.ScenarioBranchDefinition;
import rpg.world.crafting.CrafterCatalog;
import rpg.world.crafting.CrafterDefinition;
import rpg.world.crafting.CraftingRecipeCatalog;
import rpg.world.encounters.EncounterCatalog;
import rpg.world.encounters.EncounterGenerator;
import rpg.world.exploration.ExplorationEventGenerator;
import rpg.world.exploration.ExplorationEventType;
import rpg.world.exploration.ExplorationSession;
import rpg.world.exploration.ExplorationSettings;
import rpg.world.inns.InnCatalog;
import rpg.world.inns.InnDefinition;
import rpg.world.rewards.ExplorationReward;
import rpg.world.rewards.ExplorationRewardCatalog;
import rpg.world.rewards.ExplorationRewardGenerator;
import rpg.world.scenarios.City;
import rpg.world.scenarios.CityAction;
import rpg.world.scenarios.CombatScenario;
import rpg.world.scenarios.CombatScenarioAction;
import rpg.world.scenarios.Dungeon;
import rpg.world.scenarios.DungeonBossAccess;
import rpg.world.scenarios.DungeonBossAccessResolver;
import rpg.world.scenarios.DungeonBossGateDefinition;
import rpg.world.scenarios.DungeonProgress;
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.shops.ShopAccessType;
import rpg.world.shops.ShopCatalog;
import rpg.world.shops.ShopDefinition;
import rpg.world.shops.ShopState;
import rpg.world.shops.ShopStateCatalog;
import rpg.world.shops.ShopStateGenerator;
import rpg.world.transport.CityTransportCatalog;
import rpg.world.transport.CityTransportDefinition;
import rpg.world.travel.TravelProgress;
import rpg.world.travel.TravelRouteDefinition;

// Controls navigation and interaction with the game world.
public class World {

	private static final int HOURS_PER_FINISHED_BATTLE = 1;

	private final CampaignSaveService campaignSaveService;
	private final QuestProgressService questProgressService;
	private final QuestNpcInteractionResolver questNpcInteractionResolver;
	private final RecruitmentCandidateStateCatalog candidateStateCatalog;
	private final RecruitmentCandidateIntroductionService candidateIntroductionService;

	private final CityLocationMenu cityLocationMenu;
	private final CityMenu cityMenu;
	private final CityTransportMenu cityTransportMenu;
	private final InnCatalog innCatalog;
	private final InnMenu innMenu;
	private final PartyStatusMenu partyStatusMenu;
	private final InventoryMenu inventoryMenu;
	private final RouteMenu routeMenu;
	private final ScenarioBranchMenu scenarioBranchMenu;
	private final CombatScenarioMenu combatScenarioMenu;
	private final CraftingMenu craftingMenu;
	private final EpicEncounterMenu epicEncounterMenu;
	private final ShopMenu shopMenu;
	private final ExitConfirmationMenu exitConfirmationMenu;
	private final PartyRescueScreen partyRescueScreen;
	private final NarrativeLocationMenu narrativeLocationMenu;
	private final NpcInteractionMenu npcInteractionMenu;
	private final DialoguePresenter dialoguePresenter;
	private final DialogueCatalog dialogueCatalog;
	private final QuestOfferMenu questOfferMenu;
	private final QuestProgressCoordinator questProgressCoordinator;
	private final QuestProgressPresenter questProgressPresenter;
	private final QuestMenu questMenu;
	private final ClassPathChoiceCoordinator classPathChoiceCoordinator;
	private final ClassPathRecruitmentService classPathRecruitmentService;
	private final AllyRecruitmentMenu allyRecruitmentMenu;
	private final ClassPathTransitionFailurePresenter classPathTransitionFailurePresenter;
	private int firstAnimatedExplorationMessageIndex = -1;
	private int narratedExplorationPosition = -1;

	private final ItemCatalog itemCatalog;
	private final CrafterCatalog crafterCatalog;
	private final CityTransportCatalog cityTransportCatalog;
	private final ScenarioCatalog scenarioCatalog;
	private final EnemyCatalog enemyCatalog;
	private final ShopCatalog shopCatalog;
	private final ShopStateGenerator shopStateGenerator;
	private ShopStateCatalog shopStateCatalog;
	private final NarrativeLocationCatalog narrativeLocationCatalog;
	private final NpcCatalog npcCatalog;
	private final NpcPlacementCatalog npcPlacementCatalog;
	private final RequirementCatalog requirementCatalog;
	private final QuestCatalog questCatalog;
	private final QuestDialogueCatalog questDialogueCatalog;
	private final RequirementEvaluator requirementEvaluator;

	private final ExplorationRewardGenerator explorationRewardGenerator;
	private final ExplorationEventGenerator explorationEventGenerator;
	private final EncounterGenerator encounterGenerator;

	private final WorldProgress worldProgress;
    private final DungeonProgress dungeonProgress;
    private final DungeonBossAccessResolver dungeonBossAccessResolver;
	private final WorldClock worldClock;
	private final TravelProgress travelProgress;
	private final QuestProgress questProgress;
	private final CampaignFlags campaignFlags;
	private final BattleCoordinator battleCoordinator;

	private Party party;
	private ExplorationSession explorationSession;
	private final QuestTransportState questTransportState;
	private Random random;
	private Scenario currentScenario;
	private final List<String> pendingExplorationMessages =
			new ArrayList<>();

	private boolean running;
	private boolean shouldDisplayScenarioDescription;

	public World(
			Party party,
			Scenario initialScenario,
			ConsoleInput consoleInput,
			ItemCatalog itemCatalog,
			CraftingRecipeCatalog craftingRecipeCatalog,
			CrafterCatalog crafterCatalog,
			EquipmentCatalog equipmentCatalog,
			EquipmentService equipmentService,
			ShopCatalog shopCatalog,
			ShopStateCatalog shopStateCatalog,
			ShopStateGenerator shopStateGenerator,
			CampaignSaveService campaignSaveService,
			EnemyCatalog enemyCatalog,
			EnemyDropCatalog enemyDropCatalog,
			EncounterCatalog encounterCatalog,
			ExplorationRewardCatalog explorationRewardCatalog,
			ScenarioCatalog scenarioCatalog,
			WorldProgress worldProgress,
			DungeonProgress dungeonProgress,
			DungeonBossAccessResolver dungeonBossAccessResolver,
			WorldClock worldClock,
			CityTransportCatalog cityTransportCatalog,
			InnCatalog innCatalog,
			ScenarioBranchCatalog scenarioBranchCatalog,
			TravelProgress travelProgress,
			NarrativeLocationCatalog narrativeLocationCatalog,
			NpcCatalog npcCatalog,
			NpcPlacementCatalog npcPlacementCatalog,
			DialogueCatalog dialogueCatalog,
			RequirementCatalog requirementCatalog,
			QuestProgress questProgress,
			QuestTransportState questTransportState,
			CampaignFlags campaignFlags,
			RecruitmentCandidateStateCatalog candidateStateCatalog,
			RecruitmentCandidateIntroductionService candidateIntroductionService,
			QuestProgressService questProgressService,
			ClassPathChoiceCoordinator classPathChoiceCoordinator,
			ClassPathRecruitmentService classPathRecruitmentService,
			AllyRecruitmentMenu allyRecruitmentMenu,
			ClassPathTransitionFailurePresenter classPathTransitionFailurePresenter,
			QuestCatalog questCatalog,
			QuestRewardCatalog questRewardCatalog,
			QuestDialogueCatalog questDialogueCatalog) {

		Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		Objects.requireNonNull(enemyDropCatalog, "Enemy drop catalog cannot be null.");
		Objects.requireNonNull(encounterCatalog, "Encounter catalog cannot be null.");

        // Core world state and shared data
        this.party = Objects.requireNonNull(party, "Party cannot be null.");
        this.currentScenario =
                Objects.requireNonNull(initialScenario, "Initial scenario cannot be null.");
        this.enemyCatalog = Objects.requireNonNull(enemyCatalog, "Enemy catalog cannot be null.");
        this.scenarioCatalog =
                Objects.requireNonNull(scenarioCatalog, "Scenario catalog cannot be null.");
        this.worldProgress =
                Objects.requireNonNull(worldProgress, "World progress cannot be null.");
        this.dungeonProgress =
				Objects.requireNonNull(dungeonProgress, "Dungeon progress cannot be null.");
		this.dungeonBossAccessResolver = Objects
				.requireNonNull(dungeonBossAccessResolver, "Dungeon boss access resolver cannot be null.");
        this.travelProgress =
                Objects.requireNonNull(travelProgress, "Travel progress cannot be null.");
        this.crafterCatalog =
                Objects.requireNonNull(crafterCatalog, "Crafter catalog cannot be null.");
        this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");
        this.narrativeLocationCatalog =
                Objects.requireNonNull(
                        narrativeLocationCatalog, "Narrative location catalog cannot be null.");
        this.npcCatalog = Objects.requireNonNull(npcCatalog, "NPC catalog cannot be null.");
        this.npcPlacementCatalog =
                Objects.requireNonNull(
                        npcPlacementCatalog, "NPC placement catalog cannot be null.");
        this.requirementCatalog =
                Objects.requireNonNull(requirementCatalog, "Requirement catalog cannot be null.");
        this.questProgress =
                Objects.requireNonNull(questProgress, "Quest progress cannot be null.");
        this.campaignFlags =
                Objects.requireNonNull(campaignFlags, "Campaign flags cannot be null.");
		this.candidateStateCatalog =
				Objects.requireNonNull(
						candidateStateCatalog,
						"Recruitment candidate state catalog cannot be null.");
		this.candidateIntroductionService =
				Objects.requireNonNull(
						candidateIntroductionService,
						"Recruitment candidate introduction service cannot be null.");
        this.questCatalog = Objects.requireNonNull(questCatalog, "Quest catalog cannot be null.");
        this.questDialogueCatalog =
                Objects.requireNonNull(
                        questDialogueCatalog, "Quest dialogue catalog cannot be null.");
        this.requirementEvaluator = new RequirementEvaluator(this.itemCatalog);
        this.questProgressService =
				Objects.requireNonNull(
						questProgressService,
						"Quest progress service cannot be null.");
        this.questNpcInteractionResolver =
                new QuestNpcInteractionResolver(
                        this.questCatalog,
                        this.questProgress,
                        this.requirementCatalog,
                        this.requirementEvaluator,
                        this.party,
                        this.campaignFlags);
        this.shopCatalog = Objects.requireNonNull(shopCatalog, "Shop catalog cannot be null.");
        this.shopStateGenerator =
                Objects.requireNonNull(shopStateGenerator, "Shop state generator cannot be null.");
        this.shopStateCatalog =
                Objects.requireNonNull(shopStateCatalog, "Shop state catalog cannot be null.");
        this.innCatalog = Objects.requireNonNull(innCatalog, "Inn catalog cannot be null.");
        this.cityTransportCatalog =
                Objects.requireNonNull(
                        cityTransportCatalog, "City transport catalog cannot be null.");
        this.worldProgress.discover(this.currentScenario.getId());
        this.worldClock = Objects.requireNonNull(worldClock, "World clock cannot be null.");
		this.dialogueCatalog =
				Objects.requireNonNull(dialogueCatalog, "Dialogue catalog cannot be null.");

        // Shared random generator and gameplay services
        this.random = new Random();
        BattleRewardService rewardService = new BattleRewardService(random);
        this.encounterGenerator =
                new EncounterGenerator(this.enemyCatalog, encounterCatalog, random);
        this.explorationRewardGenerator =
                new ExplorationRewardGenerator(explorationRewardCatalog, random);
        this.explorationEventGenerator = new ExplorationEventGenerator(random);
        this.campaignSaveService =
        Objects.requireNonNull(campaignSaveService, "Campaign save service cannot be null.");
        Objects.requireNonNull(questRewardCatalog, "Quest reward catalog cannot be null.");
		this.classPathChoiceCoordinator =
				Objects.requireNonNull(
						classPathChoiceCoordinator,
						"Class path choice coordinator cannot be null.");
		this.classPathRecruitmentService =
				Objects.requireNonNull(
						classPathRecruitmentService,
						"Class path recruitment service cannot be null.");
		this.classPathTransitionFailurePresenter =
				Objects.requireNonNull(
						classPathTransitionFailurePresenter,
						"Class path transition failure presenter cannot be null.");
		this.questTransportState = Objects.requireNonNull(questTransportState, "Quest transport state cannot be null.");

        // User interface
        this.epicEncounterMenu = new EpicEncounterMenu(consoleInput);
        this.combatScenarioMenu = new CombatScenarioMenu(consoleInput, this.worldClock);
		this.routeMenu = new RouteMenu(consoleInput, this.travelProgress, this.scenarioCatalog);
        this.scenarioBranchMenu = new ScenarioBranchMenu(consoleInput, this.scenarioCatalog);
        EquipmentMenu equipmentMenu =
                new EquipmentMenu(
                        this.party,
                        consoleInput,
                        Objects.requireNonNull(
                                equipmentCatalog, "Equipment catalog cannot be null."),
                        this.itemCatalog,
                        Objects.requireNonNull(
                                equipmentService, "Equipment service cannot be null."));
        this.inventoryMenu = new InventoryMenu(this.party, consoleInput, equipmentMenu);
        this.partyStatusMenu = new PartyStatusMenu(this.party, consoleInput, equipmentMenu);
        this.cityMenu = new CityMenu(consoleInput, this.worldClock);
        this.cityTransportMenu =
                new CityTransportMenu(this.party, consoleInput, this.scenarioCatalog);
        this.cityLocationMenu = new CityLocationMenu(consoleInput);
        this.narrativeLocationMenu = new NarrativeLocationMenu(consoleInput);
        this.npcInteractionMenu = new NpcInteractionMenu(consoleInput);
        this.questOfferMenu = new QuestOfferMenu(consoleInput);
        this.questProgressPresenter =
                new QuestProgressPresenter(
                        consoleInput, this.questCatalog, questRewardCatalog, this.itemCatalog);
        this.questProgressCoordinator =
                new QuestProgressCoordinator(
                        this.questProgressService, this.questProgressPresenter);
        BattleRewardScreen rewardScreen = new BattleRewardScreen(consoleInput);
        this.battleCoordinator =
                new BattleCoordinator(
                        this.party,
                        consoleInput,
                        enemyDropCatalog,
                        rewardService,
                        rewardScreen,
                        this.questProgressCoordinator,
                        random);
        this.questMenu =
                new QuestMenu(
                        consoleInput,
                        this.party,
                        this.questProgress,
                        questRewardCatalog,
                        this.itemCatalog,
                        this.questProgressCoordinator);
        this.dialoguePresenter =
                new DialoguePresenter(
                        consoleInput,
						this.dialogueCatalog,
                        this.npcCatalog,
						this.enemyCatalog,
						this.candidateStateCatalog);
        this.craftingMenu =
                new CraftingMenu(
                        this.party,
                        consoleInput,
                        this.itemCatalog,
                        Objects.requireNonNull(
                                craftingRecipeCatalog, "Crafting recipe catalog cannot be null."),
                        this.questProgressCoordinator);
        this.innMenu = new InnMenu(this.party, consoleInput);
        this.shopMenu =
                new ShopMenu(
                        this.party, consoleInput, this.worldClock, this.questProgressCoordinator);
        this.partyRescueScreen = new PartyRescueScreen(consoleInput);
        this.exitConfirmationMenu = new ExitConfirmationMenu(consoleInput);
		this.allyRecruitmentMenu =
				Objects.requireNonNull(
						allyRecruitmentMenu,
						"Ally recruitment menu cannot be null.");

        // Initial exploration state
        if (this.currentScenario instanceof CombatScenario combatScenario) {
            this.explorationSession =
                    new ExplorationSession(combatScenario.getExplorationSettings());
        } else {
            this.explorationSession = null;
        }

        // Runtime flags
        this.running = true;
        this.shouldDisplayScenarioDescription = true;
    }

	public void start() {
		while (running) {
			executeCurrentScenarioMenu();
			executePendingQuestTransport();
		}
	}

	private void executePendingQuestTransport() {
	    Optional<String> pendingDestination =
	            questTransportState.consume();

	    if (pendingDestination.isEmpty()) {
	        return;
	    }

	    Scenario destination =
	            scenarioCatalog.getById(
	                    pendingDestination.orElseThrow());

	    changeScenario(destination);
	}

	private void executeCurrentScenarioMenu() {
		if (currentScenario instanceof City city) {
			executeCityMenu(city);
			return;
		}

		if (currentScenario instanceof CombatScenario) {
			executeCombatScenarioMenu();
			return;
		}

        throw new IllegalStateException(
                "Unsupported scenario type: " + currentScenario.getClass().getName());
	}

	private List<CityTransportDefinition> getAvailableCityTransportDestinations() {
		List<CityTransportDefinition> availableDestinations = new ArrayList<>();

		for (CityTransportDefinition definition : cityTransportCatalog.getAll()) {
			String destinationScenarioId = definition.getDestinationScenarioId();

			if (!worldProgress.isDiscovered(destinationScenarioId)) {
				continue;
			}

			if (destinationScenarioId.equals(currentScenario.getId())) {
				continue;
			}

			availableDestinations.add(definition);
		}

		return List.copyOf(availableDestinations);
	}

	private int getCityTransportDestinationSlotCount() {
		int destinationSlotCount = 0;

		for (Scenario scenario : scenarioCatalog.getAll()) {
			if (!(scenario instanceof City)) {
				continue;
			}

			if (scenario.getId().equals(currentScenario.getId())) {
				continue;
			}

			destinationSlotCount++;
		}

		return destinationSlotCount;
	}

	private void executeCityMenu(City city) {
		List<CityAction> availableActions = getAvailableCityActions(city);

		Optional<CityAction> selectedAction =
				cityMenu.selectAction(
						city.getName(),
						city.getDescription(),
						availableActions,
						getMarkedCityActions(city, availableActions)
				);
		if (selectedAction.isEmpty()) {
			requestExitWithoutSaving();
			return;
		}

		executeCityAction(city, selectedAction.get());
	}

    private List<CityAction> getAvailableCityActions(City city) {
        List<CityAction> availableActions = new ArrayList<>();

        boolean hasInn = innCatalog.findByScenarioId(city.getId()).isPresent();

        boolean hasCityShop = !getCityShops(city).isEmpty();

        boolean hasCrafter = !getCityCrafters(city).isEmpty();

        boolean hasNarrativeLocation = !getVisibleNarrativeLocations(city.getId()).isEmpty();

        if (hasInn || hasCityShop || hasCrafter || hasNarrativeLocation) {
            availableActions.add(CityAction.VISIT_LOCATION);
        }

        if (travelProgress.hasUnlockedRoutesFrom(city.getId())) {
            availableActions.add(CityAction.LEAVE_CITY);
        }

        if (!getAvailableCityTransportDestinations().isEmpty()) {
            availableActions.add(CityAction.CITY_TRANSPORT);
        }

        availableActions.add(CityAction.INVENTORY);
        availableActions.add(CityAction.QUESTS);
        availableActions.add(CityAction.VIEW_STATUS);

        return List.copyOf(availableActions);
    }

    private void executeCityAction(City city, CityAction action) {
        switch (action) {
            case VISIT_LOCATION -> executeCityLocationMenu(city);

            case LEAVE_CITY -> executeRouteMenu();

            case INVENTORY -> inventoryMenu.open();

            case QUESTS -> questMenu.open();

            case VIEW_STATUS -> partyStatusMenu.open();

            case CITY_TRANSPORT -> executeCityTransportMenu();
        }
    }

    private void executeCityLocationMenu(City city) {
        Optional<InnDefinition> innDefinition = innCatalog.findByScenarioId(city.getId());

        List<ShopDefinition> cityShops = getCityShops(city);

        List<CrafterDefinition> cityCrafters = getCityCrafters(city);

        List<NarrativeLocationDefinition> narrativeLocations =
                getVisibleNarrativeLocations(city.getId());

        List<String> locationNames = new ArrayList<>();

        innDefinition.ifPresent(definition -> locationNames.add(definition.getName()));

        for (ShopDefinition shopDefinition : cityShops) {
            locationNames.add(shopDefinition.getName());
        }

        for (CrafterDefinition crafterDefinition : cityCrafters) {
            locationNames.add(crafterDefinition.getName());
        }

        for (NarrativeLocationDefinition locationDefinition
                : narrativeLocations) {
            String locationLabel =
                    locationDefinition.getName();

            if (hasQuestInteractionInLocation(
                    locationDefinition)) {
                locationLabel = "\u001B[33m[!]\u001B[0m " + locationLabel;
            }

            locationNames.add(locationLabel);
        }

		int serviceCount = (innDefinition.isPresent() ? 1 : 0) + cityShops.size() + cityCrafters.size();

		OptionalInt selectedLocation = cityLocationMenu.selectLocation(locationNames, serviceCount, city.getName());

		if (selectedLocation.isEmpty()) {
			return;
		}

		int selectedIndex = selectedLocation.getAsInt();

		if (innDefinition.isPresent()) {
			if (selectedIndex == 0) {
				executeInnMenu(city, innDefinition.get());
				return;
			}

			selectedIndex--;
		}

		if (selectedIndex < cityShops.size()) {
			ShopDefinition selectedShop = cityShops.get(selectedIndex);

			ShopState selectedShopState = shopStateCatalog.getByShopId(selectedShop.getId());

			shopMenu.open(city.getName(), selectedShopState);
			return;
		}

		selectedIndex -= cityShops.size();

		if (selectedIndex < cityCrafters.size()) {
			CrafterDefinition selectedCrafter = cityCrafters.get(selectedIndex);

			craftingMenu.open(city.getName(), selectedCrafter);
			return;
        }

        selectedIndex -= cityCrafters.size();

        NarrativeLocationDefinition selectedNarrativeLocation =
                narrativeLocations.get(selectedIndex);

        executeNarrativeLocationMenu(city, selectedNarrativeLocation);
    }

    private void executeNarrativeLocationMenu(
            City city,
            NarrativeLocationDefinition locationDefinition
    ) {
        while (true) {
            List<NpcDefinition> visibleNpcs =
                    getVisibleNpcs(locationDefinition.getId());

            List<String> npcLabels =
                    visibleNpcs.stream()
                            .map(
                                    npcDefinition -> {
                                        String npcLabel =
                                                npcDefinition.getName();

                                        if (!questNpcInteractionResolver
                                                .getAvailableQuests(
                                                        npcDefinition.getId())
                                                .isEmpty()) {
                                            return "\u001B[33m[!]\u001B[0m " + npcLabel;
                                        }

                                        return npcLabel;
                                    })
                            .toList();

            OptionalInt selectedNpc =
                    narrativeLocationMenu.selectNpc(
                            city.getName(),
                            locationDefinition,
                            npcLabels);

            if (selectedNpc.isEmpty()) {
                return;
            }

            NpcDefinition npcDefinition =
                    visibleNpcs.get(selectedNpc.getAsInt());

            executeNpcInteractionMenu(
                    city,
                    locationDefinition,
                    npcDefinition);
        }
    }

    private void executeNpcInteractionMenu(
            City city,
            NarrativeLocationDefinition locationDefinition,
            NpcDefinition npcDefinition) {
        while (true) {
            List<QuestDefinition> availableQuests =
                    questNpcInteractionResolver.getAvailableQuests(npcDefinition.getId());

            List<String> questTitles =
                    availableQuests.stream().map(QuestDefinition::getTitle).toList();

            NpcInteractionMenu.Selection selection =
                    npcInteractionMenu.selectAction(
                            city.getName(), locationDefinition, npcDefinition, questTitles);

            switch (selection.action()) {
                case TALK ->
					displayDialogue(npcDefinition.getDefaultDialogueId(), city.getName(), locationDefinition.getName());

				case QUEST -> {
					QuestDefinition selectedQuest = availableQuests.get(selection.questIndex());

					executeQuestNpcInteraction(city, locationDefinition, npcDefinition, selectedQuest);
				}

				case BACK -> {
					return;
				}
				}
			}
		}

    private List<NarrativeLocationDefinition>
    getVisibleNarrativeLocations(String cityId) {
        List<NarrativeLocationDefinition> visibleLocations =
                new ArrayList<>();

        for (NarrativeLocationDefinition locationDefinition
                : narrativeLocationCatalog.getByCityId(cityId)) {
            if (isRequirementSatisfied(
                    locationDefinition.getRequirementSetId())) {
                visibleLocations.add(locationDefinition);
            }
        }

        return List.copyOf(visibleLocations);
    }

    private List<NpcDefinition> getVisibleNpcs(
            String narrativeLocationId
            ) {
        List<NpcDefinition> visibleNpcs =
                new ArrayList<>();

        for (NpcPlacementDefinition placementDefinition
                : npcPlacementCatalog.getByLocationId(
                        narrativeLocationId)) {
            if (!isRequirementSatisfied(
                    placementDefinition.getRequirementSetId())) {
                continue;
            }

            visibleNpcs.add(
                    npcCatalog.getById(
                            placementDefinition.getNpcId()));
        }

        return List.copyOf(visibleNpcs);
    }

    private boolean isRequirementSatisfied(
            Optional<String> requirementSetId
            ) {
        if (requirementSetId.isEmpty()) {
            return true;
        }

        RequirementSetDefinition requirementSet =
                requirementCatalog.getById(
                        requirementSetId.orElseThrow());

        return requirementEvaluator.isSatisfied(
                requirementSet,
                party,
                questProgress,
                campaignFlags);
    }

	private void executeInnMenu(City city, InnDefinition innDefinition) {
		while (running) {
			InnMenu.Selection selection = innMenu.open(city.getName(), innDefinition);

			if (selection == InnMenu.Selection.BACK) {
				return;
			}

			innMenu.displaySaving(city.getName(), innDefinition);

			CampaignSaveResult saveResult = saveCampaign(innDefinition);

			innMenu.displaySaveResult(city.getName(), innDefinition, saveResult);

			if (saveResult.isSuccess() && selection == InnMenu.Selection.SAVE_AND_EXIT) {
				terminateGame();
				return;
			}
		}
	}

	private CampaignSaveResult saveCampaign(InnDefinition innDefinition) {
		if (!currentScenario.getId().equals(innDefinition.getScenarioId())) {
            throw new IllegalStateException(
                    "The active INN does not belong to the current scenario.");
		}

		CampaignSnapshot snapshot;

		try {
            snapshot =
                    CampaignSnapshotFactory.create(
                            party,
                            currentScenario,
                            worldProgress,
                            travelProgress,
                            dungeonProgress,
                            worldClock,
                            shopStateCatalog,
							questProgress,
							campaignFlags,
							candidateStateCatalog);
		} catch (IllegalArgumentException | IllegalStateException | ArithmeticException exception) {
			return CampaignSaveResult.ENCODING_FAILED;
		}

		return campaignSaveService.save(snapshot);
	}

	private void displaySaveFailure(CampaignSaveResult saveResult) {
		System.out.println();

		switch (saveResult) {
            case ENCODING_FAILED ->
                    System.out.println(
                            "Não foi possível preparar os dados da campanha para salvar.");

            case FILE_WRITE_FAILED ->
                    System.out.println("Não foi possível gravar a campanha salva.");

            case SUCCESS ->
                    throw new IllegalArgumentException(
                            "A successful save cannot be displayed as a failure.");
		}
	}

	private void executeCombatScenarioMenu() {
		if (!(currentScenario
				instanceof CombatScenario combatScenario)) {
			throw new IllegalStateException(
					"Current scenario is not a combat scenario."
			);
		}

		if (explorationSession == null) {
			throw new IllegalStateException(
					"Combat scenario requires an exploration session."
			);
		}

		List<CombatScenarioAction> availableActions =
				getAvailableCombatScenarioActions();

		boolean dungeonBossObstacleVisible =
				combatScenario instanceof Dungeon dungeon
						&& !explorationSession.isComplete()
						&& explorationSession.isAtFinalPosition()
						&& isDungeonBossAvailable(dungeon);

		List<String> explorationMessages =
				getExplorationMessages();

		int firstAnimatedMessageIndex = firstAnimatedExplorationMessageIndex;
		int narratedPosition =
		        firstAnimatedMessageIndex >= 0 ? narratedExplorationPosition : -1;

		firstAnimatedExplorationMessageIndex = -1;
		narratedExplorationPosition = -1;

		Optional<CombatScenarioAction> selectedAction = combatScenarioMenu.selectAction(
				combatScenario.getName(),
				combatScenario.getDescription(),
				explorationSession,
				shouldDisplayScenarioDescription,
				dungeonBossObstacleVisible,
				party.size(),
				explorationMessages,
				availableActions,
				getMarkedCombatScenarioActions(availableActions),
				firstAnimatedMessageIndex,
				narratedPosition
		);

		if (selectedAction.isEmpty()) {
			requestExitWithoutSaving();
			return;
		}

		executeCombatScenarioAction(selectedAction.get());
	}

    private void executeQuestNpcInteraction(
            City city,
            NarrativeLocationDefinition locationDefinition,
            NpcDefinition npcDefinition,
            QuestDefinition questDefinition) {
        requireAvailableQuestInteraction(npcDefinition.getId(), questDefinition.getId());

        QuestStatus status = questProgress.getStatus(questDefinition.getId());

        switch (status) {
            case NOT_STARTED ->
                    executeQuestOffer(city, locationDefinition, npcDefinition, questDefinition);

            case ACTIVE ->
                    executeActiveQuestInteraction(
                            city, locationDefinition, npcDefinition, questDefinition);

            case COMPLETED ->
                    throw new IllegalStateException(
                            "Completed quest cannot provide an NPC interaction: "
                                    + questDefinition.getId());
        }
    }

	private void executeQuestOffer(
			City city,
			NarrativeLocationDefinition locationDefinition,
			NpcDefinition npcDefinition,
			QuestDefinition questDefinition
	) {

		displayRequiredQuestDialogue(
				questDefinition.getId(),
				0,
				QuestDialogueContext.OFFER,
				city.getName(),
				locationDefinition.getName()
		);

		QuestOfferMenu.Selection selection = questOfferMenu
				.select(city.getName(), locationDefinition.getName(), questDefinition);

		if (selection == QuestOfferMenu.Selection.DECLINE) {
			displayOptionalQuestDialogue(
					questDefinition.getId(),
					0,
					QuestDialogueContext.DECLINE,
					city.getName(),
					locationDefinition.getName()
			);

			return;
		}

		displayOptionalQuestDialogue(
				questDefinition.getId(),
				0,
				QuestDialogueContext.ACCEPT,
				city.getName(),
				locationDefinition.getName()
		);

        requireAvailableQuestInteraction(npcDefinition.getId(), questDefinition.getId());

        QuestProgressResult result = questProgressService.startQuest(questDefinition.getId());

        questProgressPresenter.displayQuestStarted(questDefinition, result);
    }

    private void executeActiveQuestInteraction(
            City city,
            NarrativeLocationDefinition locationDefinition,
            NpcDefinition npcDefinition,
            QuestDefinition questDefinition) {
		if (classPathRecruitmentService.hasRecruitmentForCurrentStep(
				questDefinition.getId())) {
			executeClassPathRecruitmentInteraction(
					city,
					locationDefinition,
					npcDefinition,
					questDefinition);

			return;
		}

		executeStandardQuestInteraction(
				city,
				locationDefinition,
				npcDefinition,
				questDefinition);
	}

	private void executeStandardQuestInteraction(
			City city,
			NarrativeLocationDefinition locationDefinition,
			NpcDefinition npcDefinition,
			QuestDefinition questDefinition) {
        int currentStep =
                questProgress.getCurrentStep(questDefinition.getId()).orElseThrow().getStep();

        List<QuestCompletionFailure> failures =
                questProgressService.validateNpcInteractionCompletion(
                        questDefinition.getId(), npcDefinition.getId());

		if (!failures.isEmpty()) {
			if (containsFailureType(failures, QuestCompletionFailureType.MISSING_DELIVERY_ITEM)) {
				displayOptionalQuestDialogue(
						questDefinition.getId(),
						currentStep,
						QuestDialogueContext.PENDING,
						city.getName(),
						locationDefinition.getName()
				);
			}

			questProgressPresenter.displayCompletionFailures(failures);

			return;
		}

		displayRequiredQuestDialogue(
				questDefinition.getId(),
				currentStep,
				QuestDialogueContext.COMPLETE,
				city.getName(),
				locationDefinition.getName()
		);

        failures =
                questProgressService.validateNpcInteractionCompletion(
                        questDefinition.getId(), npcDefinition.getId());

        if (!failures.isEmpty()) {
            questProgressPresenter.displayCompletionFailures(failures);

            return;
        }

        QuestProgressResult result =
                questProgressService.completeNpcInteraction(
                        questDefinition.getId(), npcDefinition.getId());

        questProgressPresenter.display(result);
    }

	private void executeClassPathRecruitmentInteraction(
			City city,
			NarrativeLocationDefinition locationDefinition,
			NpcDefinition npcDefinition,
			QuestDefinition questDefinition) {
		int currentStep =
				questProgress
				.getCurrentStep(questDefinition.getId())
				.orElseThrow()
				.getStep();

		QuestDialogueDefinition preCompletionDialogue =
		        getRequiredQuestDialogue(
		                questDefinition.getId(),
		                currentStep,
		                QuestDialogueContext.PRE_COMPLETE);

		QuestDialogueDefinition completionDialogue =
		        getRequiredQuestDialogue(
		                questDefinition.getId(),
		                currentStep,
		                QuestDialogueContext.COMPLETE);

		displayDialogue(
		        preCompletionDialogue.getDialogueId(),
		        city.getName(),
		        locationDefinition.getName());

		PlayableClassDefinition selectedClass =
		        classPathChoiceCoordinator.chooseClassPath();

		ClassPathRecruitmentPreparation preparation =
				classPathRecruitmentService.prepare(
						questDefinition.getId(),
						npcDefinition.getId(),
						selectedClass.getId());

		if (!preparation.isReady()) {
			displayClassPathRecruitmentFailures(preparation);
			return;
		}

		ClassPathRecruitmentPlan plan = preparation.getPlan();

		displayDialogue(
				completionDialogue.getDialogueId(),
				city.getName(),
				locationDefinition.getName());

		allyRecruitmentMenu.confirm(
				plan.getAllyRecruitmentPlan().getAlly());

		QuestProgressResult result =
				classPathRecruitmentService.apply(plan);

		questProgressPresenter.display(result);
	}

	private void displayClassPathRecruitmentFailures(
			ClassPathRecruitmentPreparation preparation) {
		if (!preparation.getClassPathFailures().isEmpty()) {
			classPathTransitionFailurePresenter.display(
					preparation.getClassPathFailures());

			return;
		}

		questProgressPresenter.displayCompletionFailures(
				preparation.getQuestCompletionFailures());
	}

	private void displayRequiredQuestDialogue(
			String questId,
			int step,
			QuestDialogueContext context,
			String cityName,
			String locationName
	) {
		QuestDialogueDefinition definition = getRequiredQuestDialogue(questId, step, context);

		displayDialogue(definition.getDialogueId(), cityName, locationName);
	}

	private QuestDialogueDefinition getRequiredQuestDialogue(
			String questId,
			int step,
			QuestDialogueContext context) {
		return questDialogueCatalog
                        .find(questId, step, context)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Required quest dialogue is missing: "
                                                        + questId
                                                        + ", step "
                                                        + step
                                                        + ", "
                                                        + context));
    }

	private void displayOptionalQuestDialogue(
			String questId,
			int step,
			QuestDialogueContext context,
			String cityName,
			String locationName
	) {
		questDialogueCatalog.find(questId, step, context)
				.ifPresent(definition -> displayDialogue(definition.getDialogueId(), cityName, locationName));
	}

    private boolean hasQuestInteractionInCity(City city) {
        for (NarrativeLocationDefinition locationDefinition :
                getVisibleNarrativeLocations(city.getId())) {
            if (hasQuestInteractionInLocation(locationDefinition)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasQuestInteractionInLocation(NarrativeLocationDefinition locationDefinition) {
        for (NpcDefinition npcDefinition : getVisibleNpcs(locationDefinition.getId())) {
            if (!questNpcInteractionResolver.getAvailableQuests(npcDefinition.getId()).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void requireAvailableQuestInteraction(String npcId, String questId) {
        for (QuestDefinition availableQuest :
                questNpcInteractionResolver.getAvailableQuests(npcId)) {
            if (availableQuest.getId().equals(questId)) {
                return;
            }
        }

        throw new IllegalStateException(
                "Quest interaction is no longer available: " + questId + " through NPC " + npcId);
    }

    private static boolean containsFailureType(
            List<QuestCompletionFailure> failures, QuestCompletionFailureType failureType) {
        for (QuestCompletionFailure failure : failures) {
            if (failure.getFailureType() == failureType) {
                return true;
            }
        }

        return false;
    }

	private void displayDialogue(String dialogueId, String heading) {
		introduceDialogueCandidates(dialogueId);

		dialoguePresenter.display(dialogueId, party, heading);
	}

	private void displayDialogue(String dialogueId, String context, String qualifier) {
		introduceDialogueCandidates(dialogueId);

		dialoguePresenter.display(dialogueId, party, context, qualifier);
	}

	private void introduceDialogueCandidates(String dialogueId) {
		DialogueDefinition dialogueDefinition =
				dialogueCatalog.getById(dialogueId);

		Set<String> candidateIds = new LinkedHashSet<>();

		for (DialogueLineDefinition lineDefinition
				: dialogueDefinition.getLines()) {
			if (lineDefinition.getSpeakerType()
					== DialogueSpeakerType.RECRUIT) {
				candidateIds.add(
						lineDefinition.getSpeakerId().orElseThrow());
			}
			candidateIds.addAll(
			        NarrativeTextProcessor.getRecruitmentCandidateIds(
			                lineDefinition.getText()));
		}

		for (String candidateId : candidateIds) {
			candidateIntroductionService.introduce(candidateId);
		}
	}

	private List<CombatScenarioAction> getAvailableCombatScenarioActions() {
		List<CombatScenarioAction> availableActions = new ArrayList<>();

        boolean combatScenarioInProgress =
                currentScenario instanceof CombatScenario
				&& (explorationSession == null || !explorationSession.isComplete());

		if (combatScenarioInProgress) {
            if (currentScenario instanceof Dungeon dungeon
                    && explorationSession != null
					&& explorationSession.isAtFinalPosition()) {
                if (isDungeonBossAvailable(dungeon)) {
				availableActions.add(CombatScenarioAction.FACE_BOSS);
                } else {
                    availableActions.add(CombatScenarioAction.EXPLORE);
                }

			} else {
				availableActions.add(CombatScenarioAction.EXPLORE);

                if (hasAvailableScenarioBranch()) {
                    availableActions.add(CombatScenarioAction.FOLLOW_BRANCH);
                }
            }

        } else if (travelProgress.hasUnlockedRoutesFrom(currentScenario.getId())) {
            availableActions.add(CombatScenarioAction.FOLLOW_ROUTE);
        }

        if (!getAvailableCityTransportDestinations().isEmpty()) {
            availableActions.add(CombatScenarioAction.CITY_TRANSPORT);
        }
        availableActions.add(CombatScenarioAction.INVENTORY);
        availableActions.add(CombatScenarioAction.QUESTS);
        availableActions.add(CombatScenarioAction.VIEW_STATUS);

        return List.copyOf(availableActions);
    }

    private boolean hasAvailableScenarioBranch() {
        if (explorationSession == null || explorationSession.isComplete()) {
            return false;
        }

        return !travelProgress
                .getUnlockedBranchesAt(
                        currentScenario.getId(),
                        explorationSession.getCurrentPosition())
                .isEmpty();
    }

    private void executeCombatScenarioAction(CombatScenarioAction action) {
        switch (action) {
            case EXPLORE -> explore();

            case FACE_BOSS -> faceBoss();

            case INVENTORY -> inventoryMenu.open();

            case QUESTS -> questMenu.open();

            case VIEW_STATUS -> partyStatusMenu.open();

            case FOLLOW_ROUTE -> executeRouteMenu();

            case FOLLOW_BRANCH -> executeScenarioBranchMenu();

            case CITY_TRANSPORT -> executeCityTransportMenu();
        }
    }

	private void executeScenarioBranchMenu() {
		if (!hasAvailableScenarioBranch()) {
			System.out.println("\nNão há outro caminho disponível nesta posição.");

			return;
		}

		List<ScenarioBranchDefinition> availableBranches =
		        travelProgress.getUnlockedBranchesAt(
		                currentScenario.getId(),
		                explorationSession.getCurrentPosition());

		Optional<ScenarioBranchDefinition> selectedBranch =
				scenarioBranchMenu.open(
						currentScenario.getName(),
						availableBranches,
						getMarkedDestinationScenarioIds()
				);

		if (selectedBranch.isEmpty()) {
			return;
		}

        Scenario destination =
                scenarioCatalog.getById(selectedBranch.get().getDestinationScenarioId());

		changeScenario(destination);
	}

	private void faceBoss() {
        if (!(currentScenario instanceof Dungeon dungeon)
                || explorationSession == null
                || !explorationSession.isAtFinalPosition()
                || explorationSession.isComplete()) {
			throw new IllegalStateException("There is no dungeon boss available here.");
		}

        if (!isDungeonBossAvailable(dungeon)) {
            throw new IllegalStateException("The dungeon boss is currently unavailable.");
        }

		executeBossEncounter(dungeon);
	}

	private void explore() {
		if (!(currentScenario instanceof CombatScenario combatScenario)) {
			System.out.println("\nEste cenário não pode ser explorado.");

			return;
		}

		if (explorationSession == null) {
			explorationSession = new ExplorationSession(combatScenario.getExplorationSettings());
		}

		ExplorationSettings settings = explorationSession.getSettings();

		if (explorationSession.isComplete()) {
			System.out.println("\nVocê já explorou toda esta região.");

			return;
		}

		shouldDisplayScenarioDescription = false;
		pendingExplorationMessages.clear();
		firstAnimatedExplorationMessageIndex = -1;
		narratedExplorationPosition = explorationSession.getCurrentPosition();

		if (currentScenario instanceof Dungeon dungeon && explorationSession.isAtFinalPosition()) {
			DungeonBossAccess bossAccess = getDungeonBossAccess(dungeon);

			if (bossAccess == DungeonBossAccess.AVAILABLE) {
				throw new IllegalStateException("The final dungeon position must be resolved through the boss action.");
			}

			if (bossAccess == DungeonBossAccess.GATED) {
				executeDungeonBossGate(dungeon);
				return;
			}

			completeCurrentExploration();

			queueExplorationMessage("Você chegou ao fim da exploração de " + currentScenario.getName() + "!");

			return;
		}

		int position = explorationSession.getCurrentPosition();

		boolean specialArea = explorationSession.determineSpecialArea(random);

		queueExplorationMessage(
				specialArea
						? settings.getSpecialAreaMessage()
						: settings.getNormalAreaMessage()
		);

        boolean explorationContinues =
                resolveCurrentExplorationEvent(settings, position, specialArea);

		if (!explorationContinues) {
			return;
		}

		advanceOrCompleteExploration();

		if (explorationSession.isComplete()) {
			queueExplorationMessage(
			        "Você chegou ao fim da exploração de "
			                + currentScenario.getName() + "!");
		}
	}

	private boolean startRandomEncounter(int position, boolean specialArea) {
		if (!(currentScenario instanceof CombatScenario combatScenario)) {
            throw new IllegalStateException(
                    "A random encounter can only occur inside a combat scenario.");
		}

		ExplorationSettings settings = combatScenario.getExplorationSettings();

        List<EnemyDefinition> selectedDefinitions =
                encounterGenerator.generateGroup(
				currentScenario.getId(),
				position,
				specialArea,
				settings.getMinEnemies(),
                        settings.getMaxEnemies());

		if (selectedDefinitions.isEmpty()) {
			queueExplorationMessage("Nenhum inimigo foi encontrado nesta região.");
			firstAnimatedExplorationMessageIndex = 0;
			return true;
		}

		queueExplorationMessage("Inimigos se aproximam!");

		combatScenarioMenu.displayExplorationEncounter(
		        currentScenario.getName(),
		        explorationSession,
		        List.copyOf(pendingExplorationMessages)
		);

		BattleResult result = startEncounter(selectedDefinitions, BattleType.NORMAL);
		queueExplorationBattleOutcome(result);
		return result != BattleResult.DEFEAT;
	}

	private void queueExplorationBattleOutcome(BattleResult result) {
		switch (result) {
		case VICTORY -> queueExplorationMessage("Com os inimigos derrotados, você segue adiante.");
		case FLED -> queueExplorationMessage("Depois de escapar dos inimigos, você segue adiante.");
		case DEFEAT -> {
			// The existing rescue flow presents the defeat.
		}
		}

		if (result != BattleResult.DEFEAT) {
			firstAnimatedExplorationMessageIndex = pendingExplorationMessages.size() - 1;
		}
	}

	private boolean executeExplorationEvent(ExplorationEventType eventType, int position, boolean specialArea) {
		switch (eventType) {
		case BATTLE -> {
			return startRandomEncounter(position, specialArea);
		}

		case MERCHANT -> executeMerchantEvent();

		case EPIC_ENCOUNTER -> {
			return executeEpicEncounter();
		}

		case REWARD -> {
			executeRewardEvent(specialArea);
			firstAnimatedExplorationMessageIndex = 0;
		}

		case NOTHING -> {
			queueExplorationMessage("Você explorou os arredores, mas não encontrou nada.");
			firstAnimatedExplorationMessageIndex = 0;
		}
		}

		return true;
	}

	private void executeRewardEvent(boolean specialArea) {
        ExplorationReward reward =
                explorationRewardGenerator.generate(currentScenario.getId(), specialArea);

		switch (reward.getType()) {
		case ITEM -> {
			Item item = itemCatalog.getById(reward.getRewardId());

			int foundQuantity = reward.getQuantity();

                int grantedQuantity =
                        Math.min(foundQuantity, party.getInventory().getRemainingCapacity(item));

			if (grantedQuantity > 0 && !party.getInventory().addItem(item, grantedQuantity)) {
                    throw new IllegalStateException(
                            "Validated exploration item reward could not be added.");
                }

                if (grantedQuantity > 0) {
                    QuestProgressEvent itemEvent =
                            new QuestProgressEvent(
                                    QuestProgressEventType.ITEM_OBTAINED,
                                    Optional.of(item.getId()),
                                    grantedQuantity);

                    questProgressCoordinator.processAndDisplay(List.of(itemEvent));
                }

				int discardedQuantity = Math.subtractExact(foundQuantity, grantedQuantity);

				queueExplorationMessage("Você encontrou " + item.getName() + " x" + foundQuantity + "!");

				if (discardedQuantity > 0) {
					queueExplorationMessage(
							"Recebido: x" + grantedQuantity + " | Descartado: x" + discardedQuantity
									+ " (limite do inventário)."
					);
				}
			}

		case GOLD -> {
			int foundGold = reward.getQuantity();

			int grantedGold = Math.min(foundGold, party.getRemainingGoldCapacity());

			if (!party.addGold(grantedGold)) {
                    throw new IllegalStateException(
                            "Validated exploration GOLD reward could not be added.");
			}

			int discardedGold = Math.subtractExact(foundGold, grantedGold);

			queueExplorationMessage("Você encontrou " + GameCurrency.format(foundGold) + "!");

			if (discardedGold > 0) {
				queueExplorationMessage(
						"Recebido: " + GameCurrency.format(grantedGold) + " | Descartado: "
								+ GameCurrency.format(discardedGold) + " (limite da carteira)."
				);
			}
		}
		}
	}

	private void executeMerchantEvent() {
		explorationSession.markMerchantEncountered();

		ShopState merchantState = selectExplorationMerchant();
		queueExplorationMessage("Você encontra " + merchantState.getDefinition().getName() + ".");

		combatScenarioMenu.displayExplorationEncounter(
				currentScenario.getName(),
				explorationSession,
				List.copyOf(pendingExplorationMessages)
		);
		shopMenu.open(currentScenario.getName(), merchantState);
		queueExplorationMessage("Você se despede do mercador e retoma a exploração.");

		firstAnimatedExplorationMessageIndex = pendingExplorationMessages.size() - 1;
	}

	private ShopState selectExplorationMerchant() {
        List<ShopDefinition> eligibleMerchants =
                shopCatalog.getByScenarioId(currentScenario.getId()).stream()
                        .filter(
                                shopDefinition ->
                                        shopDefinition.getAccessType()
                                                == ShopAccessType.EXPLORATION)
                        .toList();

		if (eligibleMerchants.isEmpty()) {
			throw new IllegalStateException(
                    "No eligible exploration merchant is available for scenario: "
                            + currentScenario.getId());
		}

		int selectedIndex = random.nextInt(eligibleMerchants.size());

		ShopDefinition selectedMerchant = eligibleMerchants.get(selectedIndex);

		return shopStateCatalog.getByShopId(selectedMerchant.getId());
	}

    private boolean isEpicEncounterAvailable(Dungeon dungeon, int position, boolean specialArea) {
        if (!explorationSession.canOfferEpicEncounter()) {
            return false;
        }

        if (!dungeonProgress.isEpicAvailable(dungeon.getId(), worldClock.getElapsedHours())) {
            return false;
        }

        if (position < dungeon.getDungeonSettings().getEpicMinPosition()) {
            return false;
        }

        return switch (dungeon.getDungeonSettings().getEpicAreaRestriction()) {
            case ANY -> true;
            case NORMAL_ONLY -> !specialArea;
            case SPECIAL_ONLY -> specialArea;
        };
    }

	private boolean executeEpicEncounter() {
		if (!(currentScenario instanceof Dungeon dungeon)) {
			throw new IllegalStateException("An epic encounter can only occur inside a dungeon.");
		}

		explorationSession.markEpicEncounterOffered();

		List<EnemyDefinition> epicDefinitions = resolveEnemyDefinitions(dungeon.getDungeonSettings().getEpicEnemyIds());

		queueExplorationMessage("Uma presença poderosa surge no caminho.");

		combatScenarioMenu.displayExplorationEncounter(
				currentScenario.getName(),
				explorationSession,
				List.copyOf(pendingExplorationMessages)
		);

		boolean accepted = epicEncounterMenu.confirmEncounter(currentScenario.getName(), epicDefinitions);

		if (!accepted) {
			queueExplorationMessage("Você decidiu evitar o confronto.");
			firstAnimatedExplorationMessageIndex = pendingExplorationMessages.size() - 1;
			return true;
		}

		BattleResult result = startEncounter(epicDefinitions, BattleType.EPIC);

		if (result == BattleResult.VICTORY) {
			dungeonProgress.recordEpicVictory(dungeon.getId(), worldClock.getElapsedHours());
		}

		queueExplorationBattleOutcome(result);
		return result != BattleResult.DEFEAT;
	}

	private List<EnemyDefinition> resolveEnemyDefinitions(List<String> enemyIds) {
		return enemyIds.stream().map(enemyCatalog::getById).toList();
	}

	private void advanceOrCompleteExploration() {

		if (explorationSession.isAtFinalPosition()) {
			completeCurrentExploration();
		} else {
			explorationSession.advance();
		}
	}

	private void completeCurrentExploration() {
		explorationSession.markComplete();
	}

	private BattleResult startEncounter(
			List<EnemyDefinition> enemyDefinitions,
			BattleType battleType
	) {
		if (!(currentScenario instanceof CombatScenario combatScenario)) {
			throw new IllegalStateException("A battle can only start inside a combat scenario.");
		}

		BattleResult result =
				battleCoordinator.start(enemyDefinitions, combatScenario, battleType);

		if (result == BattleResult.VICTORY || result == BattleResult.DEFEAT) {
			advanceWorldTime(HOURS_PER_FINISHED_BATTLE);
		}

		if (result == BattleResult.DEFEAT) {
			rescueParty(combatScenario);
		}

		return result;
	}

	private void advanceWorldTime(int hours) {
		boolean crossesMidnight = worldClock.crossesMidnightAfter(hours);

		if (!crossesMidnight) {
			worldClock.advanceHours(hours);
			return;
		}

		ShopStateCatalog replacementShopStateCatalog = shopStateGenerator.generate();

		worldClock.advanceHours(hours);
		shopStateCatalog = replacementShopStateCatalog;
	}

	private void executeCityTransportMenu() {
        List<CityTransportDefinition> availableDestinations =
                getAvailableCityTransportDestinations();

		if (availableDestinations.isEmpty()) {
			throw new IllegalStateException("There are no available city transport destinations.");
		}

		Optional<CityTransportDefinition> selectedDestination =
				cityTransportMenu.open(
						currentScenario.getName(),
						availableDestinations,
						getCityTransportDestinationSlotCount(),
						getMarkedDestinationScenarioIds()
				);

		if (selectedDestination.isEmpty()) {
			return;
		}

		CityTransportDefinition destinationDefinition = selectedDestination.get();

        Scenario destination =
                scenarioCatalog.getById(destinationDefinition.getDestinationScenarioId());

		changeScenario(destination);
	}

	private void executeRouteMenu() {
		if (currentScenario instanceof CombatScenario
		        && (explorationSession == null || !explorationSession.isComplete())) {
		    pendingExplorationMessages.clear();
		    queueExplorationMessage("Você permanece diante do caminho bloqueado.");
		    return;
		}

		Optional<TravelRouteDefinition> selectedRoute =
				routeMenu.open(
						currentScenario.getId(),
						currentScenario.getName(),
						getMarkedDestinationScenarioIds()
				);

		if (selectedRoute.isEmpty()) {
			return;
		}

		TravelRouteDefinition route = selectedRoute.get();

		Scenario destination = scenarioCatalog.getById(route.getDestinationScenarioId());

		if (destination instanceof CombatScenario) {
            int startingPosition =
                    route.getDestinationExplorationPosition()
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Travel route to combat scenario has no destination position: "
                                                            + route.getId()));

			changeScenario(destination, startingPosition);

			return;
		}

		changeScenario(destination);
	}

	private City resolveRescueDestination(CombatScenario defeatedScenario) {
        Optional<String> preferredRescueScenarioId =
                defeatedScenario.getPreferredRescueScenarioId();

		String destinationScenarioId;

        if (preferredRescueScenarioId.isPresent()
                && worldProgress.isDiscovered(preferredRescueScenarioId.get())) {
			destinationScenarioId = preferredRescueScenarioId.get();

		} else {
			destinationScenarioId = defeatedScenario.getFallbackRescueScenarioId();
		}

		Scenario destination = scenarioCatalog.getById(destinationScenarioId);

		if (!(destination instanceof City city)) {
            throw new IllegalStateException(
                    "Rescue destination is not a city: " + destinationScenarioId);
		}

		return city;
	}

	private int calculateRescueCost(InnDefinition destinationInn) {
		int currentGold = party.getGold();

		int proportionalCost = currentGold / 10;

		if (currentGold % 10 != 0) {
			proportionalCost++;
		}

		return Math.min(currentGold, Math.max(destinationInn.getPrice(), proportionalCost));
	}

	private void rescueParty(CombatScenario defeatedScenario) {
		City rescueDestination = resolveRescueDestination(defeatedScenario);

        Optional<InnDefinition> destinationInnDefinition =
                innCatalog.findByScenarioId(rescueDestination.getId());

		if (destinationInnDefinition.isEmpty()) {
            throw new IllegalStateException(
                    "No Inn is available for rescue destination: " + rescueDestination.getId());
		}

		InnDefinition destinationInn = destinationInnDefinition.get();

		int rescueCost = calculateRescueCost(destinationInn);

		if (rescueCost > 0 && !party.spendGold(rescueCost)) {
			throw new IllegalStateException("Party gold changed before rescue payment.");
		}

		party.fullyRestoreMembers();

		partyRescueScreen.show(party, defeatedScenario, rescueDestination, rescueCost);

		changeScenario(rescueDestination);
	}

	private void changeScenario(Scenario destination, int startingPosition) {
		currentScenario = destination;
		pendingExplorationMessages.clear();
		firstAnimatedExplorationMessageIndex = -1;
		narratedExplorationPosition = -1;
		worldProgress.discover(destination.getId());

		if (destination instanceof CombatScenario combatScenario) {
            explorationSession =
                    new ExplorationSession(
                            combatScenario.getExplorationSettings(), startingPosition);

		} else {
			explorationSession = null;
		}

		shouldDisplayScenarioDescription = true;

		questProgressCoordinator.processAndDisplay(
		        List.of(
		                new QuestProgressEvent(
		                        QuestProgressEventType.SCENARIO_REACHED,
		                        Optional.of(destination.getId()),
		                        1)));
	}

	private void changeScenario(Scenario destination) {
		changeScenario(destination, 1);
	}

	private DungeonBossAccess getDungeonBossAccess(Dungeon dungeon) {
		return dungeonBossAccessResolver.resolve(dungeon, worldClock.getElapsedHours());
	}

	private void executeDungeonBossGate(Dungeon dungeon) {
		DungeonBossGateDefinition gateDefinition = dungeonBossAccessResolver.getGateDefinition(dungeon);

		displayDialogue(
				dungeonBossAccessResolver
						.resolveBlockedDialogueId(dungeon),
				"Intervenção"
		);

		Scenario destination = scenarioCatalog.getById(gateDefinition.getBlockedDestinationScenarioId());
		changeScenario(destination);
	}

	private boolean isDungeonBossAvailable(Dungeon dungeon) {
		return getDungeonBossAccess(dungeon) == DungeonBossAccess.AVAILABLE;
	}

	private void executeBossEncounter(Dungeon dungeon) {
        String dungeonId = dungeon.getId();

        boolean narrativeBoss = !dungeonProgress.isNarrativeBossDefeated(dungeonId);

        List<String> bossEnemyIds =
                narrativeBoss
                        ? dungeon.getDungeonSettings().getBossEnemyIds()
                        : dungeon.getDungeonSettings().getRecurringBossEnemyIds();

        List<EnemyDefinition> bossDefinitions = resolveEnemyDefinitions(bossEnemyIds);

		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(dungeon.getName(), "Boss");

		System.out.println();
		System.out.println("O caminho está bloqueado por:");

		for (EnemyDefinition bossDefinition : bossDefinitions) {
			System.out.println("- " + bossDefinition.getName());
		}

		BattleResult result = startEncounter(bossDefinitions, BattleType.BOSS);

		if (result == BattleResult.VICTORY) {
			long elapsedHours = worldClock.getElapsedHours();

			if (narrativeBoss) {
				dungeonProgress.recordNarrativeBossVictory(dungeonId, elapsedHours);
			} else {
				dungeonProgress.recordRecurringBossVictory(dungeonId, elapsedHours);
			}

			completeCurrentExploration();

			questProgressCoordinator.processAndDisplay(
			        List.of(
			                new QuestProgressEvent(
			                        QuestProgressEventType.DUNGEON_COMPLETED,
			                        Optional.of(dungeonId),
			                        1)));

		} else if (result == BattleResult.FLED) {
			System.out.println("\nVocê permanece diante do caminho bloqueado.");
		}
	}

    private boolean resolveCurrentExplorationEvent(
            ExplorationSettings settings, int position, boolean specialArea) {
        boolean epicEncounterAvailable =
                currentScenario instanceof Dungeon dungeon
                        && isEpicEncounterAvailable(dungeon, position, specialArea);

        ExplorationEventType eventType =
                explorationEventGenerator.generate(
                        settings,
                        specialArea,
                        explorationSession.canEncounterMerchant(),
                        epicEncounterAvailable);

		return executeExplorationEvent(eventType, position, specialArea);
	}

	private void requestExitWithoutSaving() {
		if (exitConfirmationMenu.confirm()) {
			terminateGame();
		}
	}

	private void terminateGame() {
		System.out.println("\nObrigado por testar a versão atual!");
		running = false;
	}

	private List<CrafterDefinition> getCityCrafters(City city) {
		return crafterCatalog.getByScenarioId(city.getId());
	}

	private List<ShopDefinition> getCityShops(City city) {
		return shopCatalog.getByScenarioId(city.getId()).stream()
                .filter(shopDefinition -> shopDefinition.getAccessType() == ShopAccessType.CITY)
                .toList();
	}

	private Set<String> getPendingReachScenarioIds() {
	    Set<String> pendingScenarioIds = new LinkedHashSet<>();

	    for (QuestDefinition questDefinition : questProgress.getActiveQuests()) {
	        QuestStepDefinition currentStep =
	                questProgress
	                        .getCurrentStep(questDefinition.getId())
	                        .orElseThrow(
	                                () ->
	                                        new IllegalStateException(
	                                                "Active quest is missing its current step: "
	                                                        + questDefinition.getId()));

	        for (QuestObjectiveDefinition objective : currentStep.getObjectives()) {
	            if (objective.getObjectiveType() != QuestObjectiveType.REACH_SCENARIO) {
	                continue;
	            }

	            int currentProgress =
	                    questProgress.getObjectiveProgress(
	                            questDefinition.getId(),
	                            objective.getId());

	            if (currentProgress >= objective.getRequiredQuantity()) {
	                continue;
	            }

	            pendingScenarioIds.add(objective.getTargetId().orElseThrow());
	        }
	    }

	    return Set.copyOf(pendingScenarioIds);
	}

	private boolean hasPendingReachRouteFrom(
	        String sourceScenarioId,
	        Set<String> pendingScenarioIds) {
	    for (TravelRouteDefinition route :
	            travelProgress.getUnlockedRoutesFrom(sourceScenarioId)) {
	        if (pendingScenarioIds.contains(route.getDestinationScenarioId())) {
	            return true;
	        }
	    }

	    return false;
	}

	private boolean hasPendingCityTransportDestination(
	        Set<String> pendingScenarioIds) {
	    for (CityTransportDefinition destination :
	            getAvailableCityTransportDestinations()) {
	        if (pendingScenarioIds.contains(destination.getDestinationScenarioId())) {
	            return true;
	        }
	    }

	    return false;
	}

	private Set<CityAction> getMarkedCityActions(City city, List<CityAction> availableActions) {
		Set<String> markedDestinationIds = getMarkedDestinationScenarioIds();
		Set<CityAction> markedActions = new LinkedHashSet<>();

		if (availableActions.contains(CityAction.VISIT_LOCATION) && hasQuestInteractionInCity(city)) {
			markedActions.add(CityAction.VISIT_LOCATION);
		}

		if (availableActions.contains(CityAction.LEAVE_CITY)
		        && hasPendingReachRouteFrom(city.getId(), markedDestinationIds)) {
		    markedActions.add(CityAction.LEAVE_CITY);
		}

		if (availableActions.contains(CityAction.CITY_TRANSPORT)
				&& hasPendingCityTransportDestination(markedDestinationIds)) {
			markedActions.add(CityAction.CITY_TRANSPORT);
		}

		return Set.copyOf(markedActions);
	}

	private Set<CombatScenarioAction> getMarkedCombatScenarioActions(List<CombatScenarioAction> availableActions) {
		Set<String> markedDestinationIds = getMarkedDestinationScenarioIds();
		Set<CombatScenarioAction> markedActions = new LinkedHashSet<>();

		if (availableActions.contains(CombatScenarioAction.FOLLOW_ROUTE)
		        && hasPendingReachRouteFrom(
		                currentScenario.getId(), markedDestinationIds)) {
		    markedActions.add(CombatScenarioAction.FOLLOW_ROUTE);
		}

		if (availableActions.contains(CombatScenarioAction.CITY_TRANSPORT)
				&& hasPendingCityTransportDestination(markedDestinationIds)) {
			markedActions.add(CombatScenarioAction.CITY_TRANSPORT);
		}

		if (availableActions.contains(CombatScenarioAction.FOLLOW_BRANCH) && travelProgress
				.getUnlockedBranchesAt(currentScenario.getId(), explorationSession.getCurrentPosition()).stream()
				.anyMatch(branch -> markedDestinationIds.contains(branch.getDestinationScenarioId()))) {
			markedActions.add(CombatScenarioAction.FOLLOW_BRANCH);
		}

		return Set.copyOf(markedActions);
	}

	private Set<String> getMarkedDestinationScenarioIds() {
	    Set<String> markedIds = new LinkedHashSet<>(getPendingReachScenarioIds());

	    for (Scenario scenario : scenarioCatalog.getAll()) {
	        if (scenario instanceof City city && hasQuestInteractionInCity(city)) {
	            markedIds.add(city.getId());
	        }
	    }

	    return Set.copyOf(markedIds);
	}

	private void queueExplorationMessage(String message) {
		if (message == null || message.isBlank()) {
			throw new IllegalArgumentException(
					"Exploration message cannot be empty."
			);
		}

		pendingExplorationMessages.add(message);
	}

	private List<String> getExplorationMessages() {
	    return List.copyOf(pendingExplorationMessages);
	}
}
