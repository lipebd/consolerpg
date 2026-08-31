package rpg.world;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

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
import rpg.world.travel.TravelCatalog;

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
	private ItemMenu itemMenu;
	private final TravelMenu travelMenu;

	public World(Party party, Scenario initialScenario, Scanner scanner, EnemyCatalog enemyCatalog,
			EnemyDropCatalog enemyDropCatalog, EncounterCatalog encounterCatalog, ScenarioCatalog scenarioCatalog,
			TravelCatalog travelCatalog) {
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
		this.travelMenu = new TravelMenu(scanner, scenarioCatalog, travelCatalog);
		this.epicEncounterMenu = new EpicEncounterMenu(scanner);
		this.scenarioActionMenu = new ScenarioActionMenu(scanner);
		this.running = true;
		this.shouldDisplayScenarioDescription = true;
		if (initialScenario instanceof CombatScenario combatScenario) {
			this.explorationSession = new ExplorationSession(combatScenario.getExplorationSettings());
		}
		this.itemMenu = new ItemMenu(party, scanner);
		this.partyStatusMenu = new PartyStatusMenu(party, scanner);
	}

	public void start() {
		while (running) {
			displayCurrentScenario();
			executeScenarioMenu();
		}
	}

	private void displayCurrentScenario() {
		System.out.println("\n[" + currentScenario.getName().toUpperCase() + "]");

		if (shouldDisplayScenarioDescription) {
			System.out.println(currentScenario.getDescription());
			shouldDisplayScenarioDescription = false;
		}
	}

	private void executeScenarioMenu() {
		Optional<ScenarioAction> selectedAction = scenarioActionMenu.selectAction(currentScenario);

		if (selectedAction.isEmpty()) {
			System.out.println("\nObrigado por testar a V2!");

			running = false;
			return;
		}

		executeScenarioAction(selectedAction.get());
	}

	private void executeScenarioAction(ScenarioAction action) {
		switch (action) {
		case EXPLORE -> explore();

		case VIEW_STATUS -> partyStatusMenu.open();

		case INVENTORY -> itemMenu.open();

		case TRAVEL -> executeTravelMenu();
		}
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

		int position = explorationSession.advance();

		boolean specialArea = explorationSession.determineSpecialArea(random);

		System.out.println("\n[EXPLORAÇÃO " + position + "/" + settings.getLength() + "]");

		if (specialArea) {
			System.out.println(settings.getSpecialAreaMessage());
		} else {
			System.out.println(settings.getNormalAreaMessage());
		}

		boolean epicEncounterAvailable = currentScenario instanceof Dungeon
				&& explorationSession.canOfferEpicEncounter();

		if (currentScenario instanceof Dungeon dungeon && position == settings.getLength()) {
			executeBossEncounter(dungeon);
		} else {
			ExplorationEventType eventType = explorationEventGenerator.generate(settings,
					explorationSession.canEncounterMerchant(), epicEncounterAvailable);

			executeExplorationEvent(eventType, position, specialArea);
		}

		if (running && explorationSession.isComplete()) {
			System.out.println("\nVocê chegou ao fim da exploração " + "de " + currentScenario.getName() + "!");
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
			throw new IllegalStateException("An epic encounter can only occur inside a dungeon.");
		}

		explorationSession.markEpicEncounterOffered();

		String epicEnemyId = dungeon.getDungeonSettings().getEpicEnemyId();

		EnemyDefinition epicDefinition = enemyCatalog.getById(epicEnemyId);

		boolean accepted = epicEncounterMenu.confirmEncounter(epicDefinition);

		if (!accepted) {
			System.out.println("Você decidiu evitar o confronto.");

			return;
		}

		startEncounter(epicDefinition);
	}

	// sobrecarga de métodos
	private BattleResult startEncounter(EnemyDefinition enemyDefinition) {
		return startEncounter(List.of(enemyDefinition));
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
		String bossEnemyId = dungeon.getDungeonSettings().getBossEnemyId();

		EnemyDefinition bossDefinition = enemyCatalog.getById(bossEnemyId);

		System.out.println("\n[BOSS]");

		System.out.println(bossDefinition.getName() + " bloqueia o fim da masmorra!");

		BattleResult result = startEncounter(bossDefinition);

		if (result == BattleResult.FLED) {
			explorationSession.returnToPreviousPosition();

			System.out.println("\nVocê recuou para a área anterior.");
		}
	}

}