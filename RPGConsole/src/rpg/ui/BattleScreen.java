package rpg.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Locale;

import rpg.battle.BattleType;
import rpg.battle.EnemyBattleGroup;
import rpg.battle.EnemyBattleSlot;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.skills.SkillDefinition;
import rpg.characters.Enemy;
import rpg.characters.GameCharacter;

// Displays the current participants without controlling battle execution.
public final class BattleScreen {

	private static final int MESSAGE_WIDTH = 75;
	private static final int MESSAGE_WINDOW_LINES = 3;
	private static final long MESSAGE_CHARACTER_DELAY_MILLIS = 15;
	private static final long MESSAGE_LINE_PAUSE_MILLIS = 350;
	private static final int STATUS_LABEL_WIDTH = 22;
	private static final int[] ENEMY_COLUMN_WIDTHS = { 22, 23, 22 };
	private static final int PARTY_COLUMN_WIDTH = 16;
	private static final int ENEMY_COLUMNS = 3;
	private static final int PARTY_COLUMNS = 4;

	private final ConsoleInput consoleInput;

	public BattleScreen(ConsoleInput consoleInput) {
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
	}

	public void displayPanel(String scenarioName, BattleType battleType, EnemyBattleGroup enemyGroup, Party party) {
		displayPanel(scenarioName, battleType, enemyGroup, party, false, List.of());
	}

	private void displayPanel(
			String scenarioName,
			BattleType battleType,
			EnemyBattleGroup enemyGroup,
			Party party,
			boolean numberLivingEnemies,
			List<PlayableCharacter> selectableAllies
	) {
		Objects.requireNonNull(battleType, "Battle type cannot be null.");
		Objects.requireNonNull(enemyGroup, "Enemy group cannot be null.");
		Objects.requireNonNull(party, "Party cannot be null.");
		Objects.requireNonNull(selectableAllies, "Selectable allies cannot be null.");

		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(scenarioName, headingQualifier(battleType));

		displayEnemies(enemyGroup, numberLivingEnemies);

		System.out.println();
		displayParty(party, selectableAllies);
	}

	static String headingQualifier(BattleType battleType) {
		return switch (battleType) {
		case NORMAL -> "Batalha";
		case BOSS -> "Batalha de Chefe";
		case EPIC -> "Batalha Épica";
		};
	}

	private static void displayEnemies(EnemyBattleGroup enemyGroup, boolean numberLivingEnemies) {
		List<String> names = new ArrayList<>();
		List<String> hp = new ArrayList<>();
		List<String> effects = new ArrayList<>();

		List<EnemyBattleSlot> slots = enemyGroup.getSlots();
		int targetNumber = 1;
		for (int index = 0; index < slots.size(); index++) {
			EnemyBattleSlot slot = slots.get(index);
			int width = ENEMY_COLUMN_WIDTHS[index];

			String name = slot.getDisplayName();

			if (numberLivingEnemies && slot.isAlive()) {
				String marker = "[" + targetNumber++ + "]";
				int nameWidth = width - ConsoleUI.getVisibleLength(marker) - 1;

				names.add("\u001B[33m" + marker + "\u001B[0m " + truncateToWidth(name, nameWidth));
			} else {
				names.add(truncateToWidth(name, width));
			}
			hp.add(formatResource("HP", slot.getEnemy().getCurrentHp(), slot.getEnemy().getMaxHp()));
			effects.add(
					slot.isAlive() ? CombatEffectFormatter.formatCompactEffectSummary(slot.getEnemy(), width)
							: "\u001B[31mDERROTADO\u001B[0m"
			);
		}

		printCells(names, ENEMY_COLUMN_WIDTHS);
		printCells(hp, ENEMY_COLUMN_WIDTHS);
		printCells(effects, ENEMY_COLUMN_WIDTHS);
	}

	public void displayEnemyTargetSelection(
			String scenarioName,
			BattleType battleType,
			EnemyBattleGroup enemyGroup,
			Party party,
			int roundNumber,
			String menuTitle,
			String feedbackMessage
	) {
		displayPanel(scenarioName, battleType, enemyGroup, party, true, List.of());

		System.out.println();
		System.out.println("Rodada " + roundNumber + ": " + menuTitle);
		System.out.println(feedbackMessage == null ? "" : feedbackMessage);
		System.out.println("[0] Voltar");
		System.out.println();
		System.out.print("> ");
	}

	private static String truncateToWidth(String text, int width) {
		if (ConsoleUI.getVisibleLength(text) <= width) {
			return text;
		}

		int endIndex = text.offsetByCodePoints(0, width - 1);
		return text.substring(0, endIndex) + "…";
	}

	private static String formatResource(String label, int current, int max) {
	    String color = "";

	    if (max <= 0 || current <= 0 || (long) current * 100 <= (long) max * 15) {
	        color = "\u001B[31m";          // Vermelho
	    } else if ((long) current * 100 <= (long) max * 30) {
	        color = "\u001B[38;5;208m";    // Laranja
	    } else if ((long) current * 100 <= (long) max * 50) {
	        color = "\u001B[33m";          // Amarelo
	    }

	    String displayedValue = color.isEmpty()
	            ? String.valueOf(current)
	            : color + current + "\u001B[0m";

	    return label + " " + displayedValue + "/" + max;
	}

	private static void displayParty(Party party, List<PlayableCharacter> selectableAllies) {
		List<String> classesAndLevels = new ArrayList<>();
		List<String> names = new ArrayList<>();
		List<String> hp = new ArrayList<>();
		List<String> mp = new ArrayList<>();
		List<String> sp = new ArrayList<>();
		List<String> effects = new ArrayList<>();

		List<PlayableCharacter> members = party.getMembers();

		for (int index = 0; index < members.size(); index++) {
			PlayableCharacter member = members.get(index);

			String classAndLevel = member.getPlayableClassDefinition().getSymbol() + " Nv " + member.getLevel();

			int targetIndex = selectableAllies.indexOf(member);
			String marker = targetIndex < 0 ? "" : "\u001B[33m[" + (targetIndex + 1) + "]\u001B[0m ";

			int leftPadding = Math.max(
					0,
					PARTY_COLUMN_WIDTH - ConsoleUI.getVisibleLength(marker) - ConsoleUI.getVisibleLength(classAndLevel)
			);

			classesAndLevels.add(marker + " ".repeat(leftPadding) + classAndLevel);

			names.add(member.getName());
			hp.add(formatResource("HP", member.getCurrentHp(), member.getMaxHp()));
			mp.add(formatResource("MP", member.getCurrentMp(), member.getMaxMp()));
			sp.add(formatResource("SP", member.getCurrentSp(), member.getMaxSp()));

			effects.add(
					member.isAlive() ? CombatEffectFormatter.formatCompactEffectSummary(member, PARTY_COLUMN_WIDTH)
							: "\u001B[31mDERROTADO\u001B[0m"
			);
		}

		printCells(classesAndLevels, PARTY_COLUMN_WIDTH, PARTY_COLUMNS);
		printCells(names, PARTY_COLUMN_WIDTH, PARTY_COLUMNS);
		printCells(hp, PARTY_COLUMN_WIDTH, PARTY_COLUMNS);
		printCells(mp, PARTY_COLUMN_WIDTH, PARTY_COLUMNS);
		printCells(sp, PARTY_COLUMN_WIDTH, PARTY_COLUMNS);
		printCells(effects, PARTY_COLUMN_WIDTH, PARTY_COLUMNS);
	}

	public void displayAllyTargetSelection(
			String scenarioName,
			BattleType battleType,
			EnemyBattleGroup enemyGroup,
			Party party,
			int roundNumber,
			String menuTitle,
			List<PlayableCharacter> selectableAllies,
			String feedbackMessage
	) {
		displayPanel(scenarioName, battleType, enemyGroup, party, false, selectableAllies);

		System.out.println();
		System.out.println("Rodada " + roundNumber + ": " + menuTitle);
		System.out.println(feedbackMessage == null ? "" : feedbackMessage);
		System.out.println("[0] Voltar");
		System.out.println();
		System.out.print("> ");
	}

	private static void printCells(List<String> values, int[] widths) {
		for (int index = 0; index < widths.length; index++) {
			String value = index < values.size() ? values.get(index) : "";
			int padding = Math.max(0, widths[index] - ConsoleUI.getVisibleLength(value));

			System.out.print("| " + value + " ".repeat(padding) + " ");
		}

		System.out.println("|");
	}

	private static void printCells(List<String> values, int cellWidth, int columnCount) {
		for (int index = 0; index < columnCount; index++) {
			String value = index < values.size() ? values.get(index) : "";
			int padding = Math.max(0, cellWidth - ConsoleUI.getVisibleLength(value));

			System.out.print("| " + value + " ".repeat(padding) + " ");
		}

		System.out.println("|");
	}

	public void displayPlayerTurn(
			String scenarioName,
			BattleType battleType,
			EnemyBattleGroup enemyGroup,
			Party party,
			int roundNumber,
			String actingName,
			String nextName,
			String feedbackMessage
	) {
		displayPanel(scenarioName, battleType, enemyGroup, party);

		System.out.println();
		System.out.println("Rodada " + roundNumber + ": Vez de " + actingName + "!    A seguir: " + nextName);

		System.out.println(feedbackMessage == null ? "" : feedbackMessage);
		System.out.printf("%-15s%-15s%s%n", "[1] Atacar", "[2] Defender", "[3] Especial");
		System.out.printf("%-15s%-15s%s%n", "[4] Itens", "[5] Tática", "[6] Fugir");
		System.out.print("> ");
	}

	public void displayMessages(
			String scenarioName,
			BattleType battleType,
			EnemyBattleGroup enemyGroup,
			Party party,
			int roundNumber,
			List<String> messages
	) {
		Objects.requireNonNull(messages, "Battle messages cannot be null.");

		if (messages.isEmpty()) {
			return;
		}

		String roundPrefix = "Rodada " + roundNumber + ": ";
		String continuationPrefix = " ".repeat(ConsoleUI.getVisibleLength(roundPrefix));
		int messageWidth = MESSAGE_WIDTH - ConsoleUI.getVisibleLength(roundPrefix);

		List<String> lines = new ArrayList<>();

		for (String message : messages) {
			lines.addAll(ConsoleUI.wrapText(message, "", messageWidth));
		}

		consoleInput.discardPendingInput();
		boolean interrupted = false;

		for (int index = 0; index < lines.size() && !interrupted; index++) {
			displayPanel(scenarioName, battleType, enemyGroup, party);
			System.out.println();

			int firstVisible = Math.max(0, index - MESSAGE_WINDOW_LINES + 1);

			for (int previous = firstVisible; previous < index; previous++) {
				System.out.println((previous == firstVisible ? roundPrefix : continuationPrefix) + lines.get(previous));
			}

			System.out.print(index == firstVisible ? roundPrefix : continuationPrefix);

			String line = lines.get(index);

			interrupted = ConsoleUI.animateLine(
			        line,
			        MESSAGE_CHARACTER_DELAY_MILLIS,
			        consoleInput
			);

			if (!interrupted) {
				System.out.println();
				interrupted = consoleInput.pollLine(MESSAGE_LINE_PAUSE_MILLIS).isPresent();
			}
		}

		// O terminal pode ter exibido o Enter: redesenha o estado final completo.
		consoleInput.discardPendingInput();
		displayPanel(scenarioName, battleType, enemyGroup, party);
		System.out.println();

		int firstVisible = Math.max(0, lines.size() - MESSAGE_WINDOW_LINES);

		for (int index = firstVisible; index < lines.size(); index++) {
			System.out.println((index == firstVisible ? roundPrefix : continuationPrefix) + lines.get(index));
		}

		for (int index = lines.size() - firstVisible; index < MESSAGE_WINDOW_LINES; index++) {
			System.out.println();
		}

		consoleInput.discardPendingInput();
	}

	public void displaySkillSelection(
			String scenarioName,
			BattleType battleType,
			EnemyBattleGroup enemyGroup,
			Party party,
			PlayableCharacter actingCharacter,
			List<Optional<SkillDefinition>> skills,
			String feedbackMessage
	) {
		displayPanel(scenarioName, battleType, enemyGroup, party);
		System.out.println();
		System.out.println("Habilidades de " + actingCharacter.getName());
		for (int row = 0; row < 2; row++) {
		    StringBuilder rowText = new StringBuilder();

		    for (int column = 0; column < 3; column++) {
		        int index = column * 2 + row;
		        Optional<SkillDefinition> slot = skills.get(index);
		        String option = "    --";

		        if (slot.isPresent()) {
		            String name = slot.get().getName();

		            if (name.length() > 21) {
		                name = name.substring(0, 20) + "…";
		            }

		            option = "[" + (index + 1) + "] " + name;
		        }

		        rowText.append(option);

		        if (column < 2) {
		            int padding = Math.max(1, 26 - ConsoleUI.getVisibleLength(option));
		            rowText.append(" ".repeat(padding));
		        }
		    }

		    System.out.println(rowText);
		}

		String returnOption = "[0] Voltar";

		if (feedbackMessage == null) {
			System.out.println(returnOption);
		} else {
			int padding = Math.max(1, 25 - ConsoleUI.getVisibleLength(returnOption));

			System.out.println(returnOption + " ".repeat(padding) + feedbackMessage);
		}

		System.out.print("> ");
	}

	public boolean confirmSkillUse(
			String scenarioName,
			BattleType battleType,
			EnemyBattleGroup enemyGroup,
			Party party,
			SkillDefinition skill
	) {
		Objects.requireNonNull(skill, "Skill cannot be null.");

		List<String> descriptionLines = ConsoleUI.wrapText(skill.getName() + ": " + skill.getDescription());

		int descriptionPage = 0;
		boolean showingDescription = false;
		boolean invalidChoice = false;

		while (true) {
			displayPanel(scenarioName, battleType, enemyGroup, party);
			System.out.println();

			if (showingDescription) {
				int firstLine = descriptionPage * 3;
				int lastLine = Math.min(firstLine + 3, descriptionLines.size());

				for (int index = firstLine; index < lastLine; index++) {
					System.out.println(descriptionLines.get(index));
				}

				for (int index = lastLine - firstLine; index < 3; index++) {
					System.out.println();
				}
			} else {
				System.out.println(skill.getName() + " [Nv. " + skill.getLevelRequirement() + "]");
				System.out.println(
						SkillDetailFormatter.formatType(skill) + " · " + SkillDetailFormatter.formatTarget(skill)
				);
				System.out.println(
						"Poder: " + SkillDetailFormatter.formatPower(skill) + " · Custo: "
								+ SkillDetailFormatter.formatCost(skill)
				);
			}

			StringBuilder options = new StringBuilder();

			if (invalidChoice) {
				options.append("Opção inválida.  ");
			}

			options.append("[1] Usar  [2] ");
			options.append(showingDescription ? "Detalhes" : "Descrição");
			options.append("  [0] Voltar");

			if (showingDescription && descriptionPage > 0) {
				options.append("  [A] Anterior");
			}

			if (showingDescription && (descriptionPage + 1) * 3 < descriptionLines.size()) {
				options.append("  [P] Próxima");
			}

			System.out.println(options);
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine().toUpperCase(java.util.Locale.ROOT);

			invalidChoice = false;

			switch (choice) {
			case "1" -> {
				return true;
			}

			case "0" -> {
				return false;
			}

			case "2" -> {
				showingDescription = !showingDescription;

				if (showingDescription) {
					descriptionPage = 0;
				}
			}

			case "A" -> {
				if (showingDescription && descriptionPage > 0) {
					descriptionPage--;
				} else {
					invalidChoice = true;
				}
			}

			case "P" -> {
				if (showingDescription && (descriptionPage + 1) * 3 < descriptionLines.size()) {
					descriptionPage++;
				} else {
					invalidChoice = true;
				}
			}

			default -> invalidChoice = true;
			}
		}
	}

	public void displayItemHeading(String scenarioName, BattleType battleType) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(scenarioName, headingQualifier(battleType) + " - Itens");
	}

	public void displayTacticalMenu(
			String scenarioName,
			BattleType battleType,
			EnemyBattleGroup enemyGroup,
			Party party,
			int roundNumber,
			String feedbackMessage
	) {
		displayPanel(scenarioName, battleType, enemyGroup, party);

		System.out.println();
		System.out.println("Rodada " + roundNumber + ": Tática");
		System.out.println(feedbackMessage == null ? "" : feedbackMessage);
		System.out.printf("%-25s%s%n", "[1] Situação da batalha", "[2] Analisar inimigo");
		System.out.printf("%-25s%s%n", "[3] Ordem da rodada", "[0] Voltar");
		System.out.print("> ");
	}

	public void displayStatusResources(
			String scenarioName,
			BattleType battleType,
			EnemyBattleGroup enemyGroup,
			Party party,
			String feedbackMessage
	) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(scenarioName, headingQualifier(battleType) + " - Situação");

		List<EnemyBattleSlot> enemies = enemyGroup.getSlots();

		for (int index = 0; index < ENEMY_COLUMNS; index++) {
			if (index >= enemies.size()) {
				System.out.println();
				System.out.println();
				continue;
			}

			EnemyBattleSlot slot = enemies.get(index);
			GameCharacter enemy = slot.getEnemy();

			displayStatusEntry(
					"[E" + (index + 1) + "] " + slot.getDisplayName(),
					"HP " + enemy.getCurrentHp() + "/" + enemy.getMaxHp(),
					enemy
			);
		}

		List<PlayableCharacter> members = party.getMembers();

		for (int index = 0; index < PARTY_COLUMNS; index++) {
			if (index >= members.size()) {
				System.out.println();
				System.out.println();
				continue;
			}

			PlayableCharacter member = members.get(index);

			displayStatusEntry(
					"[A" + (index + 1) + "] " + member.getName(),
					"HP " + member.getCurrentHp() + "/" + member.getMaxHp() + " | MP " + member.getCurrentMp() + "/"
							+ member.getMaxMp() + " | SP " + member.getCurrentSp() + "/" + member.getMaxSp(),
					member
			);
		}

		String options = "[2] Efeitos    [3] Legenda    [0] Voltar";
		System.out.println(feedbackMessage == null ? options : options + "    " + feedbackMessage);
		System.out.print("> ");
	}

	private static void displayStatusEntry(String label, String resources, GameCharacter character) {
		String paddedLabel = formatStatusLabel(label);

		System.out.println(paddedLabel + " ↱  " + resources);
		System.out.println(
				" ".repeat(STATUS_LABEL_WIDTH) + " ↳  " + CombatEffectFormatter.formatEffectCountSummary(character)
		);
	}

	public void displayStatusEffects(
			String scenarioName,
			BattleType battleType,
			EnemyBattleGroup enemyGroup,
			Party party,
			String feedbackMessage
	) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(scenarioName, headingQualifier(battleType) + " - Situação - Efeitos");

		List<EnemyBattleSlot> enemies = enemyGroup.getSlots();

		for (int index = 0; index < ENEMY_COLUMNS; index++) {
			if (index >= enemies.size()) {
				System.out.println();
				System.out.println();
				continue;
			}

			EnemyBattleSlot slot = enemies.get(index);
			GameCharacter enemy = slot.getEnemy();

			displayStatusEffectEntry(
					"[E" + (index + 1) + "] " + (enemy.hasDistortedAttributes() ? "⇄" : "") + slot.getDisplayName(),
					enemy
			);
		}

		List<PlayableCharacter> members = party.getMembers();

		for (int index = 0; index < PARTY_COLUMNS; index++) {
			if (index >= members.size()) {
				System.out.println();
				System.out.println();
				continue;
			}

			PlayableCharacter member = members.get(index);

			displayStatusEffectEntry(
					"[A" + (index + 1) + "] " + (member.hasDistortedAttributes() ? "⇄" : "") + member.getName(),
					member
			);
		}

		String options = "[1] Recursos    [3] Legenda    [0] Voltar";
		System.out.println(feedbackMessage == null ? options : options + "    " + feedbackMessage);
		System.out.print("> ");
	}

	private static void displayStatusEffectEntry(String label, GameCharacter character) {
		List<String> lines = CombatEffectFormatter.formatStatusEffectLines(character);

		System.out.println(formatStatusLabel(label) + " ↱  " + lines.get(0));
		System.out.println(" ".repeat(STATUS_LABEL_WIDTH) + " ↳  " + lines.get(1));
	}

	private static String formatStatusLabel(String label) {
		String visibleLabel = truncateToWidth(label, STATUS_LABEL_WIDTH);
		int padding = STATUS_LABEL_WIDTH - ConsoleUI.getVisibleLength(visibleLabel);
		return visibleLabel + " ".repeat(padding);
	}

	public void displayStatusLegend(String scenarioName, BattleType battleType, String feedbackMessage) {
		ConsoleUI.clearScreen();
		ConsoleUI.displayHeading(scenarioName, headingQualifier(battleType) + " - Situação - Legenda");

		System.out.println();
		System.out.println("A  ATK - Ataque");
		System.out.println("D  DEF - Defesa");
		System.out.println("M  MAG - Magia");
		System.out.println("R  RES - Resistência");
		System.out.println("V  VEL - Velocidade");
		System.out.println("L  LCK - Sorte");
		System.out.println();
		System.out.println("↑ bônus  |  ↓ penalidade  |  -- sem efeito");
		System.out.println("Quantidade de setas = intensidade");
		System.out.println("(n) = turnos restantes");
		System.out.println("VEL = efeito na próxima rodada");
		System.out.println("⇄ = ATK/MAG e DEF/RES trocados");
		System.out.println(feedbackMessage == null ? "" : feedbackMessage);
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	public void showEnemyAnalysis(String scenarioName, BattleType battleType, EnemyBattleSlot slot) {
		Objects.requireNonNull(slot, "Enemy slot cannot be null.");

		Enemy enemy = slot.getEnemy();
		List<String> descriptionLines = ConsoleUI.wrapText(enemy.getDescription());
		int currentPage = 0;
		String feedbackMessage = null;

		while (true) {
			int firstLine = currentPage * 4;
			int lastLine = Math.min(firstLine + 4, descriptionLines.size());

			ConsoleUI.clearScreen();
			ConsoleUI.displayHeading(scenarioName, headingQualifier(battleType) + " - Análise");

			System.out.println();
			System.out.println(slot.getDisplayName());
			System.out.println("Rank: " + enemy.getRank());
			System.out.println("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
			System.out.println("ATK: " + enemy.getAttack() + "    DEF: " + enemy.getDefense());
			System.out.println("MAG: " + enemy.getMagic() + "    RES: " + enemy.getResistance());
			System.out.println("VEL: " + enemy.getSpeed() + "    LCK: " + enemy.getLuck());
			System.out.println();
			System.out.println("Descrição");

			for (int index = firstLine; index < lastLine; index++) {
				System.out.println(descriptionLines.get(index));
			}
			for (int index = lastLine - firstLine; index < 4; index++) {
				System.out.println();
			}

			StringBuilder options = new StringBuilder("[0] Voltar");
			if (currentPage > 0) {
				options.append("    [A] Anterior");
			}
			if (lastLine < descriptionLines.size()) {
				options.append("    [P] Próxima");
			}
			System.out.println(feedbackMessage == null ? "" : feedbackMessage);
			System.out.println(options);

			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine().toUpperCase(Locale.ROOT);
			feedbackMessage = null;

			switch (choice) {
			case "0" -> {
				return;
			}
			case "A" -> {
				if (currentPage > 0) {
					currentPage--;
				} else {
					feedbackMessage = "Opção inválida.";
				}
			}
			case "P" -> {
				if (lastLine < descriptionLines.size()) {
					currentPage++;
				} else {
					feedbackMessage = "Opção inválida.";
				}
			}
			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	public static void displayBattleIntroduction() {
		int lines = 17;
		int columns = 77;
		char block = '▒';
		int blockPerCicle = 3;

		List<Integer> position = new ArrayList<>(lines * columns);
		for (int l = 1; l <= lines; l++) {
			for (int c = 1; c <= columns; c++) {
				position.add((l * 100) + c);
			}
		}

		Collections.shuffle(position);

		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < position.size(); i++) {
			int pos = position.get(i);
			int line = pos / 100;
			int column = pos % 100;

			buffer.append(String.format("\033[%d;%dH%c", line, column, block));

			if ((i + 1) % blockPerCicle == 0 || (i + 1) == position.size()) {
				System.out.print(buffer.toString());
				System.out.flush();
				buffer.setLength(0);

				ConsoleUI.pause(1);
			}
		}
	}
}
