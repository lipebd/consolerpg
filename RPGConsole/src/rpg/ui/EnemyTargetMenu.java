package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import rpg.battle.EnemyBattleGroup;
import rpg.battle.EnemyBattleSlot;

// Displays the living enemies and allows target selection.
public final class EnemyTargetMenu {

	private final Scanner scanner;

	public EnemyTargetMenu(Scanner scanner) {
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
	}

	public Optional<EnemyBattleSlot> selectTarget(EnemyBattleGroup enemyGroup) {
		Objects.requireNonNull(enemyGroup, "Enemy battle group cannot be null.");

		List<EnemyBattleSlot> livingTargets = enemyGroup.getLivingSlots();

		if (livingTargets.isEmpty()) {
			throw new IllegalStateException("No living enemy is available.");
		}

		if (livingTargets.size() == 1) {
			return Optional.of(livingTargets.get(0));
		}

		while (true) {
			System.out.println("\nEscolha o alvo:");

			for (int index = 0; index < livingTargets.size(); index++) {

				EnemyBattleSlot slot = livingTargets.get(index);

				System.out.println("[" + (index + 1) + "] " + slot.getDisplayName() + " | HP: "
						+ slot.getEnemy().getCurrentHp() + "/" + slot.getEnemy().getMaxHp());
			}

			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine();

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