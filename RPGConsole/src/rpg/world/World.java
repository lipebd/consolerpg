package rpg.world;

import java.util.List;
import java.util.Scanner;
import java.util.Random;
import java.util.LinkedHashMap;
import java.util.Map;

import rpg.battle.Battle;
import rpg.battle.BattleResult;
import rpg.characters.Player;
import rpg.characters.Enemy;
import rpg.characters.EnemyRank;
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioAction;
import rpg.world.scenarios.CombatScenario;
import rpg.world.scenarios.Bree;
import rpg.items.DropEntry;
import rpg.items.Item;
import rpg.items.ItemCatalog;
import rpg.ui.ItemMenu;
import rpg.ui.GameOverScreen;

// Controls navigation and interaction with the game world.
public class World {

	private static final int ENCOUNTER_CHANCE = 50;
	private static final int EXPLORATIONS_REQUIRED = 5;

	private Random random;
	private Player player;
	private Scenario currentScenario;
	private Scenario startingScenario;
	private Scanner scanner;
	private boolean running;
	private boolean shouldDisplayScenarioDescription;
	private ItemMenu itemMenu;
	private int explorationCount;
	private boolean breeUnlocked;

	public World(
			Player player,
			Scenario initialScenario,
			Scanner scanner
			) {
		this.player = player;
		this.currentScenario = initialScenario;
		this.startingScenario = initialScenario;
		this.scanner = scanner;
		this.running = true;
		this.random = new Random();
		this.shouldDisplayScenarioDescription = true;
		this.itemMenu = new ItemMenu(player, scanner);
		this.explorationCount = 0;
		this.breeUnlocked = false;
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
			shouldDisplayScenarioDescription = true;
		}
	}

	private void executeScenarioMenu() {
		List<ScenarioAction> actions =
				currentScenario.getAvailableActions();

		System.out.println("\nO que deseja fazer?");

		for (int index = 0; index < actions.size(); index++) {
			System.out.println(
					"[" + (index + 1) + "] "
							+ actions.get(index)
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

		case TRAVEL -> travel();

		case ITEM_SHOP -> openItemShop();

		case BLACKSMITH -> openBlacksmith();

		case TAVERN -> openTavern();

		case LEAVE_CITY -> leaveCity();
		}
	}
	
	private void openItemShop() {

		System.out.println(
				"\nA loja de itens ainda está em construção."
				);

		waitForContinue();
	}
	
	private void openBlacksmith() {

		System.out.println(
				"\nO ferreiro ainda está em construção."
				);

		waitForContinue();
	}
	
	private void openTavern() {

		System.out.println(
				"\nA taverna ainda está em construção."
				);

		waitForContinue();
	}
	
	private void leaveCity() {

		if (currentScenario instanceof Bree) {

			System.out.println(
					"\nVocê deixa Bree e retorna "
					+ "aos Verdes Ermos."
					);

			currentScenario = startingScenario;

			shouldDisplayScenarioDescription = true;
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
		explorationCount++;

		System.out.println("Explorações: " + explorationCount + "/" + EXPLORATIONS_REQUIRED);
		System.out.println(
				"\nVocê começa a explorar a região..."
				);

		if (explorationCount == EXPLORATIONS_REQUIRED && !breeUnlocked) {
			unlockBree();
		}

		int encounterRoll = random.nextInt(100) + 1;

		if (encounterRoll <= ENCOUNTER_CHANCE) {
			startLobermoEncounter();
		} else {
			System.out.println(
					"Você explorou os arredores, "
							+ "mas não encontrou nada."
					);
		}
	}

	private void startLobermoEncounter() {
		System.out.println("\nUm Lobermo apareceu!");

		Enemy lobermo = new Enemy(
				"Lobermo",
				50,
				0,
				0,
				8,
				5,
				0,
				0,
				EnemyRank.COMMON,
				List.of(
						new DropEntry(
								ItemCatalog.MERUMA,
								50,
								1,
								1
								)
						),
				"Descrição do Lobermo."
				);

		Battle battle = new Battle(
				player,
				lobermo,
				scanner
				);

		BattleResult result = battle.start();

		switch (result) {
		case VICTORY -> {
			grantBattleRewards(lobermo);
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
		while (true) {
			System.out.println("\n[1] Continuar");
			System.out.print("> ");

			String choice = scanner.nextLine();

			if (choice.equals("1")) {
				return;
			}

			System.out.println("Opção inválida.");
		}
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

	private void travel() {

		if (!breeUnlocked) {
			int remaining = EXPLORATIONS_REQUIRED - explorationCount;

			System.out.println(
					"\nVocê ainda não conhece nenhum caminho seguro para fora dos Verdes Ermos."
					);

			System.out.println(
					"Explore a região mais "+ remaining	+ " vez(es) antes de viajar.");

			waitForContinue();

			return;
		}

		System.out.println("\n[DESTINOS DISPONÍVEIS]");
		System.out.println("[1] Bree");
		System.out.println("[0] Voltar");
		System.out.print("> ");

		String choice = scanner.nextLine();

		if (choice.equals("1")) {

			System.out.println(
					"\nVocê segue pela estrada em direção à Bree..."
					);

			currentScenario = new Bree(
					player,
					scanner
					);

			shouldDisplayScenarioDescription = true;
		}
	}

	private void unlockBree() {
		breeUnlocked = true;

		System.out.println(
				"\n================================"
				);

		System.out.println(
				"NOVO DESTINO DESCOBERTO!\nBREE\nVocê conhece suficientemente os arredores dos Verdes Ermos para encontrar um caminho até Bree!"
				);


		System.out.println(
				"===================================================="
				);
	}

}