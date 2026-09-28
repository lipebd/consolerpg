package rpg.ui;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import rpg.characters.CharacterAttribute;
import rpg.enemies.EnemyCatalog;
import rpg.enemies.EnemyDefinition;
import rpg.world.bestiary.BestiaryStateCatalog;

// Displays discovered enemy records without advancing world time.
public final class BestiaryMenu {

	private static final int ENTRIES_PER_PAGE = 7;

	private final ConsoleInput consoleInput;
	private final EnemyCatalog enemyCatalog;
	private final BestiaryStateCatalog bestiaryStateCatalog;

	public BestiaryMenu(
			ConsoleInput consoleInput,
			EnemyCatalog enemyCatalog,
			BestiaryStateCatalog bestiaryStateCatalog
	) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.enemyCatalog = Objects.requireNonNull(enemyCatalog, "Enemy catalog cannot be null.");
		this.bestiaryStateCatalog = Objects
				.requireNonNull(bestiaryStateCatalog, "Bestiary state catalog cannot be null.");
	}

	public void open() {
		List<EnemyDefinition> enemies = enemyCatalog.getAll();
		int pageIndex = 0;
		String feedbackMessage = null;

		while (true) {
			int pageCount = Math.max(1, (enemies.size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);
			int firstEnemyIndex = pageIndex * ENTRIES_PER_PAGE;
			int lastEnemyIndex = Math.min(firstEnemyIndex + ENTRIES_PER_PAGE, enemies.size());

			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading("BESTIÁRIO");
			System.out.println();
			System.out.println(
					"Descobertos: " + bestiaryStateCatalog.getDiscoveredEnemyIds().size() + "/" + enemies.size()
			);
			System.out.println("Página " + (pageIndex + 1) + "/" + pageCount);
			System.out.println();

			for (int enemyIndex = firstEnemyIndex; enemyIndex < lastEnemyIndex; enemyIndex++) {
				EnemyDefinition enemy = enemies.get(enemyIndex);
				String displayedName = bestiaryStateCatalog.isDiscovered(enemy.getId()) ? enemy.getName() : "???";

				System.out.println("[" + (enemyIndex - firstEnemyIndex + 1) + "] " + displayedName);
			}

			StringBuilder pageNavigation = new StringBuilder();

			if (pageIndex > 0) {
				pageNavigation.append("[A] Anterior    ");
			}

			if (pageIndex + 1 < pageCount) {
				pageNavigation.append("[P] Próxima");
			}
			if (pageCount > 1) {
				System.out.println();
				System.out.println(pageNavigation);
			}

			System.out.println(feedbackMessage != null ? feedbackMessage : "");

			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return;
			}

			if (choice.equalsIgnoreCase("A") && pageIndex > 0) {
				pageIndex--;
				feedbackMessage = null;
				continue;
			}

			if (choice.equalsIgnoreCase("P") && pageIndex + 1 < pageCount) {
				pageIndex++;
				feedbackMessage = null;
				continue;
			}

			try {
				int selectedEnemyIndex = firstEnemyIndex + Integer.parseInt(choice) - 1;

				if (selectedEnemyIndex < firstEnemyIndex || selectedEnemyIndex >= lastEnemyIndex) {
					feedbackMessage = "Registro inválido.";
					continue;
				}

				EnemyDefinition selectedEnemy = enemies.get(selectedEnemyIndex);

				if (!bestiaryStateCatalog.isDiscovered(selectedEnemy.getId())) {
					feedbackMessage = "Analise este inimigo em batalha para revelar seu registro.";
					continue;
				}

				displayEntry(selectedEnemy);
				feedbackMessage = null;
			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número de um registro.";
			}
		}
	}

	private void displayEntry(EnemyDefinition enemy) {
		String feedbackMessage = null;

		while (true) {
			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(enemy.getName());
			System.out.println();
			System.out.println(
					"Rank: " + enemy.getRank() + "    Personalidade: " + enemy.getTargetProfile().getDisplayName()
			);

			String specializations = formatSpecializations(enemy);
			if (!specializations.isEmpty()) {
				System.out.println("Especialidades: " + specializations);
			}

			if (enemy.getWeaknessAttribute() != null) {
				System.out.println("Fraqueza: " + enemy.getWeaknessAttribute() + " ↓");
			}

			System.out.println(
					"Altura: " + formatMeasurement(enemy.getHeight()) + " m    Peso: "
							+ formatMeasurement(enemy.getWeight()) + " kg"
			);
			System.out.println();
			ConsoleUI.displayWrappedText(enemy.getDescription());

			if (feedbackMessage != null) {
				System.out.println();
				ConsoleUI.displayWrappedText(feedbackMessage);
			}

			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			if (consoleInput.readTrimmedLine().equals("0")) {
				return;
			}

			feedbackMessage = "Opção inválida.";
		}
	}

	private static String formatSpecializations(EnemyDefinition enemy) {
		CharacterAttribute primaryAttribute = enemy.getPrimaryAttribute();
		CharacterAttribute secondaryAttribute = enemy.getSecondaryAttribute();

		if (primaryAttribute == null) {
			return secondaryAttribute == null ? "" : secondaryAttribute + " ↑";
		}

		if (secondaryAttribute == null) {
			return primaryAttribute + " ↑↑";
		}

		return primaryAttribute + " ↑↑ | " + secondaryAttribute + " ↑";
	}

	private static String formatMeasurement(double value) {
		return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString().replace('.', ',');
	}
}
