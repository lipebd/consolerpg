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
import rpg.world.encounters.EncounterCatalog;
import rpg.world.encounters.EncounterGenerator;
import rpg.world.exploration.ExplorationSettings;
import rpg.world.exploration.ExplorationSession;
import rpg.enemies.EnemyDefinition;
import rpg.items.DropEntry;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.ui.ItemMenu;
import rpg.ui.GameOverScreen;

// Controls navigation and interaction with the game world.
public class World {

	private ExplorationSession explorationSession;
	private final EncounterGenerator encounterGenerator;
	private Random random;
	private Player player;
	private Scenario currentScenario;
	private Scanner scanner;
	private boolean running;
	private boolean shouldDisplayScenarioDescription;
	private ItemMenu itemMenu;

	public World(
			Player player,
			Scenario initialScenario,
			Scanner scanner,
			EnemyCatalog enemyCatalog,
			EncounterCatalog encounterCatalog
			) {
		this.player = player;
		this.currentScenario = initialScenario;
		this.scanner = scanner;
		this.random = new Random();
		this.encounterGenerator = new EncounterGenerator(
				enemyCatalog,
				encounterCatalog,
				random
				);

		this.running = true;
		this.shouldDisplayScenarioDescription = true;
		if (
				initialScenario
				instanceof CombatScenario combatScenario
				) {
			this.explorationSession =
					new ExplorationSession(
							combatScenario.getExplorationSettings()
							);
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
		System.out.println(
				"\n[" + currentScenario.getName().toUpperCase() + "]"
				);

		if (shouldDisplayScenarioDescription) {
			System.out.println(currentScenario.getDescription());
			shouldDisplayScenarioDescription = false;
		}
	}

	private void executeScenarioMenu() {
		List<ScenarioAction> actions =
				currentScenario.getAvailableActions();

		System.out.println("\nO que deseja fazer?");

		for (int index = 0; index < actions.size(); index++) {
			System.out.print(
					"[" + (index + 1) + "] "
							+ actions.get(index) + " | "
					);
		}

		System.out.println("[0] Encerrar jogo");
		System.out.print("> ");

		String choice = scanner.nextLine();

		if (choice.equals("0")) {
			System.out.println(
					"\nObrigado por testar a V1!"
					);

			running = false;
			return;
		}

		try {
			int selectedIndex = Integer.parseInt(choice) - 1;

			if (
					selectedIndex < 0
					|| selectedIndex >= actions.size()
					) {
				System.out.println("Opção inválida.");
				return;
			}

			ScenarioAction selectedAction =
					actions.get(selectedIndex);

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

		case TRAVEL -> System.out.println(
				"\nNão há outros destinos disponíveis."
				);
		}
	}

	private void displayPlayerStatus() {
		System.out.println(
				"\n[" + player.getName().toUpperCase() + "]"
				);

		System.out.println(
				"Nível: " + player.getLevel()
				+ " | HP: " + player.getCurrentHp()
				+ "/" + player.getMaxHp()
				+ " | MP: " + player.getCurrentMp()
				+ "/" + player.getMaxMp()
				+ " | SP: " + player.getCurrentSp()
				+ "/" + player.getMaxSp()
				);
	}

	private void explore() {
		if (
				!(currentScenario instanceof CombatScenario combatScenario)
				) {
			System.out.println(
					"\nEste cenário não pode ser explorado."
					);

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

		System.out.println(
				"\n[EXPLORAÇÃO "
						+ position + "/"
						+ settings.getLength()
						+ "]"
				);

		if (specialArea) {
		    System.out.println(
		            settings.getSpecialAreaMessage()
		    );
		} else {
		    System.out.println(
		            settings.getNormalAreaMessage()
		    );
		}

		int encounterRoll = random.nextInt(100) + 1;

		if (
				encounterRoll
				<= settings.getEncounterChance()
				) {
			startRandomEncounter(
					position,
					specialArea
					);
		} else {
			System.out.println(
					"Você explorou os arredores, "
							+ "mas não encontrou nada."
					);
		}

		if (
				running
				&& explorationSession.isComplete()
				) {
			System.out.println(
					"\nVocê chegou ao fim da exploração "
							+ "de " + currentScenario.getName() + "!"
					);
		}
	}

	private void startRandomEncounter(
			int position,
			boolean specialArea
			) {
		Optional<EnemyDefinition> selectedDefinition =
				encounterGenerator.generate(
						currentScenario.getId(),
						position,
						specialArea
						);

		if (selectedDefinition.isEmpty()) {
			System.out.println(
					"Nenhum inimigo foi encontrado nesta região."
					);
			return;
		}

		EnemyDefinition enemyDefinition =
				selectedDefinition.get();

		Enemy enemy =
				createEnemyFromDefinition(enemyDefinition);

		System.out.println(
				"\nUm " + enemy.getName() + " apareceu!"
				);

		Battle battle = new Battle(
				player,
				enemy,
				scanner
				);

		BattleResult result = battle.start();

		switch (result) {
		case VICTORY -> {
			grantBattleRewards(enemy);
			shouldDisplayScenarioDescription = true;
		}

		case FLED ->
		shouldDisplayScenarioDescription = true;

		case DEFEAT -> {
			GameOverScreen.show(player);
			running = false;
		}
		}
	}

	private Enemy createEnemyFromDefinition(
			EnemyDefinition enemyDefinition
			) {
		if (enemyDefinition.getId().equals("lobermo")) {
			return enemyDefinition.createEnemy(
					List.of(
							new DropEntry(
									ItemCatalog.MERUMA,
									50,
									1,
									1
									)
							)
					);
		}

		return enemyDefinition.createEnemy();
	}

	private void grantBattleRewards(Enemy defeatedEnemy) {
		if (!(currentScenario instanceof CombatScenario combatScenario)) {
			return;
		}

		double rankMultiplier =
				defeatedEnemy.getRank().getRewardMultiplier();

		int experienceReward = (int) Math.round(
				combatScenario.getBaseExperienceReward()
				* rankMultiplier
				);

		int goldReward = (int) Math.round(
				combatScenario.getBaseGoldReward()
				* rankMultiplier
				);

		int levelsGained =
				player.gainExperience(experienceReward);

		player.addGold(goldReward);

		Map<Item, Integer> collectedItems =
				collectEnemyDrops(defeatedEnemy);

		displayBattleRewards(
				experienceReward,
				goldReward,
				levelsGained,
				collectedItems
				);
	}

	private void displayBattleRewards(
			int experienceReward,
			int goldReward,
			int levelsGained,
			Map<Item, Integer> collectedItems
			) {
		System.out.println("\n[RESULTADO DA BATALHA]");

		System.out.println(
				"EXP recebida: +" + experienceReward
				);

		System.out.println(
				"Ouro recebido: +" + goldReward
				);

		if (levelsGained > 0) {
			int previousLevel =
					player.getLevel() - levelsGained;

			String levelWord =
					levelsGained == 1 ? "nível" : "níveis";

			System.out.println(
					player.getName() + " subiu "
							+ levelsGained + " " + levelWord + "! ["
							+ previousLevel + " -> "
							+ player.getLevel() + "]"
					);
		}

		System.out.println("\nItens encontrados:");

		if (collectedItems.isEmpty()) {
			System.out.println("- Nenhum");
		} else {
			for (Map.Entry<Item, Integer> entry
					: collectedItems.entrySet()) {

				Item item = entry.getKey();
				int quantity = entry.getValue();

				System.out.println(
						"- " + item.getName()
						+ " x" + quantity
						);
			}
		}

		waitForContinue();
	}

	private void waitForContinue() {
		System.out.println("\nPressione ENTER para continuar...");
		System.out.print("> ");
		scanner.nextLine();
	}


	private Map<Item, Integer> collectEnemyDrops(
			Enemy defeatedEnemy
			) {
		Map<Item, Integer> collectedItems =
				new LinkedHashMap<>();

		for (DropEntry dropEntry
				: defeatedEnemy.getDropTable()) {

			int quantity = dropEntry.rollQuantity(random);

			if (quantity > 0) {
				Item item = dropEntry.getItem();

				player.getInventory().addItem(
						item,
						quantity
						);

				int currentQuantity =
						collectedItems.getOrDefault(item, 0);

				collectedItems.put(
						item,
						currentQuantity + quantity
						);
			}
		}

		return collectedItems;
	}

}