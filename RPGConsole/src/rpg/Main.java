package rpg;

import rpg.ui.TitleScreen;
import rpg.ui.ConsoleUI;
import rpg.world.World;
import rpg.world.scenarios.Field;
import rpg.world.scenarios.Scenario;
import rpg.world.encounters.EncounterLoader;
import rpg.world.encounters.EncounterCatalog;
import rpg.world.exploration.ExplorationSettings;
import rpg.characters.Player;
import rpg.characters.CharacterStats;
import rpg.skills.Skill;
import rpg.skills.DamageType;
import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyLoader;

import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Path;

// Starts the game and controls its initial execution.
public class Main {

	public static void main(String[] args) {
		
		EnemyCatalog enemyCatalog;
		EncounterCatalog encounterCatalog;
		
		Scanner scanner = new Scanner(System.in);
		
		try {
		    EnemyLoader enemyLoader = new EnemyLoader();

		    enemyCatalog = enemyLoader.load(
		            Path.of("data", "enemies.txt")
		    );

		    EncounterLoader encounterLoader =
		            new EncounterLoader();

		    encounterCatalog = encounterLoader.load(
		            Path.of("data", "encounters.txt"),
		            enemyCatalog
		    );
		} catch (IOException | IllegalArgumentException exception) {
		    System.out.println(
		            "Não foi possível carregar os dados do jogo."
		    );

		    System.out.println(exception.getMessage());
		    return;
		}
		
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
		
		ExplorationSettings verdesErmosSettings =
		        new ExplorationSettings( // casas, % luta, % área especial, min monstros, max monstros, texto normal, texto especial
		                5,   // tamanho da exploração
		                70,  // chance de luta (%)
		                100, // chance de área especial (%)
		                1,   // minimo de monstros por luta
		                2,   // maximo de monstros por luta
		                "Você avança pelos vastos campos verdes.",            // mensagem de exploraçoã área normal
		                "Você encontrou uma área especial de mini-floresta!"  // mensagem de exploração área especial
		        );
		
		Scenario currentScenario = new Field(
				"verdes_ermos",
		        "Verdes Ermos",
		        "Uma vasta região de campos e vegetação rasteira, pontuada por concentrações isoladas de árvores."
		        + "\nEssas pequenas formações florestais escondem perigos e segredos, mas também atraem mercadores"
		        + "\nque se arriscam em busca de ingredientes raros."
		        + "\nAo fundo, uma imensa formação rochosa reflete os raios do sol, tingindo a zona com feixes azuis"
		        + "\nque emanam das eletrorrochas."
		        ,
		        25,
		        5,
		        verdesErmosSettings
		);
		
		World world = new World(
		        player,
		        currentScenario,
		        scanner,
		        enemyCatalog,
		        encounterCatalog
		);

		world.start();
		
		scanner.close();


	}
}