package rpg.ui;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import rpg.battle.BattleType;
import rpg.battle.EnemyBattleGroup;
import rpg.battle.EnemyBattleSlot;
import rpg.characters.Party;

// Displays the living enemies and allows target selection.
public final class EnemyTargetMenu {

	private static final String DEFAULT_MENU_TITLE = "Escolha o alvo:";

	private final ConsoleInput consoleInput;
	private final BattleScreen battleScreen;
	private final Party party;
	private final String scenarioName;
	private final BattleType battleType;

	public EnemyTargetMenu(
			ConsoleInput consoleInput,
			BattleScreen battleScreen,
			Party party,
			String scenarioName,
			BattleType battleType
	) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.battleScreen = Objects.requireNonNull(battleScreen, "Battle screen cannot be null.");
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.scenarioName = Objects.requireNonNull(scenarioName, "Scenario name cannot be null.");
		this.battleType = Objects.requireNonNull(battleType, "Battle type cannot be null.");
	}

	public Optional<EnemyBattleSlot> selectTarget(EnemyBattleGroup enemyGroup, int roundNumber) {
		return selectTarget(enemyGroup, DEFAULT_MENU_TITLE, roundNumber);
	}

	public Optional<EnemyBattleSlot> selectTarget(EnemyBattleGroup enemyGroup, String menuTitle, int roundNumber) {
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

		String feedbackMessage = null;

		while (true) {
			battleScreen.displayEnemyTargetSelection(
					scenarioName,
					battleType,
					enemyGroup,
					party,
					roundNumber,
					menuTitle,
					feedbackMessage
			);
			feedbackMessage = null;

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return Optional.empty();
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= livingTargets.size()) {
					feedbackMessage = "Alvo inválido.";
					continue;
				}

				return Optional.of(livingTargets.get(selectedIndex));
			} catch (NumberFormatException exception) {
				feedbackMessage = "Comando inválido.";
			}
		}
	}
}