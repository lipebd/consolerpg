package rpg;

import rpg.world.World;
import rpg.world.scenarios.Field;
import rpg.world.scenarios.Scenario;
import rpg.characters.CharacterStats;
import rpg.characters.Player;
import rpg.skills.DamageType;
import rpg.skills.Skill;
import rpg.ui.TitleScreen;
import rpg.ui.ConsoleUI;
import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyLoader;

import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Path;

// Starts the game and controls its initial execution.
public class Main {

	public static void main(String[] args) {
		
		Scanner scanner = new Scanner(System.in);
		
		EnemyCatalog enemyCatalog;

		try {
		    EnemyLoader enemyLoader = new EnemyLoader();

		    enemyCatalog = enemyLoader.load(
		            Path.of("data", "enemies.txt")
		    );
		} catch (IOException | IllegalArgumentException exception) {
		    System.out.println(
		            "Não foi possível carregar os dados dos inimigos."
		    );

		    System.out.println(exception.getMessage());
		    return;
		}
		System.out.println(
		        "Inimigo carregado: "
		        + enemyCatalog.getById("lobermo").getName()
		);
		
		TitleScreen.show(scanner);
		ConsoleUI.clearScreen();
		
		CharacterStats flageloStats = new CharacterStats(
		        120, // HP
		        50,  // MP
		        110, // SP
		        15,  // ATK
		        14,  // DEF
		        9,   // MAG
		        12,  // RES
		        12,  // VEL
		        10   // LCK
		);

		Player player = new Player("Flagelo", flageloStats);
		
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
		        scanner,
		        enemyCatalog
		);

		world.start();
		
		scanner.close();


	}
}