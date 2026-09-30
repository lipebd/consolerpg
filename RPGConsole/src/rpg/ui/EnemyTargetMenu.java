package rpg.ui;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import rpg.battle.EnemyBattleGroup;
import rpg.battle.EnemyBattleSlot;

// Displays the living enemies and allows target selection.
public final class EnemyTargetMenu {

	private static final String DEFAULT_MENU_TITLE = "Selecionar alvo";

	private final ConsoleInput consoleInput;

	public EnemyTargetMenu(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public Optional<EnemyBattleSlot> selectTarget(EnemyBattleGroup enemyGroup) {
		return selectTarget(enemyGroup, DEFAULT_MENU_TITLE);
	}

	public Optional<EnemyBattleSlot> selectTarget(EnemyBattleGroup enemyGroup, String menuTitle) {
		Objects.requireNonNull(enemyGroup, "Enemy battle group cannot be null.");

		if (menuTitle == null || menuTitle.isBlank()) {
			throw new IllegalArgumentException("Enemy target menu title cannot be empty.");
		}

		List<EnemyBattleSlot> livingTargets = enemyGroup.getLivingSlots();

		if (livingTargets.isEmpty()) {
			throw new IllegalStateException("No living enemy is available.");
		}

		if (livingTargets.size() == 1) {
			return Optional.of(livingTargets.get(0));
		}

		int nameColumnWidth = livingTargets.stream().mapToInt(slot -> slot.getDisplayName().length()).max().orElse(0);

		while (true) {
			System.out.println("\n[" + menuTitle.toUpperCase(Locale.ROOT) + "]");

			System.out.println();
			System.out.println("Escolha o alvo:");
			System.out.println();

			for (int index = 0; index < livingTargets.size(); index++) {
				EnemyBattleSlot slot = livingTargets.get(index);

				System.out.printf(
						"[%d] %-" + nameColumnWidth + "s | HP: %d/%d%n",
						index + 1,
						slot.getDisplayName(),
						slot.getEnemy().getCurrentHp(),
						slot.getEnemy().getMaxHp()
				);
			}

			System.out.println();
			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= livingTargets.size()) {
					System.out.println("Alvo inválido.");
					continue;
				}

				return Optional.of(livingTargets.get(selectedIndex));
			} catch (NumberFormatException exception) {
				System.out.println("Comando inválido.");
			}
		}
	}
}