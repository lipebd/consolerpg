package rpg.battle;

import java.util.List;
import java.util.Scanner;
import java.util.Objects;
import java.util.Optional;

import rpg.characters.Party;
import rpg.ui.EnemyTargetMenu;
import rpg.characters.Enemy;
import rpg.characters.GameCharacter;
import rpg.characters.PlayableCharacter;
import rpg.skills.Skill;
import rpg.ui.ItemMenu;

// Controls a battle between a player and an enemy group.
public class Battle {

	private Party party;
	private EnemyTargetMenu enemyTargetMenu;
	private EnemyBattleGroup enemyGroup;
	private PlayableCharacter player;
	private Scanner scanner;
	private boolean playerFled;
	private ItemMenu itemMenu;

	public Battle(Party party, EnemyBattleGroup enemyGroup, Scanner scanner) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");

		/*
		 * Temporary active character while Battle is being migrated to multiple allies.
		 */
		this.player = party.getLeader();
		this.enemyGroup = Objects.requireNonNull(enemyGroup, "Enemy battle group cannot be null.");
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
		this.enemyTargetMenu = new EnemyTargetMenu(scanner);
		this.playerFled = false;
		this.itemMenu = new ItemMenu(party, scanner);
	}

	public BattleResult start() {
		int turn = 1;

		displayBattleIntroduction();

		while (player.isAlive() && !enemyGroup.isDefeated() && !playerFled) {
			System.out.println("\n--- Turno " + turn + " ---");
			displayPlayerStatus();
			displayEnemyStatuses();

			executePlayerTurn();

			if (playerFled || enemyGroup.isDefeated()) {
				break;
			}

			executeEnemyTurns();

			turn++;
		}

		BattleResult result = determineBattleResult();

		displayBattleResult(result);

		return result;
	}

	// sobrecarga de métodos
	private void displayDamage(GameCharacter target, int damage) {
		displayDamage(target, target.getName(), damage);
	}

	private void displayDamage(GameCharacter target, String displayName, int damage) {
		System.out.println(displayName + " sofreu " + damage + " de dano. HP atual: " + target.getCurrentHp() + "/"
				+ target.getMaxHp() + ".");
	}

	private void displayPlayerStatus() {
		System.out.println("[" + player.getName().toUpperCase() + "]" + " | HP: " + player.getCurrentHp() + "/"
				+ player.getMaxHp());
	}

	private void displayEnemyStatuses() {
		for (EnemyBattleSlot slot : enemyGroup.getSlots()) {

			String displayName = slot.getDisplayName().toUpperCase();

			if (!slot.isAlive()) {
				System.out.println("[" + displayName + "]" + " | DERROTADO");

				continue;
			}

			Enemy slotEnemy = slot.getEnemy();

			System.out.println(
					"[" + displayName + "]" + " | HP: " + slotEnemy.getCurrentHp() + "/" + slotEnemy.getMaxHp());
		}
	}

	private void executeEnemyTurns() {
		for (EnemyBattleSlot slot : enemyGroup.getLivingSlots()) {

			if (!player.isAlive()) {
				break;
			}

			Enemy attackingEnemy = slot.getEnemy();

			int enemyDamage = attackingEnemy.attack(player);

			System.out.println("\n" + slot.getDisplayName() + " atacou " + player.getName() + "!");

			displayDamage(player, enemyDamage);
		}
	}

	private void executePlayerTurn() {
		boolean validAction = false;

		while (!validAction) {
			System.out.println("\n[1] Atacar | [2] Defender | [3] Especial | [4] Analisar | [5] Itens | [6] Fugir");
			System.out.print("> ");

			String choice = scanner.nextLine();

			switch (choice) {
			case "1" -> validAction = executeBasicAttack();
			case "2" -> {
				player.defend();

				System.out.println(player.getName() + " assumiu uma postura defensiva!");

				validAction = true;
			}
			case "3" -> validAction = executeSkillMenu();

			case "4" -> executeEnemyAnalysis();

			case "5" -> validAction = itemMenu.open();

			case "6" -> {
				playerFled = true;
				validAction = true;
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private boolean executeSkillMenu() {
		List<Skill> skills = player.getSkills();

		if (skills.isEmpty()) {
			System.out.println("Nenhuma habilidade disponível.");
			return false;
		}

		while (true) {
			System.out.println("\nEscolha uma habilidade:");
			System.out.println("\n[" + player.getName().toUpperCase() + "] MP: " + player.getCurrentMp() + "/"
					+ player.getMaxMp() + " | SP: " + player.getCurrentSp() + "/" + player.getMaxSp() + "\n");
			for (int index = 0; index < skills.size(); index++) {
				Skill skill = skills.get(index);

				System.out.println("[" + (index + 1) + "] " + skill.getName() + " [" + skill.getDamageType() + "]"
						+ " | Poder: " + skill.getBaseDamage() + " | MP: " + skill.getMpCost() + " | SP: "
						+ skill.getSpCost());
			}

			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine();

			if (choice.equals("0")) {
				return false;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= skills.size()) {
					System.out.println("Habilidade inválida.");
					continue;
				}

				Skill selectedSkill = skills.get(selectedIndex);

				if (!confirmSkillUse(selectedSkill)) {
					continue;
				}

				if (player.getCurrentMp() < selectedSkill.getMpCost()) {
					System.out.println("MP insuficiente!");
					continue;
				}

				if (player.getCurrentSp() < selectedSkill.getSpCost()) {
					System.out.println("SP insuficiente!");
					continue;
				}

				Optional<EnemyBattleSlot> selectedSlot = enemyTargetMenu.selectTarget(enemyGroup);

				if (selectedSlot.isEmpty()) {
					continue;
				}

				EnemyBattleSlot targetSlot = selectedSlot.get();

				Enemy target = targetSlot.getEnemy();

				if (selectedSkill.getMpCost() > 0) {
					player.spendMp(selectedSkill.getMpCost());
				}

				if (selectedSkill.getSpCost() > 0) {
					player.spendSp(selectedSkill.getSpCost());
				}

				System.out.println("\n" + player.getName() + " usou " + selectedSkill.getName() + "!");

				int damage = player.usePhysicalSkill(selectedSkill, target);

				displayDamage(target, targetSlot.getDisplayName(), damage);

				return true;

			} catch (NumberFormatException exception) {
				System.out.println("Digite o número de uma habilidade.");
			}
		}
	}

	private boolean confirmSkillUse(Skill skill) {
		System.out.println("\n[" + skill.getName().toUpperCase() + "]");

		System.out.println(skill.getDescription());

		while (true) {
			System.out.println("\n[1] Usar | [0] Voltar");
			System.out.print("> ");

			String confirmation = scanner.nextLine();

			switch (confirmation) {
			case "1" -> {
				return true;
			}

			case "0" -> {
				return false;
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private BattleResult determineBattleResult() {
		if (playerFled) {
			return BattleResult.FLED;
		}

		if (enemyGroup.isDefeated()) {
			return BattleResult.VICTORY;
		}

		return BattleResult.DEFEAT;
	}

	private void displayBattleResult(BattleResult result) {
		switch (result) {
		case FLED -> System.out.println("\n" + player.getName() + " fugiu da batalha!");

		case VICTORY -> {
			if (enemyGroup.size() == 1) {
				EnemyBattleSlot slot = enemyGroup.getSlots().get(0);

				System.out.println("\n" + slot.getDisplayName() + " foi derrotado!");
			} else {
				System.out.println("\nOs inimigos foram derrotados!");
			}

			System.out.println(player.getName() + " venceu a batalha!");
		}

		case DEFEAT -> System.out.println("\n" + player.getName() + " foi derrotado!");
		}
	}

	private void displayEnemyAnalysis(EnemyBattleSlot slot) {
		Enemy analyzedEnemy = slot.getEnemy();

		System.out.println(
				"\n[" + slot.getDisplayName().toUpperCase() + "] " + analyzedEnemy.getRank().toString().toUpperCase());

		System.out.println(analyzedEnemy.getDescription());

		System.out.println("HP: " + analyzedEnemy.getCurrentHp() + "/" + analyzedEnemy.getMaxHp());

		System.out.println("ATK: " + analyzedEnemy.getAttack());

		System.out.println("DEF: " + analyzedEnemy.getDefense());
	}

	private void displayBattleIntroduction() {
		System.out.println("\nBatalha iniciada!");

		System.out.println(player.getName() + " contra:");

		for (EnemyBattleSlot slot : enemyGroup.getSlots()) {

			System.out.println("- " + slot.getDisplayName());
		}
	}

	private boolean executeBasicAttack() {
		Optional<EnemyBattleSlot> selectedSlot = enemyTargetMenu.selectTarget(enemyGroup);

		if (selectedSlot.isEmpty()) {
			return false;
		}

		EnemyBattleSlot targetSlot = selectedSlot.get();

		Enemy target = targetSlot.getEnemy();

		System.out.println("\n" + player.getName() + " atacou " + targetSlot.getDisplayName() + "!");

		int playerDamage = player.attack(target);

		displayDamage(target, targetSlot.getDisplayName(), playerDamage);

		return true;
	}

	private void executeEnemyAnalysis() {
		Optional<EnemyBattleSlot> selectedSlot = enemyTargetMenu.selectTarget(enemyGroup);

		if (selectedSlot.isEmpty()) {
			return;
		}

		displayEnemyAnalysis(selectedSlot.get());
	}
}