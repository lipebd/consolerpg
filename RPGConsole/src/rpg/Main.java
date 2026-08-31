package rpg;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import rpg.characters.CharacterStats;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyDropCatalog;
import rpg.enemies.EnemyDropLoader;
import rpg.enemies.EnemyLoader;
import rpg.skills.DamageType;
import rpg.skills.RecoveryType;
import rpg.skills.Skill;
import rpg.skills.SkillTargetType;
import rpg.ui.ConsoleUI;
import rpg.ui.TitleScreen;
import rpg.world.World;
import rpg.world.encounters.EncounterCatalog;
import rpg.world.encounters.EncounterLoader;
import rpg.world.exploration.ExplorationSettings;
import rpg.world.scenarios.City;
import rpg.world.scenarios.Dungeon;
import rpg.world.scenarios.DungeonSettings;
import rpg.world.scenarios.Field;
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.travel.TravelCatalog;

// Starts the game and controls its initial execution.
public class Main {

	public static void main(String[] args) {

		EnemyCatalog enemyCatalog;
		EncounterCatalog encounterCatalog;
		EnemyDropCatalog enemyDropCatalog;

		Scanner scanner = new Scanner(System.in);

		try {
			EnemyLoader enemyLoader = new EnemyLoader();

			enemyCatalog = enemyLoader.load(Path.of("data", "enemies.txt"));

			EnemyDropLoader enemyDropLoader = new EnemyDropLoader();

			enemyDropCatalog = enemyDropLoader.load(Path.of("data", "enemy_drops.txt"), enemyCatalog);

			EncounterLoader encounterLoader = new EncounterLoader();

			encounterCatalog = encounterLoader.load(Path.of("data", "encounters.txt"), enemyCatalog);

		} catch (IOException | IllegalArgumentException exception) {
			System.out.println("Não foi possível carregar os dados do jogo.");

			System.out.println(exception.getMessage());
			return;
		}

		TitleScreen.show(scanner);
		ConsoleUI.clearScreen();

		CharacterStats flageloStats = new CharacterStats(120, // HP
				50, // MP
				110, // SP
				15, // ATK
				14, // DEF
				9, // MAG
				12, // RES
				12, // VEL
				10 // LCK
		);

		PlayableCharacter flagelo = new PlayableCharacter("Flagelo", flageloStats);

		Party party = new Party(flagelo);

		CharacterStats bardoStats = new CharacterStats(80, // HP
				80, // MP
				60, // SP
				8, // ATK
				8, // DEF
				12, // MAG
				10, // RES
				15, // VEL
				14 // LCK
		);

		PlayableCharacter bardo = new PlayableCharacter("Bardo", bardoStats);

		party.addMember(bardo);

		Skill investida = new Skill("Investida", 1, DamageType.PHYSICAL, 5, 0, RecoveryType.NONE, SkillTargetType.NONE,
				0, 10,
				"Corre até o alvo e o atropela." + "\nCausa dano físico moderado e pode causar dano a si mesmo.");

		Skill perseveranca = new Skill("Perseverança", 1, DamageType.NONE, 0, 10, RecoveryType.HEAL,
				SkillTargetType.SELF, 0, 10,
				"Mesmo ferido, firma seus pés em batalha e avança sobre o inimigo causando dano físico."
						+ "\nAo final do ataque, recupera uma pequena quantidade do próprio HP");

		Skill palavraCurativa = new Skill("Palavra Curativa", 1, DamageType.NONE, 0, 35, RecoveryType.HEAL,
				SkillTargetType.SINGLE_ALLY, 0, 10,
				"Uma frase inspiradora, um grito de ajuda, ou apenas uma ordem, que restaura 35 de HP de um aliado.");

		Skill inspiracaoDoBardo = new Skill("Inspiração do Bardo", 1, DamageType.NONE, 0, 10, RecoveryType.HEAL,
				SkillTargetType.ALL_ALLIES, 0, 10, "Inspira seus aliados, curando HP de todo o grupo.");

		Skill chamadoDosEcos = new Skill("Chamado dos Ecos (TEMP)", 1, DamageType.NONE, 0, 10, RecoveryType.HEAL,
				SkillTargetType.ALL_BUT_SELF, 0, 10,
				"Entra em um transe altruísta, invocando forças obscuras para normalizar e curar os aliados enquanto abre mão da sua própria cura."
						+ "\nReforça a DEF e RES dos aliados em 2 pontos por 2 turnos.");

		Skill reviveUm = new Skill("Revive um (TEMP)", 1, DamageType.NONE, 0, 100, RecoveryType.REVIVE,
				SkillTargetType.SINGLE_ALLY, 0, 15, "Skill de teste. Revive um aliado.");

		Skill reviveTodos = new Skill("Revive todos (TEMP)", 1, DamageType.NONE, 0, 50, RecoveryType.REVIVE,
				SkillTargetType.ALL_ALLIES, 0, 15, "Skill de teste. Revive todos os aliados.");

		flagelo.learnSkill(investida);
		flagelo.learnSkill(perseveranca);
		flagelo.learnSkill(chamadoDosEcos);
		flagelo.learnSkill(reviveUm);
		bardo.learnSkill(inspiracaoDoBardo);
		bardo.learnSkill(palavraCurativa);
		bardo.learnSkill(reviveTodos);

		ExplorationSettings verdesErmosSettings = new ExplorationSettings(5, // exploration length
				70, // battle chance
				15, // item chance
				15, // merchant chance for open areas
				0, // epic chance for dungeons
				30, // special area chance
				1, // minimum enemies
				2, // maximum enemies
				"Você avança pelos vastos campos verdes.", "Você encontrou uma área especial " + "de mini-floresta!");

		Scenario currentScenario = new Field("verdes_ermos", "Verdes Ermos",
				"Uma vasta região de campos e vegetação rasteira, pontuada por concentrações isoladas de árvores."
						+ "\nEssas pequenas formações florestais escondem perigos e segredos, mas também atraem mercadores"
						+ "\nque se arriscam em busca de ingredientes raros."
						+ "\nAo fundo, uma imensa formação rochosa reflete os raios do sol, tingindo a zona com feixes azuis"
						+ "\nque emanam das eletrorrochas.",
				25, 5, verdesErmosSettings);

		ExplorationSettings grutaSettings = new ExplorationSettings(11, // exploration length
				70, // battle chance
				20, // item chance
				0, // merchant chance
				10, // epic encounter chance
				30, // special area chance
				2, // minimum enemies
				2, // maximum enemies
				"Você avança pelos túneis eletrificados da gruta.",
				"Você encontrou uma câmara incomum entre as eletrorrochas!");

		DungeonSettings grutaDungeonSettings = new DungeonSettings("mestre_eletrogoblin", "relampagolem");

		Scenario grutaRelamparrocha = new Dungeon("gruta_relamparrocha", "Gruta Relamparrocha",
				"Coberta de rochas magnéticas, popularmente conhecidas como eletrorrochas."
						+ "\nEla possui uma presença quase assustadora de estática no ar. Visitantes desavisados"
						+ "\nacabam encurralados por goblins que usam eletricidade como recurso ofensivo."
						+ "\nÉ possivel ouvir um barulho alto de batida vindo do fundo da caverna. ",
				100, 100, grutaSettings, grutaDungeonSettings);

		Scenario bree = new City("bree", "Bree", "Uma cidade movimentada, erguida no encontro de várias estradas."
				+ "\nSeu mercado reúne comerciantes de diversas regiões,"
				+ "\nenquanto a taverna é ponto de encontro de viajantes e aventureiros."
				+ "\nÉ de Bree que aventureiros inconsequentes saem para arriscar suas vidas em busca de tesouros e glória.");

		ScenarioCatalog scenarioCatalog = new ScenarioCatalog();

		scenarioCatalog.register(bree);
		scenarioCatalog.register(currentScenario);
		scenarioCatalog.register(grutaRelamparrocha);

		TravelCatalog travelCatalog = new TravelCatalog();

		travelCatalog.register("verdes_ermos", "bree");

		travelCatalog.register("verdes_ermos", "gruta_relamparrocha");

		travelCatalog.register("bree", "verdes_ermos");

		travelCatalog.register("gruta_relamparrocha", "verdes_ermos");

		travelCatalog.register("gruta_relamparrocha", "bree");

		World world = new World(party, currentScenario, scanner, enemyCatalog, enemyDropCatalog, encounterCatalog,
				scenarioCatalog, travelCatalog);

		world.start();

		scanner.close();

	}
}