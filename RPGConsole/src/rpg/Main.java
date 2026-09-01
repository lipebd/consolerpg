package rpg;

import rpg.world.World;
import rpg.world.scenarios.Field;
import rpg.world.scenarios.Scenario;
import rpg.characters.Player;
import rpg.skills.DamageType;
import rpg.skills.Skill;
import rpg.ui.TitleScreen;
import rpg.ui.ConsoleUI;

import java.util.Scanner;

// Starts the game and controls its initial execution.
public class Main {

	public static void main(String[] args) {
		
		Scanner scanner = new Scanner(System.in);
		
		TitleScreen.show(scanner);
		ConsoleUI.clearScreen();
		
//Ordem atual dos parâmetros: nome, HP, MP, SP, ataque, defesa, poder de cura, sorte
		Player player =
				new Player("Flagelo", 120, 50, 110, 15, 14, 50, 10);
		Skill investida = new Skill(
		        "Investida",
		        1,
		        DamageType.PHYSICAL,
		        5,
		        0,
		        0,
		        10,
		        "Corre até o alvo e o atropela. "
		        + "Causa dano físico moderado e pode causar dano a si mesmo."
		);
		
		player.learnSkill(investida);
		
		Scenario currentScenario = new Field(
		        "Verdes Ermos",
		        "Uma vasta região de campos e vegetação rasteira, pontuada por concentrações isoladas de árvores."
		        + "\nEssas pequenas formações florestais escondem perigos e segredos, mas também atraem mercadores"
		        + "\nque se arriscam em busca de ingredientes raros."
		        + "\nAo fundo, uma imensa formação rochosa reflete os raios do sol, tingindo a zona com feixes azuis"
		        + "\nque emanam das eletrorrochas."
		        ,
		        25,
		        5
		);
		
		World world = new World(
		        player,
		        currentScenario,
		        scanner
		);

		world.start();
		
		scanner.close();


	}
}