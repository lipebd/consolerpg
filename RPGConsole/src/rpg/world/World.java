package rpg.world;

import java.util.List;
import java.util.Scanner;
import java.util.Random;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import rpg.battle.Battle;
import rpg.battle.BattleResult;
import rpg.characters.Player;
import rpg.characters.Enemy;
import rpg.enemies.EnemyCatalog;
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioAction;
import rpg.world.scenarios.CombatScenario;
import rpg.world.scenarios.Dungeon;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.travel.TravelCatalog;
import rpg.world.encounters.EncounterCatalog;
import rpg.world.encounters.EncounterGenerator;
import rpg.world.exploration.ExplorationSettings;
import rpg.world.exploration.ExplorationSession;
import rpg.world.exploration.ExplorationEventGenerator;
import rpg.world.exploration.ExplorationEventType;
import rpg.enemies.EnemyDefinition;
import rpg.items.DropEntry;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.ui.ItemMenu;
import rpg.ui.GameOverScreen;
import rpg.ui.ConsoleUI;

// Controls navigation and interaction with the game world.
public class World {

	private final TravelCatalog travelCatalog;
	private final ScenarioCatalog scenarioCatalog;
	private final EnemyCatalog enemyCatalog;
	private final ExplorationEventGenerator explorationEventGenerator;
	private ExplorationSession explorationSession;
	private final EncounterGenerator encounterGenerator;
	private Random random;
	private Player player;
	private Scenario currentScenario;
	private Scanner scanner;
	private boolean running;
	private boolean shouldDisplayScenarioDescription;
	private ItemMenu itemMenu;

	public World(Player player, Scenario initialScenario, Scanner scanner, EnemyCatalog enemyCatalog,
			EncounterCatalog encounterCatalog, ScenarioCatalog scenarioCatalog, TravelCatalog travelCatalog) {
		this.player = player;
		this.currentScenario = initialScenario;
		this.scanner = scanner;
		this.random = new Random();
		this.enemyCatalog = enemyCatalog;
		this.encounterGenerator = new EncounterGenerator(enemyCatalog, encounterCatalog, random);
		this.explorationEventGenerator = new ExplorationEventGenerator(random);
		this.scenarioCatalog = scenarioCatalog;
		this.travelCatalog = travelCatalog;

		this.running = true;
		this.shouldDisplayScenarioDescription = true;
		if (initialScenario instanceof CombatScenario combatScenario) {
			this.explorationSession = new ExplorationSession(combatScenario.getExplorationSettings());
		}
		this.itemMenu = new ItemMenu(player, scanner);
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
		List<ScenarioAction> actions = currentScenario.getAvailableActions();

		System.out.println("\nO que deseja fazer?");

		for (int index = 0; index < actions.size(); index++) {
			System.out.print("[" + (index + 1) + "] " + actions.get(index) + " | ");
		}

		System.out.println("[0] Encerrar jogo");
		System.out.print("> ");

		String choice = scanner.nextLine();

		if (choice.equals("0")) {
			System.out.println("\nObrigado por testar a V1!");

			running = false;
			return;
		}

		try {
			int selectedIndex = Integer.parseInt(choice) - 1;

			if (selectedIndex < 0 || selectedIndex >= actions.size()) {
				System.out.println("Opção inválida.");
				return;
			}

			ScenarioAction selectedAction = actions.get(selectedIndex);

			executeScenarioAction(selectedAction);

		} catch (NumberFormatException exception) {
			System.out.println("Digite o número de uma ação.");
		}
	}

	private void executeScenarioAction(ScenarioAction action) {
		switch (action) {
		case EXPLORE -> explore();

		case VIEW_STATUS -> displayPlayerStatus();

		case INVENTORY -> itemMenu.open();

		case TRAVEL -> executeTravelMenu();
		}
	}

	private void displayPlayerStatus() {
		System.out.println("\n[" + player.getName().toUpperCase() + "]");

		System.out.println("Nível: " + player.getLevel() + " | HP: " + player.getCurrentHp() + "/" + player.getMaxHp()
				+ " | MP: " + player.getCurrentMp() + "/" + player.getMaxMp() + " | SP: " + player.getCurrentSp() + "/"
				+ player.getMaxSp());
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

		int encounterRoll = random.nextInt(100) + 1;

		boolean epicEncounterAvailable = currentScenario instanceof Dungeon
				&& explorationSession.canOfferEpicEncounter();

		if (
		        currentScenario instanceof Dungeon dungeon
		        && position == settings.getLength()
		) {
		    executeBossEncounter(dungeon);
		} else {
		    ExplorationEventType eventType =
		            explorationEventGenerator.generate(
		                    settings,
		                    explorationSession.canEncounterMerchant(),
		                    epicEncounterAvailable
		            );

		    executeExplorationEvent(
		            eventType,
		            position,
		            specialArea
		    );
		}

		if (running && explorationSession.isComplete()) {
			System.out.println("\nVocê chegou ao fim da exploração " + "de " + currentScenario.getName() + "!");
		}
	}

	private void startRandomEncounter(int position, boolean specialArea) {
		Optional<EnemyDefinition> selectedDefinition = encounterGenerator.generate(currentScenario.getId(), position,
				specialArea);

		if (selectedDefinition.isEmpty()) {
			System.out.println("Nenhum inimigo foi encontrado nesta região.");

			return;
		}

		startEncounter(selectedDefinition.get());
	}

	private Enemy createEnemyFromDefinition(EnemyDefinition enemyDefinition) {
		if (enemyDefinition.getId().equals("lobermo")) {
			return enemyDefinition.createEnemy(List.of(new DropEntry(ItemCatalog.MERUMA, 50, 1, 1)));
		}

		return enemyDefinition.createEnemy();
	}

	private void grantBattleRewards(Enemy defeatedEnemy) {
		if (!(currentScenario instanceof CombatScenario combatScenario)) {
			return;
		}

		double rankMultiplier = defeatedEnemy.getRank().getRewardMultiplier();

		int experienceReward = (int) Math.round(combatScenario.getBaseExperienceReward() * rankMultiplier);

		int goldReward = (int) Math.round(combatScenario.getBaseGoldReward() * rankMultiplier);

		int levelsGained = player.gainExperience(experienceReward);

		player.addGold(goldReward);

		Map<Item, Integer> collectedItems = collectEnemyDrops(defeatedEnemy);

		displayBattleRewards(experienceReward, goldReward, levelsGained, collectedItems);
	}

	private void displayBattleRewards(int experienceReward, int goldReward, int levelsGained,
			Map<Item, Integer> collectedItems) {
		System.out.println("\n[RESULTADO DA BATALHA]");

		System.out.println("EXP recebida: +" + experienceReward);

		System.out.println("Ouro recebido: +" + goldReward);

		if (levelsGained > 0) {
			int previousLevel = player.getLevel() - levelsGained;

			String levelWord = levelsGained == 1 ? "nível" : "níveis";

			System.out.println(player.getName() + " subiu " + levelsGained + " " + levelWord + "! [" + previousLevel
					+ " -> " + player.getLevel() + "]");
		}

		System.out.println("\nItens encontrados:");

		if (collectedItems.isEmpty()) {
			System.out.println("- Nenhum");
		} else {
			for (Map.Entry<Item, Integer> entry : collectedItems.entrySet()) {

				Item item = entry.getKey();
				int quantity = entry.getValue();

				System.out.println("- " + item.getName() + " x" + quantity);
			}
		}

		waitForContinue();
	}

	private void waitForContinue() {
		System.out.println("\nPressione ENTER para continuar...");
		System.out.print("> ");
		scanner.nextLine();
	}

	private Map<Item, Integer> collectEnemyDrops(Enemy defeatedEnemy) {
		Map<Item, Integer> collectedItems = new LinkedHashMap<>();

		for (DropEntry dropEntry : defeatedEnemy.getDropTable()) {

			int quantity = dropEntry.rollQuantity(random);

			if (quantity > 0) {
				Item item = dropEntry.getItem();

				player.getInventory().addItem(item, quantity);

				int currentQuantity = collectedItems.getOrDefault(item, 0);

				collectedItems.put(item, currentQuantity + quantity);
			}
		}

		return collectedItems;
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

		player.getInventory().addItem(item, 1);

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

		String epicEnemyId = dungeon.getDungeonSettings().getEpicEnemyId();

		EnemyDefinition epicDefinition = enemyCatalog.getById(epicEnemyId);

		System.out.println("\n[ENCONTRO ÉPICO]");

		System.out.println("Você encontrou " + epicDefinition.getName() + "!");

		System.out.println(epicDefinition.getDescription());

		while (true) {
			System.out.println("\n[1] Enfrentar | [0] Evitar confronto");

			System.out.print("> ");

			String choice = scanner.nextLine();

			switch (choice) {
			case "1" -> {
				startEncounter(epicDefinition);
				return;
			}

			case "0" -> {
				System.out.println("Você decidiu evitar o confronto.");

				return;
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private BattleResult startEncounter(EnemyDefinition enemyDefinition) {
		Enemy enemy = createEnemyFromDefinition(enemyDefinition);

		System.out.println("\nUm " + enemy.getName() + " apareceu!");

		Battle battle = new Battle(player, enemy, scanner);

		BattleResult result = battle.start();

		switch (result) {
		case VICTORY -> {
			grantBattleRewards(enemy);
			shouldDisplayScenarioDescription = true;
		}

		case FLED -> shouldDisplayScenarioDescription = true;

		case DEFEAT -> {
			GameOverScreen.show(player);
			running = false;
		}
		}
		return result;
	}

	private void executeTravelMenu() {
		if (currentScenario instanceof CombatScenario
				&& (explorationSession == null || !explorationSession.isComplete())) {
			System.out.println("\nVocê precisa concluir a exploração " + "antes de viajar.");

			return;
		}

		List<String> destinationIds = travelCatalog.getDestinationIds(currentScenario.getId());

		if (destinationIds.isEmpty()) {
			System.out.println("\nNão há destinos disponíveis.");

			return;
		}

		while (true) {
			System.out.println("\nEscolha seu destino:");

			for (int index = 0; index < destinationIds.size(); index++) {
				String destinationId = destinationIds.get(index);

				Scenario destination = scenarioCatalog.getById(destinationId);

				System.out.println("[" + (index + 1) + "] " + destination.getName());
			}

			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine();

			if (choice.equals("0")) {
				return;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= destinationIds.size()) {
					System.out.println("Destino inválido.");

					continue;
				}

				String destinationId = destinationIds.get(selectedIndex);

				Scenario destination = scenarioCatalog.getById(destinationId);

				changeScenario(destination);
				return;

			} catch (NumberFormatException exception) {
				System.out.println("Digite o número de um destino.");
			}
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
	
	private void executeBossEncounter(
	        Dungeon dungeon
	) {
	    String bossEnemyId =
	            dungeon.getDungeonSettings()
	                    .getBossEnemyId();

	    EnemyDefinition bossDefinition =
	            enemyCatalog.getById(bossEnemyId);

	    System.out.println("\n[BOSS]");

	    System.out.println(
	            bossDefinition.getName()
	            + " bloqueia o fim da masmorra!"
	    );

	    BattleResult result =
	            startEncounter(bossDefinition);

	    if (result == BattleResult.FLED) {
	        explorationSession
	                .returnToPreviousPosition();

	        System.out.println(
	                "\nVocê recuou para a área anterior."
	        );
	    }
	}

}