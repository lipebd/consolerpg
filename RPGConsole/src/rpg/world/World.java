package rpg.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;

import rpg.GameCurrency;
import rpg.battle.BattleCoordinator;
import rpg.battle.BattleResult;
import rpg.battle.rewards.BattleRewardService;
import rpg.characters.Party;
import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyDefinition;
import rpg.enemies.EnemyDropCatalog;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.items.equipment.EquipmentCatalog;
import rpg.items.equipment.EquipmentService;
import rpg.persistence.CampaignSaveResult;
import rpg.persistence.CampaignSaveService;
import rpg.persistence.CampaignSnapshot;
import rpg.persistence.CampaignSnapshotFactory;
import rpg.ui.EquipmentMenu;
import rpg.ui.BattleRewardScreen;
import rpg.ui.CityLocationMenu;
import rpg.ui.CityMenu;
import rpg.ui.CityTransportMenu;
import rpg.ui.CombatScenarioMenu;
import rpg.ui.ConsoleInput;
import rpg.ui.ConsoleUI;
import rpg.ui.EpicEncounterMenu;
import rpg.ui.ExitConfirmationMenu;
import rpg.ui.InnMenu;
import rpg.ui.InventoryMenu;
import rpg.ui.PartyRescueScreen;
import rpg.ui.PartyStatusMenu;
import rpg.ui.RouteMenu;
import rpg.ui.ScenarioBranchMenu;
import rpg.ui.ShopMenu;
import rpg.ui.CraftingMenu;
import rpg.world.crafting.CrafterCatalog;
import rpg.world.crafting.CrafterDefinition;
import rpg.world.crafting.CraftingRecipeCatalog;
import rpg.world.branches.ScenarioBranchCatalog;
import rpg.world.branches.ScenarioBranchDefinition;
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
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.shops.ShopAccessType;
import rpg.world.shops.ShopCatalog;
import rpg.world.shops.ShopDefinition;
import rpg.world.shops.ShopState;
import rpg.world.shops.ShopStateCatalog;
import rpg.world.transport.CityTransportCatalog;
import rpg.world.transport.CityTransportDefinition;
import rpg.world.travel.TravelProgress;
import rpg.world.travel.TravelRouteDefinition;
import rpg.world.shops.ShopStateGenerator;
import rpg.world.scenarios.DungeonProgress;

// Controls navigation and interaction with the game world.
public class World {

	private static final int HOURS_PER_FINISHED_BATTLE = 1;

	private final CampaignSaveService campaignSaveService;

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

	private final ItemCatalog itemCatalog;
	private final CrafterCatalog crafterCatalog;
	private final CityTransportCatalog cityTransportCatalog;
	private final ScenarioCatalog scenarioCatalog;
	private final ScenarioBranchCatalog scenarioBranchCatalog;
	private final EnemyCatalog enemyCatalog;
	private final ShopCatalog shopCatalog;
	private final ShopStateGenerator shopStateGenerator;
	private ShopStateCatalog shopStateCatalog;

	private final ExplorationRewardGenerator explorationRewardGenerator;
	private final ExplorationEventGenerator explorationEventGenerator;
	private final EncounterGenerator encounterGenerator;

	private final WorldProgress worldProgress;
    private final DungeonProgress dungeonProgress;
	private final WorldClock worldClock;
	private final TravelProgress travelProgress;
	private final BattleCoordinator battleCoordinator;

	private Party party;
	private ExplorationSession explorationSession;
	private Random random;
	private Scenario currentScenario;

	private boolean running;
	private boolean shouldDisplayScenarioDescription;
	private boolean scenarioNameAlreadyDisplayed;

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
			WorldClock worldClock,
			CityTransportCatalog cityTransportCatalog,
			InnCatalog innCatalog,
			ScenarioBranchCatalog scenarioBranchCatalog,
            TravelProgress travelProgress) {

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
        this.scenarioBranchCatalog =
                Objects.requireNonNull(
                        scenarioBranchCatalog, "Scenario branch catalog cannot be null.");
        this.travelProgress =
                Objects.requireNonNull(travelProgress, "Travel progress cannot be null.");
        this.crafterCatalog =
                Objects.requireNonNull(crafterCatalog, "Crafter catalog cannot be null.");
		this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");
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

		// Shared random generator and gameplay services
		this.random = new Random();
		BattleRewardService rewardService = new BattleRewardService(random);
		BattleRewardScreen rewardScreen = new BattleRewardScreen(consoleInput);
        this.battleCoordinator =
                new BattleCoordinator(
				this.party,
				consoleInput,
				enemyDropCatalog,
				rewardService,
				rewardScreen,
                        random);
        this.encounterGenerator =
                new EncounterGenerator(this.enemyCatalog, encounterCatalog, random);
        this.explorationRewardGenerator =
                new ExplorationRewardGenerator(explorationRewardCatalog, random);
		this.explorationEventGenerator = new ExplorationEventGenerator(random);
        this.campaignSaveService =
                Objects.requireNonNull(
                        campaignSaveService, "Campaign save service cannot be null.");

		// User interface
		this.epicEncounterMenu = new EpicEncounterMenu(consoleInput);
		this.combatScenarioMenu = new CombatScenarioMenu(consoleInput, this.worldClock);
		this.routeMenu = new RouteMenu(consoleInput, this.travelProgress);
		this.scenarioBranchMenu = new ScenarioBranchMenu(consoleInput);
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
        this.craftingMenu =
                new CraftingMenu(
				this.party,
				consoleInput,
				this.itemCatalog,
                        Objects.requireNonNull(
                                craftingRecipeCatalog, "Crafting recipe catalog cannot be null."));
		this.innMenu = new InnMenu(this.party, consoleInput);
		this.shopMenu = new ShopMenu(this.party, consoleInput, this.worldClock);
		this.partyRescueScreen = new PartyRescueScreen(consoleInput);
		this.exitConfirmationMenu = new ExitConfirmationMenu(consoleInput);

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
		this.scenarioNameAlreadyDisplayed = false;
	}

	public void start() {
		while (running) {
			displayCurrentScenario();
			executeCurrentScenarioMenu();
		}
	}

	private void displayCurrentScenario() {
		if (!scenarioNameAlreadyDisplayed || shouldDisplayScenarioDescription) {
			displayScenarioName();
		}

		scenarioNameAlreadyDisplayed = false;

		if (shouldDisplayScenarioDescription) {
			System.out.println(currentScenario.getDescription());
			shouldDisplayScenarioDescription = false;
		}

		displayExplorationProgress();
	}

	private void displayExplorationProgress() {
		if (!(currentScenario instanceof CombatScenario) || explorationSession == null) {
			return;
		}

		if (explorationSession.isComplete()) {
			System.out.println("\n[EXPLORAÇÃO CONCLUÍDA]");
			return;
		}

		System.out.println(
                "\n[EXPLORAÇÃO "
                        + explorationSession.getCurrentPosition()
                        + "/"
                        + explorationSession.getSettings().getLength()
                        + "]");

        if (currentScenario instanceof Dungeon dungeon
                && explorationSession.isAtFinalPosition()
                && isDungeonBossAvailable(dungeon)) {
			displayDungeonBossObstacle();
		}
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
                cityMenu.selectAction(city.getName(), availableActions);

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

		if (hasInn || hasCityShop || hasCrafter) {
			availableActions.add(CityAction.VISIT_LOCATION);
		}

		if (travelProgress.hasUnlockedRoutesFrom(city.getId())) {
			availableActions.add(CityAction.LEAVE_CITY);
		}

		if (!getAvailableCityTransportDestinations().isEmpty()) {
			availableActions.add(CityAction.CITY_TRANSPORT);
		}

		availableActions.add(CityAction.INVENTORY);
		availableActions.add(CityAction.VIEW_STATUS);

		return List.copyOf(availableActions);
	}

	private void executeCityAction(City city, CityAction action) {
		switch (action) {
		case VISIT_LOCATION -> executeCityLocationMenu(city);

		case LEAVE_CITY -> executeRouteMenu();

		case INVENTORY -> inventoryMenu.open();

		case VIEW_STATUS -> partyStatusMenu.open();

		case CITY_TRANSPORT -> executeCityTransportMenu();
		}
	}

	private void executeCityLocationMenu(City city) {
		Optional<InnDefinition> innDefinition = innCatalog.findByScenarioId(city.getId());

		List<ShopDefinition> cityShops = getCityShops(city);
		List<CrafterDefinition> cityCrafters = getCityCrafters(city);

		List<String> locationNames = new ArrayList<>();

		innDefinition.ifPresent(definition -> locationNames.add(definition.getName()));

		for (ShopDefinition shopDefinition : cityShops) {
			locationNames.add(shopDefinition.getName());
		}

		for (CrafterDefinition crafterDefinition : cityCrafters) {
			locationNames.add(crafterDefinition.getName());
		}

		OptionalInt selectedLocation = cityLocationMenu.selectLocation(locationNames);

		if (selectedLocation.isEmpty()) {
			return;
		}

		int selectedIndex = selectedLocation.getAsInt();

		if (innDefinition.isPresent()) {
			if (selectedIndex == 0) {
				executeInnMenu(innDefinition.get());
				return;
			}

			selectedIndex--;
		}

		if (selectedIndex < cityShops.size()) {
			ShopDefinition selectedShop = cityShops.get(selectedIndex);

			ShopState selectedShopState = shopStateCatalog.getByShopId(selectedShop.getId());

			shopMenu.open(selectedShopState);
			return;
		}

		selectedIndex -= cityShops.size();

		CrafterDefinition selectedCrafter = cityCrafters.get(selectedIndex);

		craftingMenu.open(selectedCrafter);
	}

	private void executeInnMenu(InnDefinition innDefinition) {
		while (running) {
			InnMenu.Selection selection = innMenu.open(innDefinition);

			if (selection == InnMenu.Selection.BACK) {
				return;
			}

			CampaignSaveResult saveResult = saveCampaign(innDefinition);

			if (!saveResult.isSuccess()) {
				displaySaveFailure(saveResult);
				continue;
			}

			System.out.println();
			System.out.println("Campanha salva com sucesso.");

			if (selection == InnMenu.Selection.SAVE_AND_EXIT) {
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
                            shopStateCatalog);
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
		List<CombatScenarioAction> availableActions = getAvailableCombatScenarioActions();

        Optional<CombatScenarioAction> selectedAction =
                combatScenarioMenu.selectAction(availableActions);

		if (selectedAction.isEmpty()) {
			requestExitWithoutSaving();
			return;
		}

		executeCombatScenarioAction(selectedAction.get());
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
		availableActions.add(CombatScenarioAction.VIEW_STATUS);

		return List.copyOf(availableActions);
	}

	private boolean hasAvailableScenarioBranch() {
		if (explorationSession == null || explorationSession.isComplete()) {
			return false;
		}

        return scenarioBranchCatalog.hasBranchesAt(
                currentScenario.getId(), explorationSession.getCurrentPosition());
	}

	private void executeCombatScenarioAction(CombatScenarioAction action) {
		switch (action) {
		case EXPLORE -> explore();

		case FACE_BOSS -> faceBoss();

		case VIEW_STATUS -> partyStatusMenu.open();

		case INVENTORY -> inventoryMenu.open();

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
                scenarioBranchCatalog.getBranchesAt(
                        currentScenario.getId(), explorationSession.getCurrentPosition());

        Optional<ScenarioBranchDefinition> selectedBranch =
                scenarioBranchMenu.open(availableBranches);

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

        if (currentScenario instanceof Dungeon dungeon && explorationSession.isAtFinalPosition()) {
            if (isDungeonBossAvailable(dungeon)) {
                throw new IllegalStateException(
                        "The final dungeon position must be resolved through the boss action.");
            }

            completeCurrentExploration();

            System.out.println(
                    "\nVocê chegou ao fim da exploração de " + currentScenario.getName() + "!");

            return;
		}

		int position = explorationSession.getCurrentPosition();

		boolean specialArea = explorationSession.determineSpecialArea(random);

		displayCurrentExplorationArea(settings, specialArea);

        boolean explorationContinues =
                resolveCurrentExplorationEvent(settings, position, specialArea);

		if (!explorationContinues) {
			return;
		}

		advanceOrCompleteExploration();

		if (explorationSession.isComplete()) {
            System.out.println(
                    "\nVocê chegou ao fim da exploração de " + currentScenario.getName() + "!");
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
			System.out.println("Nenhum inimigo foi encontrado nesta região.");

			return true;
		}

		return startEncounter(selectedDefinitions) != BattleResult.DEFEAT;
	}

    private boolean executeExplorationEvent(
            ExplorationEventType eventType, int position, boolean specialArea) {
		switch (eventType) {
		case BATTLE -> {
			return startRandomEncounter(position, specialArea);
		}

		case REWARD -> executeRewardEvent(specialArea);

		case MERCHANT -> executeMerchantEvent();

		case EPIC_ENCOUNTER -> {
			return executeEpicEncounter();
		}

            case NOTHING ->
                    System.out.println("Você explorou os arredores, mas não encontrou nada.");
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

			int discardedQuantity = Math.subtractExact(foundQuantity, grantedQuantity);

			System.out.println("Você encontrou " + item.getName() + " x" + foundQuantity + "!");

			if (discardedQuantity > 0) {
				System.out.println(
                            "Recebido: x"
                                    + grantedQuantity
                                    + " | Descartado: x"
                                    + discardedQuantity
                                    + " (limite do inventário).");
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

			System.out.println("Você encontrou " + GameCurrency.format(foundGold) + "!");

			if (discardedGold > 0) {
				System.out.println(
                            "Recebido: "
                                    + GameCurrency.format(grantedGold)
                                    + " | Descartado: "
                                    + GameCurrency.format(discardedGold)
                                    + " (limite da carteira).");
			}
		}
		}
	}

	private void executeMerchantEvent() {
		explorationSession.markMerchantEncountered();

		ShopState merchantState = selectExplorationMerchant();

		System.out.println("\n[MERCADOR]");

		shopMenu.open(merchantState);
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

        List<EnemyDefinition> epicDefinitions =
                resolveEnemyDefinitions(dungeon.getDungeonSettings().getEpicEnemyIds());

		boolean accepted = epicEncounterMenu.confirmEncounter(epicDefinitions);

		if (!accepted) {
			System.out.println("Você decidiu evitar o confronto.");

			return true;
		}

        BattleResult result = startEncounter(epicDefinitions);

        if (result == BattleResult.VICTORY) {
            dungeonProgress.recordEpicVictory(dungeon.getId(), worldClock.getElapsedHours());
        }

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
		travelProgress.unlockRoutesFrom(currentScenario.getId());
	}

	private BattleResult startEncounter(List<EnemyDefinition> enemyDefinitions) {
		if (!(currentScenario instanceof CombatScenario combatScenario)) {
			throw new IllegalStateException("A battle can only start inside a combat scenario.");
		}

		BattleResult result = battleCoordinator.start(enemyDefinitions, combatScenario);

		if (result == BattleResult.VICTORY || result == BattleResult.DEFEAT) {
			advanceWorldTime(HOURS_PER_FINISHED_BATTLE);
		}

		switch (result) {
		case VICTORY, FLED -> shouldDisplayScenarioDescription = true;

		case DEFEAT -> rescueParty(combatScenario);
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
                        availableDestinations, getCityTransportDestinationSlotCount());

		if (selectedDestination.isEmpty()) {
			return;
		}

		CityTransportDefinition destinationDefinition = selectedDestination.get();

        Scenario destination =
                scenarioCatalog.getById(destinationDefinition.getDestinationScenarioId());

		changeScenario(destination);

		System.out.println("\n" + destinationDefinition.getTravelDescription());
	}

	private void executeRouteMenu() {
		if (currentScenario instanceof CombatScenario
				&& (explorationSession == null || !explorationSession.isComplete())) {
			System.out.println("\nVocê precisa concluir a exploração antes de viajar.");

			return;
		}

		Optional<TravelRouteDefinition> selectedRoute = routeMenu.open(currentScenario.getId());

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

		worldProgress.discover(destination.getId());

		if (destination instanceof CombatScenario combatScenario) {
            explorationSession =
                    new ExplorationSession(
                            combatScenario.getExplorationSettings(), startingPosition);

		} else {
			explorationSession = null;
		}

		shouldDisplayScenarioDescription = true;

		ConsoleUI.clearScreen();
	}

	private void changeScenario(Scenario destination) {
		changeScenario(destination, 1);
	}

    private boolean isDungeonBossAvailable(Dungeon dungeon) {
        String dungeonId = dungeon.getId();

        if (!dungeonProgress.isNarrativeBossDefeated(dungeonId)) {
            return true;
        }

        return dungeonProgress.isRecurringBossAvailable(dungeonId, worldClock.getElapsedHours());
    }

	private void executeBossEncounter(Dungeon dungeon) {
        String dungeonId = dungeon.getId();

        boolean narrativeBoss = !dungeonProgress.isNarrativeBossDefeated(dungeonId);

        List<String> bossEnemyIds =
                narrativeBoss
                        ? dungeon.getDungeonSettings().getBossEnemyIds()
                        : dungeon.getDungeonSettings().getRecurringBossEnemyIds();

        List<EnemyDefinition> bossDefinitions = resolveEnemyDefinitions(bossEnemyIds);

        System.out.println("\n[BOSS]");
		System.out.println("\nO caminho está bloqueado por:");

		for (EnemyDefinition bossDefinition : bossDefinitions) {
			System.out.println("- " + bossDefinition.getName());
		}

		BattleResult result = startEncounter(bossDefinitions);

		if (result == BattleResult.VICTORY) {
            long elapsedHours = worldClock.getElapsedHours();

            if (narrativeBoss) {
                dungeonProgress.recordNarrativeBossVictory(dungeonId, elapsedHours);
            } else {
                dungeonProgress.recordRecurringBossVictory(dungeonId, elapsedHours);
            }

			completeCurrentExploration();

		} else if (result == BattleResult.FLED) {
			System.out.println("\nVocê permanece diante do caminho bloqueado.");
		}
	}

	private void displayScenarioName() {
		System.out.println("\n[" + currentScenario.getName().toUpperCase() + "]");
	}

	private void displayDungeonBossObstacle() {
		String target = party.size() == 1 ? "você" : "o grupo";

		System.out.println("\nUma criatura se coloca entre " + target + " e o caminho de volta.");
	}

	private void displayCurrentExplorationArea(ExplorationSettings settings, boolean specialArea) {
		displayScenarioName();
		scenarioNameAlreadyDisplayed = true;

		System.out.println();

        String areaMessage =
                specialArea ? settings.getSpecialAreaMessage() : settings.getNormalAreaMessage();

		System.out.println(areaMessage);
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
		System.out.println("\nObrigado por testar a V4!");
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
}
