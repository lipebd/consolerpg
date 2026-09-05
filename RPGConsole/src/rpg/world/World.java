package rpg.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Scanner;

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
import rpg.ui.BattleRewardScreen;
import rpg.ui.CityLocationMenu;
import rpg.ui.CityMenu;
import rpg.ui.CityTransportMenu;
import rpg.ui.CombatScenarioMenu;
import rpg.ui.ConsoleUI;
import rpg.ui.EpicEncounterMenu;
import rpg.ui.GameOverScreen;
import rpg.ui.InnMenu;
import rpg.ui.InventoryMenu;
import rpg.ui.PartyStatusMenu;
import rpg.ui.RouteMenu;
import rpg.ui.ScenarioBranchMenu;
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
import rpg.world.transport.CityTransportCatalog;
import rpg.world.transport.CityTransportDefinition;
import rpg.world.travel.TravelProgress;
import rpg.world.travel.TravelRouteDefinition;

// Controls navigation and interaction with the game world.
public class World {

	private final BattleCoordinator battleCoordinator;

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
	private final EpicEncounterMenu epicEncounterMenu;

	private final ItemCatalog itemCatalog;
	private final CityTransportCatalog cityTransportCatalog;
	private final ScenarioCatalog scenarioCatalog;
	private final ScenarioBranchCatalog scenarioBranchCatalog;
	private final EnemyCatalog enemyCatalog;

	private final ExplorationRewardGenerator explorationRewardGenerator;
	private final ExplorationEventGenerator explorationEventGenerator;
	private final EncounterGenerator encounterGenerator;

	private final WorldProgress worldProgress;
	private final TravelProgress travelProgress;

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
			Scanner scanner,
			ItemCatalog itemCatalog,
			EnemyCatalog enemyCatalog,
			EnemyDropCatalog enemyDropCatalog,
			EncounterCatalog encounterCatalog,
			ExplorationRewardCatalog explorationRewardCatalog,
			ScenarioCatalog scenarioCatalog,
			WorldProgress worldProgress,
			CityTransportCatalog cityTransportCatalog,
			InnCatalog innCatalog,
			ScenarioBranchCatalog scenarioBranchCatalog,
			TravelProgress travelProgress
	) {

		Objects.requireNonNull(scanner, "Scanner cannot be null.");
		Objects.requireNonNull(enemyDropCatalog, "Enemy drop catalog cannot be null.");
		Objects.requireNonNull(encounterCatalog, "Encounter catalog cannot be null.");

		// Core world state and shared data
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.currentScenario = Objects.requireNonNull(initialScenario, "Initial scenario cannot be null.");
		this.enemyCatalog = Objects.requireNonNull(enemyCatalog, "Enemy catalog cannot be null.");
		this.scenarioCatalog = Objects.requireNonNull(scenarioCatalog, "Scenario catalog cannot be null.");
		this.worldProgress = Objects.requireNonNull(worldProgress, "World progress cannot be null.");
		this.scenarioBranchCatalog = Objects
				.requireNonNull(scenarioBranchCatalog, "Scenario branch catalog cannot be null.");
		this.travelProgress = Objects.requireNonNull(travelProgress, "Travel progress cannot be null.");
		this.itemCatalog = Objects.requireNonNull(itemCatalog, "Item catalog cannot be null.");
		this.innCatalog = Objects.requireNonNull(innCatalog, "Inn catalog cannot be null.");
		this.cityTransportCatalog = Objects
				.requireNonNull(cityTransportCatalog, "City transport catalog cannot be null.");
		this.worldProgress.discover(this.currentScenario.getId());

		// Shared random generator and gameplay services
		this.random = new Random();
		BattleRewardService rewardService = new BattleRewardService(random);
		BattleRewardScreen rewardScreen = new BattleRewardScreen(scanner);
		this.battleCoordinator = new BattleCoordinator(
				this.party,
				scanner,
				enemyDropCatalog,
				rewardService,
				rewardScreen,
				random
		);
		this.encounterGenerator = new EncounterGenerator(this.enemyCatalog, encounterCatalog, random);
		this.explorationRewardGenerator = new ExplorationRewardGenerator(explorationRewardCatalog, random);
		this.explorationEventGenerator = new ExplorationEventGenerator(random);

		// User interface
		this.epicEncounterMenu = new EpicEncounterMenu(scanner);
		this.combatScenarioMenu = new CombatScenarioMenu(scanner);
		this.routeMenu = new RouteMenu(scanner, this.travelProgress);
		this.scenarioBranchMenu = new ScenarioBranchMenu(scanner);
		this.inventoryMenu = new InventoryMenu(this.party, scanner);
		this.partyStatusMenu = new PartyStatusMenu(this.party, scanner);
		this.cityMenu = new CityMenu(scanner);
		this.cityTransportMenu = new CityTransportMenu(this.party, scanner, this.scenarioCatalog);
		this.cityLocationMenu = new CityLocationMenu(scanner);
		this.innMenu = new InnMenu(this.party, scanner);

		// Initial exploration state
		if (this.currentScenario instanceof CombatScenario combatScenario) {
			this.explorationSession = new ExplorationSession(combatScenario.getExplorationSettings());
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
				"\n[EXPLORAÇÃO " + explorationSession.getCurrentPosition() + "/"
						+ explorationSession.getSettings().getLength() + "]"
		);

		if (currentScenario instanceof Dungeon && explorationSession.isAtFinalPosition()) {
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

		throw new IllegalStateException("Unsupported scenario type: " + currentScenario.getClass().getName());
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

		Optional<CityAction> selectedAction = cityMenu.selectAction(city.getName(), availableActions);

		if (selectedAction.isEmpty()) {
			exitGame();
			return;
		}

		executeCityAction(city, selectedAction.get());
	}

	private List<CityAction> getAvailableCityActions(City city) {
		List<CityAction> availableActions = new ArrayList<>();

		if (innCatalog.findByScenarioId(city.getId()).isPresent()) {
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
		InnDefinition innDefinition = innCatalog.findByScenarioId(city.getId())
				.orElseThrow(() -> new IllegalStateException("City has no registered Inn: " + city.getId()));

		OptionalInt selectedLocation = cityLocationMenu.selectLocation(List.of(innDefinition.getName()));

		if (selectedLocation.isEmpty()) {
			return;
		}

		innMenu.open(innDefinition);
	}

	private void executeCombatScenarioMenu() {
		List<CombatScenarioAction> availableActions = getAvailableCombatScenarioActions();

		Optional<CombatScenarioAction> selectedAction = combatScenarioMenu.selectAction(availableActions);

		if (selectedAction.isEmpty()) {
			exitGame();
			return;
		}

		executeCombatScenarioAction(selectedAction.get());
	}

	private List<CombatScenarioAction> getAvailableCombatScenarioActions() {
		List<CombatScenarioAction> availableActions = new ArrayList<>();

		boolean combatScenarioInProgress = currentScenario instanceof CombatScenario
				&& (explorationSession == null || !explorationSession.isComplete());

		if (combatScenarioInProgress) {
			if (currentScenario instanceof Dungeon && explorationSession != null
					&& explorationSession.isAtFinalPosition()) {
				availableActions.add(CombatScenarioAction.FACE_BOSS);

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

		return scenarioBranchCatalog.hasBranchesAt(currentScenario.getId(), explorationSession.getCurrentPosition());
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

		List<ScenarioBranchDefinition> availableBranches = scenarioBranchCatalog
				.getBranchesAt(currentScenario.getId(), explorationSession.getCurrentPosition());

		Optional<ScenarioBranchDefinition> selectedBranch = scenarioBranchMenu.open(availableBranches);

		if (selectedBranch.isEmpty()) {
			return;
		}

		Scenario destination = scenarioCatalog.getById(selectedBranch.get().getDestinationScenarioId());

		changeScenario(destination);
	}

	private void faceBoss() {
		if (!(currentScenario instanceof Dungeon dungeon) || explorationSession == null
				|| !explorationSession.isAtFinalPosition() || explorationSession.isComplete()) {
			throw new IllegalStateException("There is no dungeon boss available here.");
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

		if (currentScenario instanceof Dungeon && explorationSession.isAtFinalPosition()) {
			throw new IllegalStateException("The final dungeon position must be resolved through the boss action.");
		}

		int position = explorationSession.getCurrentPosition();

		boolean specialArea = explorationSession.determineSpecialArea(random);

		displayCurrentExplorationArea(settings, specialArea);

		resolveCurrentExplorationEvent(settings, position, specialArea);

		advanceOrCompleteExploration();

		if (running && explorationSession.isComplete()) {
			System.out.println("\nVocê chegou ao fim da exploração de " + currentScenario.getName() + "!");
		}
	}

	private void startRandomEncounter(int position, boolean specialArea) {
		if (!(currentScenario instanceof CombatScenario combatScenario)) {
			throw new IllegalStateException("A random encounter can only occur inside a combat scenario.");
		}

		ExplorationSettings settings = combatScenario.getExplorationSettings();

		List<EnemyDefinition> selectedDefinitions = encounterGenerator.generateGroup(
				currentScenario.getId(),
				position,
				specialArea,
				settings.getMinEnemies(),
				settings.getMaxEnemies()
		);

		if (selectedDefinitions.isEmpty()) {
			System.out.println("Nenhum inimigo foi encontrado nesta região.");

			return;
		}

		startEncounter(selectedDefinitions);
	}

	private void executeExplorationEvent(ExplorationEventType eventType, int position, boolean specialArea) {
		switch (eventType) {
		case BATTLE -> startRandomEncounter(position, specialArea);

		case REWARD -> executeRewardEvent(specialArea);

		case MERCHANT -> executeMerchantEvent();

		case EPIC_ENCOUNTER -> executeEpicEncounter();

		case NOTHING -> System.out.println("Você explorou os arredores, mas não encontrou nada.");
		}
	}

	private void executeRewardEvent(boolean specialArea) {
		ExplorationReward reward = explorationRewardGenerator.generate(currentScenario.getId(), specialArea);

		switch (reward.getType()) {
		case ITEM -> {
			Item item = itemCatalog.getById(reward.getRewardId());

			party.getInventory().addItem(item, reward.getQuantity());

			System.out.println("Você encontrou " + item.getName() + " x" + reward.getQuantity() + "!");
		}

		case GOLD -> {
			party.addGold(reward.getQuantity());

			System.out.println("Você encontrou " + GameCurrency.format(reward.getQuantity()) + "!");
		}
		}
	}

	private void executeMerchantEvent() {
		explorationSession.markMerchantEncountered();

		System.out.println("Você encontrou um mercador viajante!");

		System.out.println("Ele ainda não possui mercadorias disponíveis.");
	}

	private void executeEpicEncounter() {
		if (!(currentScenario instanceof Dungeon dungeon)) {
			throw new IllegalStateException("An epic encounter can only occur inside a dungeon.");
		}

		explorationSession.markEpicEncounterOffered();

		List<EnemyDefinition> epicDefinitions = resolveEnemyDefinitions(dungeon.getDungeonSettings().getEpicEnemyIds());

		boolean accepted = epicEncounterMenu.confirmEncounter(epicDefinitions);

		if (!accepted) {
			System.out.println("Você decidiu evitar o confronto.");

			return;
		}

		startEncounter(epicDefinitions);
	}

	private List<EnemyDefinition> resolveEnemyDefinitions(List<String> enemyIds) {
		return enemyIds.stream().map(enemyCatalog::getById).toList();
	}

	private void advanceOrCompleteExploration() {
		if (!running) {
			return;
		}

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

		switch (result) {
		case VICTORY, FLED -> shouldDisplayScenarioDescription = true;

		case DEFEAT -> {
			GameOverScreen.show(party);
			running = false;
		}
		}

		return result;
	}

	private void executeCityTransportMenu() {
		List<CityTransportDefinition> availableDestinations = getAvailableCityTransportDestinations();

		if (availableDestinations.isEmpty()) {
			throw new IllegalStateException("There are no available city transport destinations.");
		}

		Optional<CityTransportDefinition> selectedDestination = cityTransportMenu
				.open(availableDestinations, getCityTransportDestinationSlotCount());

		if (selectedDestination.isEmpty()) {
			return;
		}

		CityTransportDefinition destinationDefinition = selectedDestination.get();

		Scenario destination = scenarioCatalog.getById(destinationDefinition.getDestinationScenarioId());

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
			int startingPosition = route.getDestinationExplorationPosition().orElseThrow(
					() -> new IllegalStateException(
							"Travel route to combat scenario has no destination position: " + route.getId()
					)
			);

			changeScenario(destination, startingPosition);

			return;
		}

		changeScenario(destination);
	}

	private void changeScenario(Scenario destination, int startingPosition) {
		currentScenario = destination;

		worldProgress.discover(destination.getId());

		if (destination instanceof CombatScenario combatScenario) {
			explorationSession = new ExplorationSession(combatScenario.getExplorationSettings(), startingPosition);

		} else {
			explorationSession = null;
		}

		shouldDisplayScenarioDescription = true;

		ConsoleUI.clearScreen();
	}

	private void changeScenario(Scenario destination) {
		changeScenario(destination, 1);
	}

	private void executeBossEncounter(Dungeon dungeon) {
		List<EnemyDefinition> bossDefinitions = resolveEnemyDefinitions(dungeon.getDungeonSettings().getBossEnemyIds());

		System.out.println("\n[BOSS]");

		System.out.println("\nO caminho está bloqueado por:");

		for (EnemyDefinition bossDefinition : bossDefinitions) {
			System.out.println("- " + bossDefinition.getName());
		}

		BattleResult result = startEncounter(bossDefinitions);

		if (result == BattleResult.VICTORY) {
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

		String areaMessage = specialArea ? settings.getSpecialAreaMessage() : settings.getNormalAreaMessage();

		System.out.println(areaMessage);
	}

	private void resolveCurrentExplorationEvent(ExplorationSettings settings, int position, boolean specialArea) {
		boolean epicEncounterAvailable = currentScenario instanceof Dungeon
				&& explorationSession.canOfferEpicEncounter();

		ExplorationEventType eventType = explorationEventGenerator
				.generate(settings, explorationSession.canEncounterMerchant(), epicEncounterAvailable);

		executeExplorationEvent(eventType, position, specialArea);
	}

	private void exitGame() {
		System.out.println("\nObrigado por testar a V2!");
		running = false;
	}

}
