package rpg;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import rpg.world.branches.ScenarioBranchCatalog;
import rpg.world.branches.ScenarioBranchLoader;
import rpg.world.travel.TravelProgress;
import rpg.world.travel.TravelRouteLoader;
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
import rpg.world.scenarios.CombatScenarioDefinitionCatalog;
import rpg.world.scenarios.CombatScenarioLoader;
import rpg.world.scenarios.DungeonDefinitionCatalog;
import rpg.world.scenarios.DungeonLoader;
import rpg.world.scenarios.Scenario;
import rpg.world.scenarios.ScenarioCatalog;
import rpg.world.scenarios.ScenarioDefinition;
import rpg.world.scenarios.ScenarioDefinitionCatalog;
import rpg.world.scenarios.ScenarioFactory;
import rpg.world.scenarios.ScenarioLoader;
import rpg.world.travel.TravelCatalog;

// Starts the game and controls its initial execution.
public class Main {

	public static void main(String[] args) {

		EnemyCatalog enemyCatalog;
		EncounterCatalog encounterCatalog;
		EnemyDropCatalog enemyDropCatalog;
		ScenarioCatalog scenarioCatalog;
		ScenarioBranchCatalog scenarioBranchCatalog;
		Scenario currentScenario;
		TravelProgress travelProgress;

		Scanner scanner = new Scanner(System.in);

		try {
			EnemyLoader enemyLoader = new EnemyLoader();

			enemyCatalog = enemyLoader.load(Path.of("data", "enemies.txt"));

			EnemyDropLoader enemyDropLoader = new EnemyDropLoader();

			enemyDropCatalog = enemyDropLoader.load(Path.of("data", "enemy_drops.txt"), enemyCatalog);

			EncounterLoader encounterLoader = new EncounterLoader();

			encounterCatalog = encounterLoader.load(Path.of("data", "encounters.txt"), enemyCatalog);

			ScenarioLoader scenarioLoader = new ScenarioLoader();

			ScenarioDefinitionCatalog scenarioDefinitionCatalog = scenarioLoader.load(Path.of("data", "scenarios.txt"));

			CombatScenarioLoader combatScenarioLoader = new CombatScenarioLoader();

			CombatScenarioDefinitionCatalog combatScenarioDefinitionCatalog = combatScenarioLoader
					.load(Path.of("data", "combat_scenarios.txt"), scenarioDefinitionCatalog);

			DungeonLoader dungeonLoader = new DungeonLoader();

			DungeonDefinitionCatalog dungeonDefinitionCatalog = dungeonLoader.load(Path.of("data", "dungeons.txt"),
					scenarioDefinitionCatalog, combatScenarioDefinitionCatalog, enemyCatalog);

			ScenarioBranchLoader scenarioBranchLoader = new ScenarioBranchLoader();

			scenarioBranchCatalog = scenarioBranchLoader.load(Path.of("data", "scenario_branches.txt"),
					scenarioDefinitionCatalog, combatScenarioDefinitionCatalog);

			scenarioCatalog = new ScenarioCatalog();

			ScenarioFactory scenarioFactory = new ScenarioFactory();

			for (ScenarioDefinition definition : scenarioDefinitionCatalog.getAll()) {
				Scenario scenario = scenarioFactory.create(definition, combatScenarioDefinitionCatalog,
						dungeonDefinitionCatalog);

				scenarioCatalog.register(scenario);
			}

			currentScenario = scenarioCatalog.getById("bree");

			TravelRouteLoader travelRouteLoader = new TravelRouteLoader();

			TravelCatalog travelCatalog = travelRouteLoader.load(Path.of("data", "travel_routes.txt"),
					scenarioDefinitionCatalog, combatScenarioDefinitionCatalog);

			travelProgress = new TravelProgress(travelCatalog);

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

		World world = new World(party, currentScenario, scanner, enemyCatalog, enemyDropCatalog, encounterCatalog,
				scenarioCatalog, scenarioBranchCatalog, travelProgress);

		world.start();

		scanner.close();

	}
}