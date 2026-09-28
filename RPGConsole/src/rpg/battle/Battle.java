package rpg.battle;

import java.util.ArrayList;
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
import java.util.function.BiConsumer;

import rpg.characters.CharacterAttribute;
import rpg.characters.Enemy;
import rpg.characters.GameCharacter;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.items.RecoveryItem;
import rpg.items.equipment.WeaponDefinition;
import rpg.skills.CombatSkillDefinition;
import rpg.skills.EnemySkillDefinition;
import rpg.skills.SkillDefinition;
import rpg.skills.SkillEffectDefinition;
import rpg.skills.SkillEffectType;
import rpg.skills.SkillExecutionPhase;
import rpg.skills.SkillTargetType;
import rpg.ui.AllyTargetMenu;
import rpg.ui.BattleItemMenu;
import rpg.ui.BattleScreen;
import rpg.ui.BattleTurnOrderScreen;
import rpg.ui.ConsoleInput;
import rpg.ui.ConsoleUI;
import rpg.ui.EnemyTargetMenu;

// Controls a battle between a party and an enemy group.
public class Battle {

	private static final long PACING_PAUSE_MILLISECONDS = 1500L;

	private final BattleType battleType;
	private final String scenarioName;
	private final BattleScreen battleScreen;
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
	private final DamageCalculator damageCalculator;
	private final SkillExecutor skillExecutor;
	private SkillUseState pendingPostActionExecution;
	private RecoveryItem pendingPostActionItem;
	private PlayableCharacter pendingPostActionTarget;
	private boolean partyFled;
	private final BattleItemMenu battleItemMenu;
	private final Random random;

	public Battle(
			Party party,
			EnemyBattleGroup enemyGroup,
			ConsoleInput consoleInput,
			Random random,
			BattleType battleType,
			String scenarioName
	) {
		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.enemyGroup = Objects.requireNonNull(enemyGroup, "Enemy battle group cannot be null.");
		this.consoleInput = Objects.requireNonNull(consoleInput, "Console input cannot be null.");
		this.damageCalculator = new DamageCalculator();
		this.skillExecutor = new SkillExecutor(
				damageCalculator,
				new RecoveryCalculator(),
				new EffectResistanceCalculator(),
				this.random
		);
		this.pendingPostActionExecution = null;
		this.partyFled = false;
		this.battleType = Objects.requireNonNull(battleType, "Battle kind cannot be null.");
		this.battleScreen = new BattleScreen(consoleInput);
		this.scenarioName = Objects.requireNonNull(scenarioName, "Scenario name cannot be null.");
		this.battleItemMenu = new BattleItemMenu(
				party,
				consoleInput,
				battleScreen,
				scenarioName,
				battleType,
				this::displayActionMessages,
				(targets, feedback) -> battleScreen.displayAllyTargetSelection(
						scenarioName,
						battleType,
						enemyGroup,
						party,
						currentRoundNumber,
						"Escolha o aliado:",
						targets,
						feedback
				),
				this::schedulePostActionItemStage
		);
		this.allyTargetMenu = new AllyTargetMenu(consoleInput);
		this.turnOrderScreen = new BattleTurnOrderScreen();
		this.turnOrderGenerator = new BattleTurnOrderGenerator(this.random);
		this.escapeService = new BattleEscapeService(this.random);
		this.analyzedEnemyIds = new LinkedHashSet<>();
		this.enemyTargetMenu = new EnemyTargetMenu(consoleInput, battleScreen, party, scenarioName, battleType);
	}

	public BattleResult start() {
		currentRoundNumber = 1;

		BattleScreen.displayBattleIntroduction();

		while (!party.isDefeated() && !enemyGroup.isDefeated() && !partyFled) {
			battleScreen.displayPanel(scenarioName, battleType, enemyGroup, party);
			System.out.println();
			System.out.println("Rodada " + currentRoundNumber);

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

	private void displayPlayerTurn(BattleTurnEntry actingEntry, String feedbackMessage) {
		String nextName = currentTurnOrder.getNextLivingEntry().map(BattleTurnEntry::getDisplayName)
				.orElse("próxima rodada");

		battleScreen.displayPlayerTurn(
				scenarioName,
				battleType,
				enemyGroup,
				party,
				currentRoundNumber,
				actingEntry.getDisplayName(),
				nextName,
				feedbackMessage
		);
	}

	private void displayActionMessages(List<String> messages) {
		battleScreen.displayMessages(scenarioName, battleType, enemyGroup, party, currentRoundNumber, messages);
	}

	private void displayBattleResult(BattleResult result) {
		switch (result) {
		case FLED -> {
			String subject = getBattleResultSubject(fleeingCharacter);

			String verb = hasMultiplePartyMembers() ? " fugiram da batalha!" : " fugiu da batalha!";

			displayActionMessages(List.of(subject + verb));
		}

		case VICTORY -> {
			String subject = getBattleResultSubject(finishingCharacter);

			String verb = hasMultiplePartyMembers() ? " derrotaram " : " derrotou ";

			if (enemyGroup.size() == 1) {
				EnemyBattleSlot slot = enemyGroup.getSlots().get(0);

				displayActionMessages(List.of(subject + verb + slot.getDisplayName() + "!"));
			} else {
				displayActionMessages(List.of(subject + verb + "o grupo inimigo!"));
			}
		}

		case DEFEAT -> {
			String subject = getBattleResultSubject(lastDefeatedCharacter);

			String ending = hasMultiplePartyMembers() ? " foram derrotados!" : " foi derrotado!";

			displayActionMessages(List.of(subject + ending));
		}
		}
	}

	private void executeEnemyTurn(BattleTurnEntry actingEntry) {
		Enemy actingEnemy = (Enemy) actingEntry.getCharacter();

		List<EnemySkillDefinition> usableSkills = actingEnemy.getSkillDefinitions().stream()
				.filter(skill -> hasResourcesForSkill(actingEnemy, skill)).toList();

		int selectedActionIndex = random.nextInt(usableSkills.size() + 1);

		if (selectedActionIndex > 0) {
			EnemySkillDefinition selectedSkill = usableSkills.get(selectedActionIndex - 1);

			if (executeEnemySkill(actingEntry, actingEnemy, selectedSkill)) {
				return;
			}
		}

		executeEnemyBasicAttack(actingEntry, actingEnemy);
	}

	private boolean hasResourcesForSkill(Enemy actingEnemy, EnemySkillDefinition skillDefinition) {
		return actingEnemy.getCurrentMp() >= skillDefinition.getMpCost()
				&& actingEnemy.getCurrentSp() >= skillDefinition.getSpCost();
	}

	private boolean
			executeEnemySkill(BattleTurnEntry actingEntry, Enemy actingEnemy, EnemySkillDefinition skillDefinition) {
		SkillExecutionContext context = createEnemySkillExecutionContext(actingEnemy, skillDefinition);

		List<PlayableCharacter> livingPartyBeforeExecution = List.copyOf(party.getLivingMembers());

		SkillUseState execution = skillExecutor.beginExecution(skillDefinition, context);

		if (!execution.canConsumeTurn()) {
			return false;
		}

		spendSkillResources(actingEnemy, skillDefinition);

		List<String> messages = new ArrayList<>();

		messages.add(actingEntry.getDisplayName() + " usou " + skillDefinition.getName() + "!");

		messages.addAll(formatSkillEffectResults(execution.getEffectResults()));

		addNewlyDefeatedPartyMessages(livingPartyBeforeExecution, messages);

		displayActionMessages(messages);

		if (!skillDefinition.getEffectsByPhase(SkillExecutionPhase.POST_ACTION).isEmpty()) {
			pendingPostActionExecution = execution;
		}

		return true;
	}

	private void executeEnemyBasicAttack(BattleTurnEntry actingEntry, Enemy attackingEnemy) {
		PlayableCharacter target = selectEnemyTarget();
		int damage = applyBasicAttackDamage(attackingEnemy, target);

		List<String> messages = new ArrayList<>();

		messages.add(actingEntry.getDisplayName() + " atacou " + target.getName() + "!");

		messages.add(target.getName() + " sofreu " + damage + " de dano.");

		if (!target.isAlive()) {
			lastDefeatedCharacter = target;

			if (!party.isDefeated()) {
				messages.add(target.getName() + " chegou a 0 HP!");
			}
		}

		displayActionMessages(messages);
	}

	private void
			addNewlyDefeatedPartyMessages(List<PlayableCharacter> livingPartyBeforeExecution, List<String> messages) {
		for (PlayableCharacter character : livingPartyBeforeExecution) {
			if (character.isAlive()) {
				continue;
			}

			lastDefeatedCharacter = character;

			if (!party.isDefeated()) {
				messages.add(character.getName() + " chegou a 0 HP!");
			}
		}
	}

	private void executeCharacterTurn(BattleTurnEntry actingEntry) {
		PlayableCharacter actingCharacter = (PlayableCharacter) actingEntry.getCharacter();

		boolean validAction = false;
		String feedbackMessage = null;

		while (!validAction) {
			displayPlayerTurn(actingEntry, feedbackMessage);
			feedbackMessage = null;

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "1" -> validAction = executeBasicAttack(actingCharacter);

			case "2" -> {
				actingCharacter.defend();

				displayActionMessages(List.of(actingCharacter.getName() + " assumiu uma postura defensiva!"));

				validAction = true;
			}

			case "3" -> {
				if (actingCharacter.getUnlockedSkillDefinitions().isEmpty()) {
					feedbackMessage = "Nenhuma habilidade disponível.";
				} else {
					validAction = executeSkillMenu(actingCharacter);
				}
			}

			case "4" -> {
				if (battleItemMenu.hasAvailableItems()) {
					validAction = battleItemMenu.open(actingCharacter);
				} else {
					feedbackMessage = "Nenhum item disponível para usar em batalha.";
				}
			}

			case "5" -> openTacticalMenu();

			case "6" -> {
				executeEscapeAttempt(actingCharacter);
				validAction = true;
			}

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	private void openTacticalMenu() {
		String feedbackMessage = null;

		while (true) {
			battleScreen.displayTacticalMenu(
					scenarioName,
					battleType,
					enemyGroup,
					party,
					currentRoundNumber,
					feedbackMessage
			);
			feedbackMessage = null;

			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "0" -> {
				return;
			}

			case "1" -> openStatusMenu();

			case "2" -> executeEnemyAnalysis();

			case "3" ->
				turnOrderScreen.show(currentTurnOrder, currentRoundNumber, scenarioName, battleType, consoleInput);

			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	private boolean executeSkillMenu(PlayableCharacter actingCharacter) {
		List<Optional<SkillDefinition>> skills = actingCharacter.getSkillSlots();

		String feedbackMessage = null;

		while (true) {
			battleScreen.displaySkillSelection(
					scenarioName,
					battleType,
					enemyGroup,
					party,
					actingCharacter,
					skills,
					feedbackMessage
			);
			feedbackMessage = null;

			String choice = consoleInput.readTrimmedLine();

			if (choice.equals("0")) {
				return false;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= skills.size() || skills.get(selectedIndex).isEmpty()) {
					feedbackMessage = "Habilidade inválida.";
					continue;
				}

				SkillDefinition selectedSkill = skills.get(selectedIndex).orElseThrow();

				if (!battleScreen.confirmSkillUse(scenarioName, battleType, enemyGroup, party, selectedSkill)) {
					continue;
				}

				if (actingCharacter.getCurrentMp() < selectedSkill.getMpCost()) {
					feedbackMessage = "MP insuficiente!";
					continue;
				}

				if (actingCharacter.getCurrentSp() < selectedSkill.getSpCost()) {
					feedbackMessage = "SP insuficiente!";
					continue;
				}

				Optional<SkillExecutionContext> selectedContext = selectSkillTargets(actingCharacter, selectedSkill);

				if (selectedContext.isEmpty()) {
					continue; // Voltou da seleção; não consome o turno nem mostra erro.
				}

				if (executeSelectedSkill(actingCharacter, selectedSkill, selectedContext.get())) {
					return true;
				}

				feedbackMessage = "Sem alvo válido para esta habilidade.";
			} catch (NumberFormatException exception) {
				feedbackMessage = "Digite o número de uma habilidade.";
			}
		}
	}

	private boolean executeBasicAttack(PlayableCharacter actingCharacter) {
		Optional<EnemyBattleSlot> selectedSlot = enemyTargetMenu.selectTarget(enemyGroup, currentRoundNumber);

		if (selectedSlot.isEmpty()) {
			return false;
		}

		EnemyBattleSlot targetSlot = selectedSlot.get();
		Enemy target = targetSlot.getEnemy();

		Optional<WeaponDefinition> equippedWeapon = actingCharacter.getEquipment().getWeapon();

		List<String> messages = new ArrayList<>();
		int damage;

		if (equippedWeapon.isPresent()) {
			WeaponDefinition weapon = equippedWeapon.get();

			messages.add(actingCharacter.getName() + " executou " + weapon.getNormalAttackName() + " com sua arma!");

			damage = applyBasicAttackDamage(
					actingCharacter,
					target,
					weapon.getDamageType(),
					weapon.getBaseWeaponDamage()
			);
		} else {
			messages.add(actingCharacter.getName() + " atacou " + targetSlot.getDisplayName() + "!");

			damage = applyBasicAttackDamage(actingCharacter, target);
		}

		messages.add(
				targetSlot.getDisplayName() + " sofreu " + damage + " de dano. HP: " + target.getCurrentHp() + "/"
						+ target.getMaxHp() + "."
		);

		if (!target.isAlive() && !enemyGroup.isDefeated()) {
			messages.add(targetSlot.getDisplayName() + " foi derrotado!");
		}

		if (enemyGroup.isDefeated()) {
			finishingCharacter = actingCharacter;
		}

		displayActionMessages(messages);
		return true;
	}

	private void executeTurn(BattleTurnEntry actingEntry) {
		actingEntry.getCharacter().stopDefending();
		switch (actingEntry.getSide()) {
		case PARTY -> executeCharacterTurn(actingEntry);

		case ENEMY -> executeEnemyTurn(actingEntry);
		}
	}

	private void executeEscapeAttempt(PlayableCharacter actingCharacter) {
		EscapeAttemptResult result = escapeService.attemptEscape(actingCharacter, enemyGroup);

		switch (result) {
		case LUCK_SUCCESS -> {
			completeEscape(actingCharacter);
			displayActionMessages(List.of("A sorte sorriu para " + actingCharacter.getName() + "!"));
		}

		case ATTRIBUTE_SUCCESS -> {
			completeEscape(actingCharacter);
			displayActionMessages(List.of(actingCharacter.getName() + " encontrou uma abertura para fugir!"));
		}

		case FAILURE -> displayActionMessages(
				List.of(actingCharacter.getName() + " tentou fugir, mas os inimigos bloquearam o caminho!")
		);
		}
	}

	private void executeEnemyAnalysis() {
		Optional<EnemyBattleSlot> selectedSlot = enemyTargetMenu
				.selectTarget(enemyGroup, "Analisar inimigo:", currentRoundNumber);

		if (selectedSlot.isEmpty()) {
			return;
		}

		EnemyBattleSlot slot = selectedSlot.get();

		battleScreen.showEnemyAnalysis(scenarioName, battleType, slot);
		analyzedEnemyIds.add(slot.getEnemy().getId());
	}

	private void executeCurrentRound() {
		while (!currentTurnOrder.isComplete() && !party.isDefeated() && !enemyGroup.isDefeated() && !partyFled) {
			Optional<BattleTurnEntry> currentEntry = currentTurnOrder.getCurrentEntry();

			if (currentEntry.isEmpty()) {
				return;
			}

			BattleTurnEntry actingEntry = currentEntry.get();

			if (actingEntry.getSide() == BattleSide.ENEMY) {
				ConsoleUI.pause(PACING_PAUSE_MILLISECONDS, consoleInput);
			}
			pendingPostActionExecution = null;
			pendingPostActionItem = null;
			pendingPostActionTarget = null;
			executeTurn(actingEntry);

			actingEntry.getCharacter().finishConsumedTurn();

			if (pendingPostActionExecution != null) {
				List<PlayableCharacter> livingPartyBeforePostAction = List.copyOf(party.getLivingMembers());

				List<SkillEffectResult> postActionResults = skillExecutor.executePostAction(pendingPostActionExecution);

				List<String> postActionMessages = new ArrayList<>(formatSkillEffectResults(postActionResults));

				addNewlyDefeatedPartyMessages(livingPartyBeforePostAction, postActionMessages);

				if (!postActionMessages.isEmpty()) {
					displayActionMessages(postActionMessages);
				}

				updateFinishingCharacter(pendingPostActionExecution);
			}

			if (pendingPostActionItem != null) {
				if (!pendingPostActionItem.applyAttributeStage(pendingPostActionTarget)) {
					throw new IllegalStateException("Scheduled item stage did not affect its target.");
				}

				displayActionMessages(
						List.of(
								getAttributeDisplayName(pendingPostActionItem.getStageAttribute()) + " de "
										+ pendingPostActionTarget.getName() + " aumentou."
						)
				);
			}

			ConsoleUI.pause(PACING_PAUSE_MILLISECONDS, consoleInput);

			currentTurnOrder.advance();
		}
	}

	private boolean executeSelectedSkill(
			PlayableCharacter actingCharacter,
			SkillDefinition skill,
			SkillExecutionContext selectedContext
	) {

		SkillUseState execution = skillExecutor.beginExecution(skill, selectedContext);

		if (!execution.canConsumeTurn()) {
			return false;
		}

		spendSkillResources(actingCharacter, skill);

		List<String> messages = new ArrayList<>();
		messages.add(actingCharacter.getName() + " usou " + skill.getName() + "!");
		messages.addAll(formatSkillEffectResults(execution.getEffectResults()));
		displayActionMessages(messages);

		if (!skill.getEffectsByPhase(SkillExecutionPhase.POST_ACTION).isEmpty()) {
			pendingPostActionExecution = execution;
		}

		updateFinishingCharacter(execution);

		return true;
	}

	private SkillExecutionContext
			createEnemySkillExecutionContext(Enemy actingEnemy, EnemySkillDefinition skillDefinition) {
		Map<SkillTargetType, List<GameCharacter>> targetsByType = new EnumMap<>(SkillTargetType.class);

		EnumSet<SkillTargetType> resolvedTargetTypes = EnumSet.noneOf(SkillTargetType.class);

		for (SkillEffectDefinition effect : skillDefinition.getEffects()) {
			SkillTargetType targetType = effect.getTargetType();

			if (targetType == SkillTargetType.SELF || !resolvedTargetTypes.add(targetType)) {
				continue;
			}

			targetsByType.put(targetType, resolveEnemySkillTargets(actingEnemy, targetType));
		}

		return new SkillExecutionContext(actingEnemy, targetsByType, 0);
	}

	private List<GameCharacter> resolveEnemySkillTargets(Enemy actingEnemy, SkillTargetType targetType) {
		return switch (targetType) {
		case SELF -> throw new IllegalArgumentException("SELF targets are supplied automatically.");

		case SINGLE_ALLY -> List.of(selectEnemyAllyTarget());

		case ALL_ALLIES, RANDOM_ALLY ->
			enemyGroup.getSlots().stream().map(slot -> (GameCharacter) slot.getEnemy()).toList();

		case ALL_BUT_SELF -> enemyGroup.getSlots().stream().map(EnemyBattleSlot::getEnemy)
				.filter(enemy -> enemy != actingEnemy).map(enemy -> (GameCharacter) enemy).toList();

		case SINGLE_ENEMY -> List.of(selectEnemyTarget());

		case ALL_ENEMIES, RANDOM_ENEMY -> party.getMembers().stream().map(member -> (GameCharacter) member).toList();
		};
	}

	private Enemy selectEnemyAllyTarget() {
		List<EnemyBattleSlot> livingSlots = enemyGroup.getLivingSlots();

		if (livingSlots.isEmpty()) {
			throw new IllegalStateException("No living enemy ally is available.");
		}

		return livingSlots.get(random.nextInt(livingSlots.size())).getEnemy();
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

		case RANDOM_ALLY -> Optional.of(party.getMembers().stream().map(member -> (GameCharacter) member).toList());

		case SINGLE_ENEMY -> enemyTargetMenu.selectTarget(enemyGroup, currentRoundNumber)
				.map(slot -> List.<GameCharacter>of(slot.getEnemy()));

		case ALL_ENEMIES ->
			Optional.of(enemyGroup.getLivingSlots().stream().map(slot -> (GameCharacter) slot.getEnemy()).toList());

		case RANDOM_ENEMY -> selectRandomEnemyTarget();
		};
	}

	private Optional<List<GameCharacter>> selectSingleAllyTarget(SkillDefinition skill) {
		boolean targetsDefeated = targetsDefeatedAlly(skill, SkillTargetType.SINGLE_ALLY);

		String menuTitle = targetsDefeated ? "Escolha o aliado para reviver:" : "Escolha o aliado:";

		BiConsumer<List<PlayableCharacter>, String> display = (targets, feedback) -> battleScreen
				.displayAllyTargetSelection(
						scenarioName,
						battleType,
						enemyGroup,
						party,
						currentRoundNumber,
						menuTitle,
						targets,
						feedback
				);

		Optional<PlayableCharacter> selectedTarget = targetsDefeated
				? allyTargetMenu.selectDefeatedBattleTarget(party, display)
				: allyTargetMenu.selectLivingBattleTarget(party, display);

		return selectedTarget.map(target -> List.<GameCharacter>of(target));
	}

	private Optional<List<GameCharacter>> selectRandomEnemyTarget() {
		List<EnemyBattleSlot> candidates = enemyGroup.getLivingSlots();

		if (candidates.isEmpty()) {
			throw new IllegalStateException("No living enemy is available.");
		}

		return Optional.of(candidates.stream().map(slot -> (GameCharacter) slot.getEnemy()).toList());
	}

	private boolean targetsDefeatedAlly(SkillDefinition skill, SkillTargetType targetType) {
		return skill.getEffects().stream().anyMatch(
				effect -> effect.getTargetType() == targetType && effect.getEffectType() == SkillEffectType.REVIVE
		);
	}

	private List<String> formatSkillEffectResults(List<SkillEffectResult> results) {
		List<String> messages = new ArrayList<>();

		for (SkillEffectResult result : results) {
			GameCharacter target = result.getTarget();
			String targetName = getCharacterDisplayName(target);

			String message = switch (result.getOutcome()) {
			case RESISTED -> {
				SkillEffectDefinition effect = result.getEffect();

				if (effect.getEffectType() == SkillEffectType.ATTRIBUTE_STAGE) {
					yield targetName + " resistiu à redução de " + effect.getAttribute() + ".";
				}

				yield targetName + " resistiu à dissipação dos efeitos positivos.";
			}

			case NOT_TRIGGERED -> null;
			case APPLIED -> formatAppliedSkillEffectResult(result, target, targetName);
			};

			if (message != null) {
				messages.add(message);
			}
		}

		return messages;
	}

	private String formatAppliedSkillEffectResult(SkillEffectResult result, GameCharacter target, String targetName) {
		return switch (result.getEffect().getEffectType()) {
		case DAMAGE, RECOIL_DAMAGE -> targetName + " sofreu " + result.getAmount() + " de dano.";

		case HP_SACRIFICE -> targetName + " sacrificou " + result.getAmount() + " de HP.";

		case HP_RECOVERY -> targetName + " recuperou " + result.getAmount() + " de HP.";

		case MP_RECOVERY -> targetName + " recuperou " + result.getAmount() + " de MP.";

		case SP_RECOVERY -> targetName + " recuperou " + result.getAmount() + " de SP.";

		case HP_BALANCE -> targetName + " ficou com " + result.getAmount() + " de HP.";

		case REVIVE -> targetName + " foi revivido com " + result.getAmount() + " HP!";

		case ATTRIBUTE_STAGE -> formatAttributeStageResult(result, targetName);

		case PURIFICATION ->
			formatRemovedStates(result, targetName, "efeito negativo purificado", "efeitos negativos purificados");
		case DISSIPATION ->
			formatRemovedStates(result, targetName, "efeito positivo dissipado", "efeitos positivos dissipados");

		case SPECIAL_STATE -> formatSpecialStateResult(target, targetName);

		case DAMAGE_POWER_MODIFIER -> null;
		};
	}

	private String formatRemovedStates(SkillEffectResult result, String targetName, String singular, String plural) {
		int count = result.getAmount();

		return targetName + " teve " + count + " " + (count == 1 ? singular : plural) + ".";
	}

	private String formatAttributeStageResult(SkillEffectResult result, String targetName) {
		SkillEffectDefinition effect = result.getEffect();
		String direction = effect.getStages() > 0 ? " aumentou." : " diminuiu.";

		return getAttributeDisplayName(effect.getAttribute()) + " de " + targetName + direction;
	}

	private String formatSpecialStateResult(GameCharacter target, String targetName) {
		if (target.hasDistortedAttributes()) {
			return "Os atributos de " + targetName + " foram distorcidos.";
		}

		return "Os atributos de " + targetName + " voltaram ao normal.";
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

	private String getCharacterDisplayName(GameCharacter character) {
		for (EnemyBattleSlot slot : enemyGroup.getSlots()) {
			if (slot.getEnemy() == character) {
				return slot.getDisplayName();
			}
		}

		return character.getName();
	}

	private void updateFinishingCharacter(SkillUseState execution) {
		if (!enemyGroup.isDefeated()) {
			return;
		}

		GameCharacter actingCharacter = execution.getContext().getActingCharacter();

		if (!(actingCharacter instanceof PlayableCharacter playableCharacter)) {
			throw new IllegalStateException("Only a playable character can finish the enemy group.");
		}

		finishingCharacter = playableCharacter;
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

	private void spendSkillResources(GameCharacter actingCharacter, CombatSkillDefinition skill) {
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
				.calculateDamage(attacker, target, damageType, null, 0, weaponBaseDamage, 0);

		return target.takeDamage(calculatedDamage);
	}

	private void openStatusMenu() {
		boolean showingEffects = false;
		String feedbackMessage = null;

		while (true) {
			if (showingEffects) {
				battleScreen.displayStatusEffects(scenarioName, battleType, enemyGroup, party, feedbackMessage);
			} else {
				battleScreen.displayStatusResources(scenarioName, battleType, enemyGroup, party, feedbackMessage);
			}

			feedbackMessage = null;
			String choice = consoleInput.readTrimmedLine();

			switch (choice) {
			case "0" -> {
				return;
			}
			case "1" -> {
				if (showingEffects) {
					showingEffects = false;
				} else {
					feedbackMessage = "Opção inválida.";
				}
			}
			case "2" -> {
				if (!showingEffects) {
					showingEffects = true;
				} else {
					feedbackMessage = "Opção inválida.";
				}
			}
			case "3" -> openStatusLegend();
			default -> feedbackMessage = "Opção inválida.";
			}
		}
	}

	private void openStatusLegend() {
		String feedbackMessage = null;

		while (true) {
			battleScreen.displayStatusLegend(scenarioName, battleType, feedbackMessage);
			feedbackMessage = null;

			if (consoleInput.readTrimmedLine().equals("0")) {
				return;
			}

			feedbackMessage = "Opção inválida.";
		}
	}

	private void schedulePostActionItemStage(RecoveryItem item, PlayableCharacter target) {
		pendingPostActionItem = item;
		pendingPostActionTarget = target;
	}
}
