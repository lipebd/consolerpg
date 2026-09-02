package rpg.world;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.Objects;

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
import rpg.ui.ConsoleUI;
import rpg.ui.EpicEncounterMenu;
import rpg.ui.GameOverScreen;
import rpg.ui.ItemMenu;
import rpg.ui.PartyStatusMenu;
import rpg.ui.ScenarioActionMenu;
import rpg.ui.TravelMenu;
import rpg.world.encounters.EncounterCatalog;
import rpg.world.encounters.EncounterGenerator;
import rpg.world.exploration.ExplorationEventGenerator;
import rpg.world.exploration.ExplorationEventType;
import rpg.world.exploration.ExplorationSession;
import rpg.world.exploration.ExplorationSettings;
import rpg.world.scenarios.CombatScenario;
import rpg.world.scenarios.Dungeon;
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioAction;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.travel.TravelProgress;

// Controls navigation and interaction with the game world.
public class World {

	private final PartyStatusMenu partyStatusMenu;
	private Party party;
	private final ScenarioActionMenu scenarioActionMenu;
	private final BattleCoordinator battleCoordinator;
	private final EpicEncounterMenu epicEncounterMenu;
	private final EnemyCatalog enemyCatalog;
	private final ExplorationEventGenerator explorationEventGenerator;
	private ExplorationSession explorationSession;
	private final EncounterGenerator encounterGenerator;
	private Random random;
	private Scenario currentScenario;
	private boolean running;
	private boolean shouldDisplayScenarioDescription;
	private boolean scenarioNameAlreadyDisplayed;
	private ItemMenu itemMenu;
	private final TravelMenu travelMenu;
	private final TravelProgress travelProgress;

	public World(Party party, Scenario initialScenario, Scanner scanner, EnemyCatalog enemyCatalog,
			EnemyDropCatalog enemyDropCatalog, EncounterCatalog encounterCatalog, ScenarioCatalog scenarioCatalog,
			TravelProgress travelProgress) {
		this.party = party;
		this.currentScenario = initialScenario;
		this.random = new Random();
		BattleRewardService rewardService = new BattleRewardService(random);
		BattleRewardScreen rewardScreen = new BattleRewardScreen(scanner);
		this.battleCoordinator = new BattleCoordinator(party, scanner, enemyDropCatalog, rewardService, rewardScreen,
				random);
		this.enemyCatalog = enemyCatalog;
		this.encounterGenerator = new EncounterGenerator(enemyCatalog, encounterCatalog, random);
		this.explorationEventGenerator = new ExplorationEventGenerator(random);
		this.travelProgress = Objects.requireNonNull(travelProgress, "Travel progress cannot be null.");
		this.travelMenu = new TravelMenu(scanner, scenarioCatalog, this.travelProgress);
		this.epicEncounterMenu = new EpicEncounterMenu(scanner);
		this.scenarioActionMenu = new ScenarioActionMenu(scanner);
		this.running = true;
		this.shouldDisplayScenarioDescription = true;
		if (initialScenario instanceof CombatScenario combatScenario) {
			this.explorationSession = new ExplorationSession(combatScenario.getExplorationSettings());
		}
		this.itemMenu = new ItemMenu(party, scanner);
		this.partyStatusMenu = new PartyStatusMenu(party, scanner);
		this.scenarioNameAlreadyDisplayed = false;
	}

	public void start() {
		while (running) {
			displayCurrentScenario();
			executeScenarioMenu();
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

		System.out.println("\n[EXPLORAÇÃO " + explorationSession.getCurrentPosition() + "/"
				+ explorationSession.getSettings().getLength() + "]");

		if (currentScenario instanceof Dungeon && explorationSession.isAtFinalPosition()) {
			displayDungeonBossObstacle();
		}
	}

	private void executeScenarioMenu() {
		List<ScenarioAction> availableActions = getAvailableScenarioActions();

		Optional<ScenarioAction> selectedAction = scenarioActionMenu.selectAction(availableActions);

		if (selectedAction.isEmpty()) {
			System.out.println("\nObrigado por testar a V2!");

			running = false;
			return;
		}

		executeScenarioAction(selectedAction.get());
	}

	private List<ScenarioAction> getAvailableScenarioActions() {
		boolean travelAvailable = travelProgress.hasUnlockedRoutesFrom(currentScenario.getId());

		if (!(currentScenario instanceof CombatScenario)) {
			if (travelAvailable) {
				return List.of(ScenarioAction.INVENTORY, ScenarioAction.VIEW_STATUS, ScenarioAction.TRAVEL);
			}

			return List.of(ScenarioAction.INVENTORY, ScenarioAction.VIEW_STATUS);
		}

		if (explorationSession != null && explorationSession.isComplete()) {
			if (travelAvailable) {
				return List.of(ScenarioAction.TRAVEL, ScenarioAction.INVENTORY, ScenarioAction.VIEW_STATUS);
			}

			return List.of(ScenarioAction.INVENTORY, ScenarioAction.VIEW_STATUS);
		}

		if (currentScenario instanceof Dungeon && explorationSession != null
				&& explorationSession.isAtFinalPosition()) {
			return List.of(ScenarioAction.FACE_BOSS, ScenarioAction.INVENTORY, ScenarioAction.VIEW_STATUS);
		}

		return List.of(ScenarioAction.EXPLORE, ScenarioAction.INVENTORY, ScenarioAction.VIEW_STATUS);
	}

	private void executeScenarioAction(ScenarioAction action) {
		switch (action) {
		case EXPLORE -> explore();

		case FACE_BOSS -> faceBoss();

		case VIEW_STATUS -> partyStatusMenu.open();

		case INVENTORY -> itemMenu.open();

		case TRAVEL -> executeTravelMenu();
		}
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
			throw new IllegalStateException("A random encounter can only occur " + "inside a combat scenario.");
		}

		ExplorationSettings settings = combatScenario.getExplorationSettings();

		List<EnemyDefinition> selectedDefinitions = encounterGenerator.generateGroup(currentScenario.getId(), position,
				specialArea, settings.getMinEnemies(), settings.getMaxEnemies());

		if (selectedDefinitions.isEmpty()) {
			System.out.println("Nenhum inimigo foi encontrado nesta região.");

			return;
		}

		startEncounter(selectedDefinitions);
	}

	private void executeExplorationEvent(ExplorationEventType eventType, int position, boolean specialArea) {
		switch (eventType) {
		case BATTLE -> startRandomEncounter(position, specialArea);

		case ITEM -> executeItemEvent();

		case MERCHANT -> executeMerchantEvent();

		case EPIC_ENCOUNTER -> executeEpicEncounter();

		case NOTHING -> System.out.println("Você explorou os arredores, " + "mas não encontrou nada.");
		}
	}

	private void executeItemEvent() {
		Item item = ItemCatalog.MERUMA;

		party.getInventory().addItem(item, 1);

		System.out.println("Você encontrou " + item.getName() + " x1!");
	}

	private void executeMerchantEvent() {
		explorationSession.markMerchantEncountered();

		System.out.println("Você encontrou um mercador viajante!");

		System.out.println("Ele ainda não possui mercadorias disponíveis.");
	}

	private void executeEpicEncounter() {
		if (!(currentScenario instanceof Dungeon dungeon)) {
			throw new IllegalStateException("An epic encounter can only occur " + "inside a dungeon.");
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
			explorationSession.markComplete();
		} else {
			explorationSession.advance();
		}
	}

	private BattleResult startEncounter(List<EnemyDefinition> enemyDefinitions) {
		if (!(currentScenario instanceof CombatScenario combatScenario)) {
			throw new IllegalStateException("A battle can only start " + "inside a combat scenario.");
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

	private void executeTravelMenu() {
		if (currentScenario instanceof CombatScenario
				&& (explorationSession == null || !explorationSession.isComplete())) {
			System.out.println("\nVocê precisa concluir a exploração antes de viajar.");

			return;
		}

		Optional<Scenario> selectedScenario = travelMenu.open(currentScenario);

		if (selectedScenario.isPresent()) {
			changeScenario(selectedScenario.get());
		}
	}

	private void changeScenario(Scenario destination) {
		currentScenario = destination;

		if (destination instanceof CombatScenario combatScenario) {
			explorationSession = new ExplorationSession(combatScenario.getExplorationSettings());
		} else {
			explorationSession = null;
		}

		shouldDisplayScenarioDescription = true;

		ConsoleUI.clearScreen();
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
			explorationSession.markComplete();

		} else if (result == BattleResult.FLED) {
			System.out.println("\nVocê permanece diante " + "do caminho bloqueado.");
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

		ExplorationEventType eventType = explorationEventGenerator.generate(settings,
				explorationSession.canEncounterMerchant(), epicEncounterAvailable);

		executeExplorationEvent(eventType, position, specialArea);
	}

}