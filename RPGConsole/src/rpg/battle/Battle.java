package rpg.battle;

import java.util.Scanner;
import java.util.List;
import rpg.skills.Skill;
import rpg.characters.Enemy;
import rpg.characters.Player;
import rpg.characters.GameCharacter;
import rpg.ui.ItemMenu;

// Controls a battle between a player and an enemy.
public class Battle {

	private Player player;
	private Enemy enemy;
	private Scanner scanner;
	private boolean playerFled;
	private ItemMenu itemMenu;

	public Battle(Player player, Enemy enemy, Scanner scanner) {
		this.player = player;
		this.enemy = enemy;
		this.playerFled = false;
		this.scanner = scanner;
		this.itemMenu = new ItemMenu(player, scanner);
	}

	public BattleResult start() {
		int turn = 1;

		System.out.println("\nBatalha iniciada!");
		System.out.println(player.getName() + " contra " + enemy.getName());

		while (
				player.isAlive()
				&& enemy.isAlive()
				&& !playerFled
				) {
			System.out.println("\n--- Turno " + turn + " ---");
			displayPlayerStatus();
			displayEnemyStatus();

			executePlayerTurn();

			if (playerFled || !enemy.isAlive()) {
			    break;
			}

			executeEnemyTurn();

			turn++;
		}

		BattleResult result = determineBattleResult();

		displayBattleResult(result);

		return result;
	}

	private void displayDamage(GameCharacter target, int damage) {
		System.out.println(
				target.getName() + " sofreu "
						+ damage + " de dano. HP atual: "
						+ target.getCurrentHp() +  "/"
						+ target.getMaxHp() + ".");
	}

	private void displayPlayerStatus() {
		System.out.println(
				"[" + player.getName().toUpperCase() + "]"
				+ " | HP: " + player.getCurrentHp()
				+ "/" + player.getMaxHp()
				);
	}
	
	private void displayEnemyStatus() {
		System.out.println(
				"[" + enemy.getName().toUpperCase() + "]"
				+ " | HP: " + enemy.getCurrentHp()
				+ "/" + enemy.getMaxHp()
				);
	}

	private void executeEnemyTurn() {
		int enemyDamage = enemy.attack(player);

		System.out.println(
				"\n" + enemy.getName()
				+ " atacou " + player.getName() + "!"
				);

		displayDamage(player, enemyDamage);
	}

	private void executePlayerTurn() {
		boolean validAction = false;

		while (!validAction) {
			System.out.println("\n[1] Atacar | [2] Defender | [3] Especial | [4] Analisar | [5] Itens | [6] Fugir");
			System.out.print("> ");

			String choice = scanner.nextLine();

			switch (choice) {
			case "1" -> {
			    System.out.println(
			            "\n" + player.getName()
			            + " atacou " + enemy.getName() + "!"
			    );

			    int playerDamage = player.attack(enemy);
			    displayDamage(enemy, playerDamage);

			    validAction = true;
			}
			case "2" -> {
				player.defend();

				System.out.println(
						player.getName()
						+ " assumiu uma postura defensiva!"
						);

				validAction = true;
			}
			case "3" -> validAction = executeSkillMenu();
			
			case "4" -> displayEnemyAnalysis();
			
			case "5" -> validAction = itemMenu.open();

			case "6" -> {
			    playerFled = true;
			    validAction = true;
			}

			default -> System.out.println(
					"Opção inválida."
					);
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
			System.out.println("\n["+ player.getName().toUpperCase() +"] MP: "+ player.getCurrentMp()
			+ "/" + player.getMaxMp()
			+ " | SP: " + player.getCurrentSp()
			+ "/" + player.getMaxSp()+"\n");
			for (int index = 0; index < skills.size(); index++) {
				Skill skill = skills.get(index);

				System.out.println(
						"[" + (index + 1) + "] "
								+ skill.getName() + " ["
								+ skill.getDamageType() + "]"
								+ " | Poder: " + skill.getBaseDamage()
								+ " | MP: " + skill.getMpCost()
								+ " | SP: " + skill.getSpCost()
						);
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

				if (selectedSkill.getMpCost() > 0) {
					player.spendMp(selectedSkill.getMpCost());
				}

				if (selectedSkill.getSpCost() > 0) {
					player.spendSp(selectedSkill.getSpCost());
				}

				System.out.println(
						"\n" + player.getName()
						+ " usou " + selectedSkill.getName() + "!"
						);

				int damage =
						player.usePhysicalSkill(selectedSkill, enemy);

				displayDamage(enemy, damage);

				return true;

			} catch (NumberFormatException exception) {
				System.out.println(
						"Digite o número de uma habilidade."
						);
			}
		}
	}

	private boolean confirmSkillUse(Skill skill) {
		System.out.println(
				"\n[" + skill.getName().toUpperCase() + "]"
				);

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

			default -> System.out.println(
					"Opção inválida."
					);
			}
		}
	}
	
	private BattleResult determineBattleResult() {
	    if (playerFled) {
	        return BattleResult.FLED;
	    }

	    if (player.isAlive()) {
	        return BattleResult.VICTORY;
	    }

	    return BattleResult.DEFEAT;
	}
	
	private void displayBattleResult(BattleResult result) {
	    switch (result) {
	        case FLED -> System.out.println(
	                "\n" + player.getName() + " fugiu da batalha!"
	        );

	        case VICTORY -> {
	            System.out.println(
	                    "\n" + enemy.getName() + " foi derrotado!"
	            );

	            System.out.println(
	                    player.getName() + " venceu a batalha!"
	            );
	        }

	        case DEFEAT -> System.out.println(
	                "\n" + player.getName() + " foi derrotado!"
	        );
	    }
	}
	
	private void displayEnemyAnalysis() {
	    System.out.println(
	            "\n[" + enemy.getName().toUpperCase() + "] "
	    + enemy.getRank().toString().toUpperCase());
	    System.out.println(enemy.getDescription());

	    System.out.println(
	            "HP: " + enemy.getCurrentHp()
	            + "/" + enemy.getMaxHp()
	    );

	    System.out.println("ATK: " + enemy.getAttack());
	    System.out.println("DEF: " + enemy.getDefense());
	}
	
}