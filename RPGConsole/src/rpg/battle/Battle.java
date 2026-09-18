package rpg.battle;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import rpg.characters.CharacterAttribute;
import rpg.characters.Enemy;
import rpg.characters.GameCharacter;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.equipment.WeaponDefinition;
import rpg.skills.SkillDefinition;
import rpg.skills.SkillEffectDefinition;
import rpg.skills.SkillEffectType;
import rpg.skills.SkillExecutionPhase;
import rpg.skills.SkillTargetType;
import rpg.ui.AllyTargetMenu;
import rpg.ui.BattleItemMenu;
import rpg.ui.BattleTurnOrderScreen;
import rpg.ui.CombatEffectFormatter;
import rpg.ui.ConsoleInput;
import rpg.ui.ConsoleUI;
import rpg.ui.EnemyTargetMenu;
import rpg.ui.SkillDetailFormatter;

// Controls a battle between a party and an enemy group.
public class Battle {

	private static final long PACING_PAUSE_MILLISECONDS = 1500L;
	private static final int MINIMUM_STATUS_NAME_WIDTH = 8;
	private static final int SLOT_LABEL_WIDTH = 4;
	private static final int ACTION_MENU_COLUMN_WIDTH = 15;

	private static final String EFFECTS_LABEL = "Efeitos:";

	private final Set<String> analyzedEnemyIds;
	private BattleEscapeService escapeService;
	private BattleTurnOrderGenerator turnOrderGenerator;
	private BattleTurnOrderScreen turnOrderScreen;
	private BattleTurnOrder currentTurnOrder;
	private int currentRoundNumber;
	private AllyTargetMenu allyTargetMenu;
	private PlayableCharacter fleeingCharacter;
	private PlayableCharacter finishingCharacter;
	private PlayableCharacter lastDefeatedCharacter;
	private Party party;
	private EnemyTargetMenu enemyTargetMenu;
	private EnemyBattleGroup enemyGroup;
	private ConsoleInput consoleInput;
	private final Random random;
	private final DamageCalculator damageCalculator;
	private final SkillExecutor skillExecutor;
	private SkillExecution pendingPostActionExecution;
	private boolean partyFled;
	private final BattleItemMenu battleItemMenu;

	public Battle(Party party, EnemyBattleGroup enemyGroup, ConsoleInput consoleInput, Random random) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.enemyGroup = Objects.requireNonNull(enemyGroup, "Enemy battle group cannot be null.");
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");
		this.damageCalculator = new DamageCalculator();
		this.skillExecutor = new SkillExecutor(
				damageCalculator,
				new RecoveryCalculator(),
				new EffectResistanceCalculator(),
				random
		);
		this.pendingPostActionExecution = null;
		this.enemyTargetMenu = new EnemyTargetMenu(consoleInput);
		this.partyFled = false;
		this.battleItemMenu = new BattleItemMenu(party, consoleInput);
		this.allyTargetMenu = new AllyTargetMenu(consoleInput);
		this.turnOrderGenerator = new BattleTurnOrderGenerator(random);
		this.turnOrderScreen = new BattleTurnOrderScreen();
		this.escapeService = new BattleEscapeService(random);
		this.analyzedEnemyIds = new LinkedHashSet<>();
	}

	public BattleResult start() {
		currentRoundNumber = 1;

		displayBattleIntroduction();

		ConsoleUI.pause(PACING_PAUSE_MILLISECONDS, consoleInput);

		while (!party.isDefeated() && !enemyGroup.isDefeated() && !partyFled) {
			System.out.println("\n[BATALHA — RODADA " + currentRoundNumber + "]");

			displayCurrentBattleStatus();

			ConsoleUI.pause(PACING_PAUSE_MILLISECONDS, consoleInput);

			List<BattleTurnEntry> turnEntries = turnOrderGenerator.generate(party, enemyGroup);

			consumePendingSpeedStages();

			currentTurnOrder = new BattleTurnOrder(turnEntries);

			executeCurrentRound();

			currentRoundNumber++;
		}

		clearTemporaryCharacterEffects();

		BattleResult result = determineBattleResult();

		displayBattleResult(result);

		ConsoleUI.pause(PACING_PAUSE_MILLISECONDS, consoleInput);

		return result;
	}

	public List<String> getAnalyzedEnemyIds() {
	    return List.copyOf(analyzedEnemyIds);
	}

	// sobrecarga de métodos
	private void displayDamage(GameCharacter target, int damage) {
		displayDamage(target, target.getName(), damage);
	}

	private void displayDamage(GameCharacter target, String displayName, int damage) {
		System.out.println(
				displayName + " sofreu " + damage + " de dano. HP: " + target.getCurrentHp() + "/" + target.getMaxHp()
						+ "."
		);
	}

	private int calculateStatusNameColumnWidth() {
		int longestPartyName = party.getMembers().stream().mapToInt(member -> member.getName().length()).max()
				.orElse(0);

		int longestEnemyName = enemyGroup.getSlots().stream().mapToInt(slot -> slot.getDisplayName().length()).max()
				.orElse(0);

		return Math.max(MINIMUM_STATUS_NAME_WIDTH, Math.max(longestPartyName, longestEnemyName));
	}

	private String formatResourceValue(int currentValue, int maximumValue) {
		return currentValue + "/" + maximumValue;
	}

	private int calculateHpValueColumnWidth() {
		int longestPartyValue = party.getMembers().stream()
				.mapToInt(member -> formatResourceValue(member.getCurrentHp(), member.getMaxHp()).length()).max()
				.orElse(0);

		int longestEnemyValue = enemyGroup.getSlots().stream().map(EnemyBattleSlot::getEnemy)
				.mapToInt(enemy -> formatResourceValue(enemy.getCurrentHp(), enemy.getMaxHp()).length()).max()
				.orElse(0);

		return Math.max(longestPartyValue, longestEnemyValue);
	}

	private int calculateMpValueColumnWidth() {
		return party.getMembers().stream()
				.mapToInt(member -> formatResourceValue(member.getCurrentMp(), member.getMaxMp()).length()).max()
				.orElse(0);
	}

	private int calculateSpValueColumnWidth() {
		return party.getMembers().stream()
				.mapToInt(member -> formatResourceValue(member.getCurrentSp(), member.getMaxSp()).length()).max()
				.orElse(0);
	}

	private void displayCurrentBattleStatus() {
		int nameColumnWidth = calculateStatusNameColumnWidth();
		int hpValueColumnWidth = calculateHpValueColumnWidth();
		int mpValueColumnWidth = calculateMpValueColumnWidth();
		int spValueColumnWidth = calculateSpValueColumnWidth();

		displayEnemyStatuses(nameColumnWidth, hpValueColumnWidth);

		displayPartyStatus(nameColumnWidth, hpValueColumnWidth, mpValueColumnWidth, spValueColumnWidth);
	}

	private void displayEnemyStatuses(int nameColumnWidth, int hpValueColumnWidth) {
		System.out.println("\n[INIMIGOS]");

		String rowFormat = "%-" + SLOT_LABEL_WIDTH + "s %-" + nameColumnWidth + "s  HP: %-" + hpValueColumnWidth
				+ "s%s%n";

		List<EnemyBattleSlot> slots = enemyGroup.getSlots();

		for (int index = 0; index < slots.size(); index++) {
			EnemyBattleSlot slot = slots.get(index);
			Enemy enemy = slot.getEnemy();

			String slotLabel = "[E" + (index + 1) + "]";
			String condition = slot.isAlive() ? "" : "  DERROTADO";

			System.out.printf(
					rowFormat,
					slotLabel,
					slot.getDisplayName(),
					formatResourceValue(enemy.getCurrentHp(), enemy.getMaxHp()),
					condition
			);

			displayActiveEffects(enemy);
		}
	}

	private void displayPartyStatus(
			int nameColumnWidth,
			int hpValueColumnWidth,
			int mpValueColumnWidth,
			int spValueColumnWidth
	) {
		System.out.println("\n[GRUPO]");

		String rowFormat = "%-" + SLOT_LABEL_WIDTH + "s %-" + nameColumnWidth + "s  HP: %-" + hpValueColumnWidth
				+ "s  MP: %-" + mpValueColumnWidth + "s  SP: %-" + spValueColumnWidth + "s%s%n";

		List<PlayableCharacter> members = party.getMembers();

		for (int index = 0; index < members.size(); index++) {
			PlayableCharacter member = members.get(index);

			String slotLabel = "[A" + (index + 1) + "]";
			String condition = member.isAlive() ? "" : "  DERROTADO";

			System.out.printf(
					rowFormat,
					slotLabel,
					member.getName(),
					formatResourceValue(member.getCurrentHp(), member.getMaxHp()),
					formatResourceValue(member.getCurrentMp(), member.getMaxMp()),
					formatResourceValue(member.getCurrentSp(), member.getMaxSp()),
					condition
			);

			displayActiveEffects(member);
		}
	}

	private void displayActiveEffects(GameCharacter character) {
		List<String> effectLines = CombatEffectFormatter.formatActiveEffectLines(character);

		String statusIndent = " ".repeat(SLOT_LABEL_WIDTH + 1);

		String firstLinePrefix = statusIndent + EFFECTS_LABEL + " ";

		String continuationIndent = " ".repeat(firstLinePrefix.length());

		for (int index = 0; index < effectLines.size(); index++) {
			String prefix = index == 0 ? firstLinePrefix : continuationIndent;

			System.out.println(prefix + effectLines.get(index));
		}
	}

	private void displayBattleResult(BattleResult result) {
		switch (result) {
		case FLED -> {
			String subject = getBattleResultSubject(fleeingCharacter);

			String verb = hasMultiplePartyMembers() ? " fugiram da batalha!" : " fugiu da batalha!";

			System.out.println("\n" + subject + verb);
		}

		case VICTORY -> {
			String subject = getBattleResultSubject(finishingCharacter);

			String verb = hasMultiplePartyMembers() ? " derrotaram " : " derrotou ";

			if (enemyGroup.size() == 1) {
				EnemyBattleSlot slot = enemyGroup.getSlots().get(0);

				System.out.println("\n" + subject + verb + slot.getDisplayName() + "!");
			} else {
				System.out.println("\n" + subject + verb + "o grupo inimigo!");
			}
		}

		case DEFEAT -> {
			String subject = getBattleResultSubject(lastDefeatedCharacter);

			String ending = hasMultiplePartyMembers() ? " foram derrotados!" : " foi derrotado!";

			System.out.println("\n" + subject + ending);
		}
		}
	}

	private void displayEnemyAnalysis(EnemyBattleSlot slot) {
		Enemy analyzedEnemy = slot.getEnemy();

		System.out.println("\n[ANÁLISE — " + slot.getDisplayName().toUpperCase() + "]");

		System.out.println();
		System.out.println("Rank: " + analyzedEnemy.getRank());

		System.out.println();
		System.out.println(analyzedEnemy.getDescription());

		System.out.println();
		System.out
				.printf("%-5s%s%n", "HP:", formatResourceValue(analyzedEnemy.getCurrentHp(), analyzedEnemy.getMaxHp()));

		System.out.printf("%-5s%d%n", "ATK:", analyzedEnemy.getAttack());
		System.out.printf("%-5s%d%n", "DEF:", analyzedEnemy.getDefense());
	}

	private void displayCurrentTurn(BattleTurnEntry actingEntry) {
		System.out.println("\nVez de " + actingEntry.getDisplayName() + "!");

		Optional<BattleTurnEntry> nextEntry = currentTurnOrder.getNextLivingEntry();

		if (nextEntry.isPresent()) {
			System.out.println("A seguir: " + nextEntry.get().getDisplayName());
		} else {
			System.out.println("A seguir: próxima rodada");
		}
	}

	private void displayBattleIntroduction() {
		System.out.println("\nO grupo foi atacado!");

		ConsoleUI.pause(PACING_PAUSE_MILLISECONDS, consoleInput);

		// Temporarily commented to test battle flow. Might remove this method entirely.
		/*
		 * System.out.println("\nBatalha contra:");
		 * 
		 * for (EnemyBattleSlot slot : enemyGroup.getSlots()) {
		 * 
		 * System.out.println("- " + slot.getDisplayName()); }
		 */
	}

	private void executeEnemyTurn(BattleTurnEntry actingEntry) {
		Enemy attackingEnemy = (Enemy) actingEntry.getCharacter();

		PlayableCharacter target = selectEnemyTarget();

		int damage = applyBasicAttackDamage(attackingEnemy, target);

		System.out.println("\n" + actingEntry.getDisplayName() + " atacou " + target.getName() + "!");

		displayDamage(target, damage);

		if (!target.isAlive()) {
			lastDefeatedCharacter = target;
		}
	}

	private void executeCharacterTurn(BattleTurnEntry actingEntry) {
		PlayableCharacter actingCharacter = (PlayableCharacter) actingEntry.getCharacter();
		boolean validAction = false;

		while (!validAction) {
			displayCharacterActionMenu();

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "1" -> {
				validAction = executeBasicAttack(actingCharacter);

				if (!validAction) {
					displayCurrentTurn(actingEntry);
				}
			}

			case "2" -> {
				actingCharacter.defend();

				System.out.println(actingCharacter.getName() + " assumiu uma postura defensiva!");

				validAction = true;
			}

			case "3" -> {
				validAction = executeSkillMenu(actingCharacter);

				if (!validAction) {
					displayCurrentTurn(actingEntry);
				}
			}

			case "4" -> {
				validAction = battleItemMenu.open(actingCharacter);

				if (!validAction) {
					displayCurrentTurn(actingEntry);
				}
			}

			case "5" -> {
				openTacticalMenu();
				displayCurrentTurn(actingEntry);
			}

			case "6" -> {
				executeEscapeAttempt(actingCharacter);
				validAction = true;
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private void displayCharacterActionMenu() {
		String rowFormat = "%-" + ACTION_MENU_COLUMN_WIDTH + "s%-" + ACTION_MENU_COLUMN_WIDTH + "s%s%n";

		System.out.println();

		System.out.printf(rowFormat, "[1] Atacar", "[2] Defender", "[3] Especial");

		System.out.printf(rowFormat, "[4] Itens", "[5] Tática", "[6] Fugir");

		System.out.print("> ");
	}

	private void openTacticalMenu() {
		while (true) {
			displayTacticalMenu();

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "0" -> {
				return;
			}

			case "1" -> {
				displayCurrentBattleStatus();
				waitForBattleScreenBack();
			}

			case "2" -> {
				if (executeEnemyAnalysis()) {
					waitForBattleScreenBack();
				}
			}

			case "3" -> {
				turnOrderScreen.show(currentTurnOrder, currentRoundNumber);
				waitForBattleScreenBack();
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private void displayTacticalMenu() {
		System.out.println("\n[TÁTICA]");

		System.out.println();
		System.out.println("[1] Situação da batalha");
		System.out.println("[2] Analisar inimigo");
		System.out.println("[3] Ordem da rodada");

		System.out.println();
		System.out.println("[0] Voltar");
		System.out.print("> ");
	}

	private void waitForBattleScreenBack() {
		while (true) {
			System.out.println("\n[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return;
			}

			System.out.println("Opção inválida.");
		}
	}

	private boolean executeSkillMenu(PlayableCharacter actingCharacter) {
		List<SkillDefinition> skills = actingCharacter.getUnlockedSkillDefinitions();

		if (skills.isEmpty()) {
			System.out.println("Nenhuma habilidade disponível.");
			return false;
		}

		while (true) {
			System.out.println("\nEscolha uma habilidade:");
			System.out.println(
					"\n[" + actingCharacter.getName().toUpperCase() + "] MP: " + actingCharacter.getCurrentMp() + "/"
							+ actingCharacter.getMaxMp() + " | SP: " + actingCharacter.getCurrentSp() + "/"
							+ actingCharacter.getMaxSp() + "\n"
			);
			for (int index = 0; index < skills.size(); index++) {
				SkillDefinition skill = skills.get(index);

				System.out.println(
						"[" + (index + 1) + "] " + skill.getName() + " | Custo: "
								+ SkillDetailFormatter.formatCost(skill)
				);
			}

			System.out.println("\n[0] Voltar");
			System.out.print("> ");

			String choice = consoleInput.readLine();

			if (choice.equals("0")) {
				return false;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= skills.size()) {
					System.out.println("Habilidade inválida.");
					continue;
				}

				SkillDefinition selectedSkill = skills.get(selectedIndex);

				if (!confirmSkillUse(selectedSkill)) {
					continue;
				}

				if (actingCharacter.getCurrentMp() < selectedSkill.getMpCost()) {
					System.out.println("MP insuficiente!");
					continue;
				}

				if (actingCharacter.getCurrentSp() < selectedSkill.getSpCost()) {
					System.out.println("SP insuficiente!");
					continue;
				}

				if (executeSelectedSkill(actingCharacter, selectedSkill)) {
					return true;
				}
			} catch (NumberFormatException exception) {
				System.out.println("Digite o número de uma habilidade.");
			}
		}
	}

	private boolean executeBasicAttack(PlayableCharacter actingCharacter) {
		Optional<EnemyBattleSlot> selectedSlot = enemyTargetMenu.selectTarget(enemyGroup);

		if (selectedSlot.isEmpty()) {
			return false;
		}

		EnemyBattleSlot targetSlot = selectedSlot.get();

		Enemy target = targetSlot.getEnemy();

		Optional<WeaponDefinition> equippedWeapon = actingCharacter.getEquipment().getWeapon();

		int damage;

		if (equippedWeapon.isPresent()) {
			WeaponDefinition weapon = equippedWeapon.get();

			System.out.println(
					"\n" + actingCharacter.getName() + " executou " + weapon.getNormalAttackName() + " com sua arma!"
			);

			damage = applyBasicAttackDamage(
					actingCharacter,
					target,
					weapon.getDamageType(),
					weapon.getBaseWeaponDamage()
			);
		} else {
			System.out.println("\n" + actingCharacter.getName() + " atacou " + targetSlot.getDisplayName() + "!");

			damage = applyBasicAttackDamage(actingCharacter, target);
		}

		displayDamage(target, targetSlot.getDisplayName(), damage);

		if (enemyGroup.isDefeated()) {
			finishingCharacter = actingCharacter;
		}

		return true;
	}

	private void executeTurn(BattleTurnEntry actingEntry) {
		switch (actingEntry.getSide()) {
		case PARTY -> executeCharacterTurn(actingEntry);

		case ENEMY -> executeEnemyTurn(actingEntry);
		}
	}

	private void executeEscapeAttempt(PlayableCharacter actingCharacter) {
		EscapeAttemptResult result = escapeService.attemptEscape(actingCharacter, enemyGroup);

		switch (result) {
		case LUCK_SUCCESS -> {
			System.out.println("\nA sorte sorriu para " + actingCharacter.getName() + "!");

			completeEscape(actingCharacter);
		}

		case ATTRIBUTE_SUCCESS -> {
			System.out.println("\n" + actingCharacter.getName() + " encontrou uma abertura para fugir!");

			completeEscape(actingCharacter);
		}

		case FAILURE -> System.out
				.println("\n" + actingCharacter.getName() + " tentou fugir, mas os inimigos bloquearam o caminho!");
		}
	}

	private boolean executeEnemyAnalysis() {
	    Optional<EnemyBattleSlot> selectedSlot =
	            enemyTargetMenu.selectTarget(enemyGroup, "Analisar inimigo");

	    if (selectedSlot.isEmpty()) {
	        return false;
	    }

	    EnemyBattleSlot enemySlot = selectedSlot.orElseThrow();

	    displayEnemyAnalysis(enemySlot);

	    analyzedEnemyIds.add(enemySlot.getEnemy().getId());

	    return true;
	}

	private void executeCurrentRound() {
		while (!currentTurnOrder.isComplete() && !party.isDefeated() && !enemyGroup.isDefeated() && !partyFled) {
			Optional<BattleTurnEntry> currentEntry = currentTurnOrder.getCurrentEntry();

			if (currentEntry.isEmpty()) {
				return;
			}

			BattleTurnEntry actingEntry = currentEntry.get();

			displayCurrentTurn(actingEntry);
			ConsoleUI.pause(PACING_PAUSE_MILLISECONDS, consoleInput);
			pendingPostActionExecution = null;
			executeTurn(actingEntry);

			actingEntry.getCharacter().finishConsumedTurn();

			if (pendingPostActionExecution != null) {
				List<SkillEffectResult> postActionResults = skillExecutor.executePostAction(pendingPostActionExecution);

				displaySkillEffectResults(postActionResults);
				updateFinishingCharacter(pendingPostActionExecution);
			}

			ConsoleUI.pause(PACING_PAUSE_MILLISECONDS, consoleInput);

			currentTurnOrder.advance();
		}
	}

	private boolean executeSelectedSkill(PlayableCharacter actingCharacter, SkillDefinition skill) {
		Optional<SkillExecutionContext> selectedContext = selectSkillTargets(actingCharacter, skill);

		if (selectedContext.isEmpty()) {
			return false;
		}

		SkillExecution execution = skillExecutor.beginExecution(skill, selectedContext.get());

		if (!execution.canConsumeTurn()) {
			System.out.println("A habilidade não pode afetar nenhum dos alvos selecionados.");

			return false;
		}

		spendSkillResources(actingCharacter, skill);

		System.out.println("\n" + actingCharacter.getName() + " usou " + skill.getName() + "!");

		displaySkillEffectResults(execution.getEffectResults());

		if (!skill.getEffectsByPhase(SkillExecutionPhase.POST_ACTION).isEmpty()) {
			pendingPostActionExecution = execution;
		}

		updateFinishingCharacter(execution);

		return true;
	}

	private Optional<SkillExecutionContext>
			selectSkillTargets(PlayableCharacter actingCharacter, SkillDefinition skill) {
		Map<SkillTargetType, List<GameCharacter>> targetsByType = new EnumMap<>(SkillTargetType.class);

		EnumSet<SkillTargetType> selectedTargetTypes = EnumSet.noneOf(SkillTargetType.class);

		for (SkillEffectDefinition effect : skill.getEffects()) {
			SkillTargetType targetType = effect.getTargetType();

			if (targetType == SkillTargetType.SELF || !selectedTargetTypes.add(targetType)) {
				continue;
			}

			Optional<List<GameCharacter>> selectedTargets = selectTargetsForType(actingCharacter, skill, targetType);

			if (selectedTargets.isEmpty()) {
				return Optional.empty();
			}

			targetsByType.put(targetType, selectedTargets.get());
		}

		int weaponBaseDamage = actingCharacter.getEquipment().getWeapon().map(WeaponDefinition::getBaseWeaponDamage)
				.orElse(0);

		return Optional.of(new SkillExecutionContext(actingCharacter, targetsByType, weaponBaseDamage));
	}

	private Optional<List<GameCharacter>>
			selectTargetsForType(PlayableCharacter actingCharacter, SkillDefinition skill, SkillTargetType targetType) {
		return switch (targetType) {
		case SELF -> throw new IllegalArgumentException("SELF targets are supplied automatically.");

		case SINGLE_ALLY -> selectSingleAllyTarget(skill);

		case ALL_ALLIES -> Optional.of(party.getMembers().stream().map(member -> (GameCharacter) member).toList());

		case ALL_BUT_SELF -> Optional.of(
				party.getMembers().stream().filter(member -> member != actingCharacter)
						.map(member -> (GameCharacter) member).toList()
		);

		case RANDOM_ALLY -> selectRandomAllyTarget(skill);

		case SINGLE_ENEMY ->
			enemyTargetMenu.selectTarget(enemyGroup).map(slot -> List.<GameCharacter>of(slot.getEnemy()));

		case ALL_ENEMIES ->
			Optional.of(enemyGroup.getLivingSlots().stream().map(slot -> (GameCharacter) slot.getEnemy()).toList());

		case RANDOM_ENEMY -> selectRandomEnemyTarget();
		};
	}

	private Optional<List<GameCharacter>> selectSingleAllyTarget(SkillDefinition skill) {
		Optional<PlayableCharacter> selectedTarget;

		if (targetsDefeatedAlly(skill, SkillTargetType.SINGLE_ALLY)) {
			selectedTarget = allyTargetMenu.selectDefeatedTarget(party);
		} else {
			selectedTarget = allyTargetMenu.selectLivingTarget(party);
		}

		return selectedTarget.map(target -> List.<GameCharacter>of(target));
	}

	private Optional<List<GameCharacter>> selectRandomAllyTarget(SkillDefinition skill) {
		List<PlayableCharacter> candidates = targetsDefeatedAlly(skill, SkillTargetType.RANDOM_ALLY)
				? party.getDefeatedMembers()
				: party.getLivingMembers();

		if (candidates.isEmpty()) {
			System.out.println("Não há aliados disponíveis para essa habilidade.");

			return Optional.empty();
		}

		PlayableCharacter selectedTarget = candidates.get(random.nextInt(candidates.size()));

		return Optional.of(List.of(selectedTarget));
	}

	private Optional<List<GameCharacter>> selectRandomEnemyTarget() {
		List<EnemyBattleSlot> candidates = enemyGroup.getLivingSlots();

		if (candidates.isEmpty()) {
			throw new IllegalStateException("No living enemy is available.");
		}

		Enemy selectedTarget = candidates.get(random.nextInt(candidates.size())).getEnemy();

		return Optional.of(List.of(selectedTarget));
	}

	private boolean targetsDefeatedAlly(SkillDefinition skill, SkillTargetType targetType) {
		return skill.getEffects().stream().anyMatch(
				effect -> effect.getTargetType() == targetType && effect.getEffectType() == SkillEffectType.REVIVE
		);
	}

	private void displaySkillEffectResults(List<SkillEffectResult> results) {
		for (SkillEffectResult result : results) {
			GameCharacter target = result.getTarget();

			String targetName = getCharacterDisplayName(target);

			switch (result.getOutcome()) {
			case RESISTED -> System.out.println(targetName + " resistiu ao efeito!");

			case NOT_TRIGGERED -> System.out.println("O efeito adicional sobre " + targetName + " não foi ativado.");

			case APPLIED -> displayAppliedSkillEffectResult(result, target, targetName);
			}
		}
	}

	private void displayAppliedSkillEffectResult(SkillEffectResult result, GameCharacter target, String targetName) {
		switch (result.getEffect().getEffectType()) {
		case DAMAGE -> displayDamage(target, targetName, result.getAmount());

		case RECOIL_DAMAGE -> displayDamage(target, targetName, result.getAmount());

		case HP_RECOVERY -> System.out.println(
				targetName + " recuperou " + result.getAmount() + " de HP. HP atual: " + target.getCurrentHp() + "/"
						+ target.getMaxHp() + "."
		);

		case MP_RECOVERY -> System.out.println(
				targetName + " recuperou " + result.getAmount() + " de MP. MP atual: " + target.getCurrentMp() + "/"
						+ target.getMaxMp() + "."
		);

		case SP_RECOVERY -> System.out.println(
				targetName + " recuperou " + result.getAmount() + " de SP. SP atual: " + target.getCurrentSp() + "/"
						+ target.getMaxSp() + "."
		);

		case REVIVE -> System.out.println(targetName + " foi revivido com " + result.getAmount() + " HP!");

		case ATTRIBUTE_STAGE -> displayAttributeStageResult(result, targetName);

		case PURIFICATION -> System.out.println("Os efeitos negativos de " + targetName + " foram purificados.");

		case DISSIPATION -> System.out.println("Os efeitos positivos de " + targetName + " foram dissipados.");

		case SPECIAL_STATE -> displaySpecialStateResult(target, targetName);

		case DAMAGE_POWER_MODIFIER -> {
		}
		}
	}

	private void displayAttributeStageResult(SkillEffectResult result, String targetName) {
		SkillEffectDefinition effect = result.getEffect();

		String direction = effect.getStages() > 0 ? " aumentou." : " diminuiu.";

		System.out.println(getAttributeDisplayName(effect.getAttribute()) + " de " + targetName + direction);
	}

	private String getAttributeDisplayName(CharacterAttribute attribute) {
		return switch (attribute) {
		case ATK -> "O Ataque";
		case DEF -> "A Defesa";
		case MAG -> "A Magia";
		case RES -> "A Resistência";
		case VEL -> "A Velocidade";
		case LCK -> "A Sorte";
		};
	}

	private void displaySpecialStateResult(GameCharacter target, String targetName) {
		if (target.hasDistortedAttributes()) {
			System.out.println("Os atributos de " + targetName + " foram distorcidos.");
		} else {
			System.out.println("Os atributos de " + targetName + " voltaram ao normal.");
		}
	}

	private String getCharacterDisplayName(GameCharacter character) {
		for (EnemyBattleSlot slot : enemyGroup.getSlots()) {
			if (slot.getEnemy() == character) {
				return slot.getDisplayName();
			}
		}

		return character.getName();
	}

	private void updateFinishingCharacter(SkillExecution execution) {
		if (enemyGroup.isDefeated()) {
			finishingCharacter = execution.getContext().getActingCharacter();
		}
	}

	private boolean confirmSkillUse(SkillDefinition skill) {
		System.out.println("\n" + SkillDetailFormatter.formatDetails(skill));

		while (true) {
			System.out.println("\n[1] Usar | [0] Voltar");

			System.out.print("> ");

			String confirmation = consoleInput.readLine();

			switch (confirmation) {
			case "1" -> {
				return true;
			}

			case "0" -> {
				return false;
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private BattleResult determineBattleResult() {
		if (partyFled) {
			return BattleResult.FLED;
		}

		if (enemyGroup.isDefeated()) {
			return BattleResult.VICTORY;
		}

		return BattleResult.DEFEAT;
	}

	private PlayableCharacter selectEnemyTarget() {
		return party.getLivingMembers().stream().min(Comparator.comparingInt(PlayableCharacter::getCurrentHp))
				.orElseThrow(() -> new IllegalStateException("No living party member is available."));
	}

	private boolean hasMultiplePartyMembers() {
		return party.size() > 1;
	}

	private void spendSkillResources(PlayableCharacter actingCharacter, SkillDefinition skill) {
		if (skill.getMpCost() > 0) {
			actingCharacter.spendMp(skill.getMpCost());
		}

		if (skill.getSpCost() > 0) {
			actingCharacter.spendSp(skill.getSpCost());
		}
	}

	private void completeEscape(PlayableCharacter actingCharacter) {
		fleeingCharacter = actingCharacter;
		partyFled = true;
	}

	private String getBattleResultSubject(PlayableCharacter character) {
		if (party.size() == 1) {
			return character.getName();
		}

		return character.getName() + " e seus companheiros";
	}

	private void consumePendingSpeedStages() {
		for (PlayableCharacter member : party.getMembers()) {
			member.consumePendingSpeedStages();
		}

		for (EnemyBattleSlot slot : enemyGroup.getSlots()) {
			slot.getEnemy().consumePendingSpeedStages();
		}
	}

	private void clearTemporaryCharacterEffects() {
		for (PlayableCharacter member : party.getMembers()) {
			member.clearTemporaryBattleEffects();
		}

		for (EnemyBattleSlot slot : enemyGroup.getSlots()) {
			slot.getEnemy().clearTemporaryBattleEffects();
		}
	}

	private int applyBasicAttackDamage(GameCharacter attacker, GameCharacter target) {
		return applyBasicAttackDamage(attacker, target, DamageType.PHYSICAL, 0);
	}

	private int applyBasicAttackDamage(
			GameCharacter attacker,
			GameCharacter target,
			DamageType damageType,
			int weaponBaseDamage
	) {
		double calculatedDamage = damageCalculator
				.calculateDamage(attacker, target, damageType, 0, weaponBaseDamage, 0);

		return target.takeDamage(calculatedDamage);
	}
}
