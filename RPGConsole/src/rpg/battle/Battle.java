package rpg.battle;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

import rpg.characters.Enemy;
import rpg.characters.GameCharacter;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;
import rpg.skills.RecoveryType;
import rpg.skills.Skill;
import rpg.ui.AllyTargetMenu;
import rpg.ui.BattleTurnOrderScreen;
import rpg.ui.EnemyTargetMenu;
import rpg.ui.BattleItemMenu;

// Controls a battle between a party and an enemy group.
public class Battle {

	private static final int MINIMUM_STATUS_NAME_WIDTH = 8;

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
	private Scanner scanner;
	private boolean partyFled;
	private final BattleItemMenu battleItemMenu;

	public Battle(Party party, EnemyBattleGroup enemyGroup, Scanner scanner, Random random) {
		this.party = Objects.requireNonNull(party, "Party cannot be null.");
		this.enemyGroup = Objects.requireNonNull(enemyGroup, "Enemy battle group cannot be null.");
		this.scanner = Objects.requireNonNull(scanner, "Scanner cannot be null.");
		Objects.requireNonNull(random, "Random generator cannot be null.");
		this.enemyTargetMenu = new EnemyTargetMenu(scanner);
		this.partyFled = false;
		this.battleItemMenu = new BattleItemMenu(party, scanner);
		this.allyTargetMenu = new AllyTargetMenu(scanner);
		this.turnOrderGenerator = new BattleTurnOrderGenerator(random);
		this.turnOrderScreen = new BattleTurnOrderScreen();
		this.escapeService = new BattleEscapeService(random);
	}

	public BattleResult start() {
		currentRoundNumber = 1;

		displayBattleIntroduction();

		while (!party.isDefeated() && !enemyGroup.isDefeated() && !partyFled) {
			System.out.println("\n[BATALHA — RODADA " + currentRoundNumber + "]");

			int statusNameColumnWidth = calculateStatusNameColumnWidth();
			int hpValueColumnWidth = calculateHpValueColumnWidth();
			int mpValueColumnWidth = calculateMpValueColumnWidth();
			int spValueColumnWidth = calculateSpValueColumnWidth();

			displayEnemyStatuses(statusNameColumnWidth, hpValueColumnWidth);

			displayPartyStatus(statusNameColumnWidth, hpValueColumnWidth, mpValueColumnWidth, spValueColumnWidth);

			List<BattleTurnEntry> turnEntries = turnOrderGenerator.generate(party, enemyGroup);

			currentTurnOrder = new BattleTurnOrder(turnEntries);

			executeCurrentRound();

			currentRoundNumber++;
		}

		BattleResult result = determineBattleResult();

		displayBattleResult(result);

		return result;
	}

	// sobrecarga de métodos
	private void displayDamage(GameCharacter target, int damage) {
		displayDamage(target, target.getName(), damage);
	}

	private void displayDamage(GameCharacter target, String displayName, int damage) {
		System.out.println(displayName + " sofreu " + damage + " de dano. HP atual: " + target.getCurrentHp() + "/"
				+ target.getMaxHp() + ".");
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

	private void displayEnemyStatuses(int nameColumnWidth, int hpValueColumnWidth) {
		System.out.println("\n[INIMIGOS]");

		String rowFormat = "%-4s %-" + nameColumnWidth + "s HP %" + hpValueColumnWidth + "s%s%n";

		List<EnemyBattleSlot> slots = enemyGroup.getSlots();

		for (int index = 0; index < slots.size(); index++) {
			EnemyBattleSlot slot = slots.get(index);
			Enemy enemy = slot.getEnemy();

			String slotLabel = "[E" + (index + 1) + "]";
			String condition = slot.isAlive() ? "" : "  DERROTADO";

			System.out.printf(rowFormat, slotLabel, slot.getDisplayName(),
					formatResourceValue(enemy.getCurrentHp(), enemy.getMaxHp()), condition);
		}
	}

	private void displayPartyStatus(int nameColumnWidth, int hpValueColumnWidth, int mpValueColumnWidth,
			int spValueColumnWidth) {
		System.out.println("\n[GRUPO]");

		String rowFormat = "%-4s %-" + nameColumnWidth + "s HP %" + hpValueColumnWidth + "s  MP %" + mpValueColumnWidth
				+ "s  SP %" + spValueColumnWidth + "s%s%n";

		List<PlayableCharacter> members = party.getMembers();

		for (int index = 0; index < members.size(); index++) {
			PlayableCharacter member = members.get(index);

			String slotLabel = "[A" + (index + 1) + "]";
			String condition = member.isAlive() ? "" : "  DERROTADO";

			System.out.printf(rowFormat, slotLabel, member.getName(),
					formatResourceValue(member.getCurrentHp(), member.getMaxHp()),
					formatResourceValue(member.getCurrentMp(), member.getMaxMp()),
					formatResourceValue(member.getCurrentSp(), member.getMaxSp()), condition);
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

		System.out.println(
				"\n[" + slot.getDisplayName().toUpperCase() + "] " + analyzedEnemy.getRank().toString().toUpperCase());

		System.out.println(analyzedEnemy.getDescription());

		System.out.println("HP: " + analyzedEnemy.getCurrentHp() + "/" + analyzedEnemy.getMaxHp());

		System.out.println("ATK: " + analyzedEnemy.getAttack());

		System.out.println("DEF: " + analyzedEnemy.getDefense());
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

		System.out.println("Batalha contra:");

		for (EnemyBattleSlot slot : enemyGroup.getSlots()) {

			System.out.println("- " + slot.getDisplayName());
		}
	}

	private void displayNoRecoveryTargetMessage(Skill skill, boolean excludesSelf) {
		if (skill.getRecoveryType() == RecoveryType.REVIVE) {
			System.out.println("Não há aliados derrotados para reviver.");

			return;
		}

		if (excludesSelf) {
			System.out.println("Não há outros aliados disponíveis " + "para receber esta cura.");

			return;
		}

		System.out.println("Nenhum aliado precisa de cura.");
	}

	private void displayRecovery(PlayableCharacter target, int recoveredHp, RecoveryType recoveryType) {
		if (recoveryType == RecoveryType.REVIVE) {
			System.out.println(target.getName() + " foi revivido com " + recoveredHp + " HP!");

			return;
		}

		System.out.println(target.getName() + " recuperou " + recoveredHp + " de HP. HP atual: " + target.getCurrentHp()
				+ "/" + target.getMaxHp() + ".");
	}

	private void executeEnemyTurn(BattleTurnEntry actingEntry) {
		Enemy attackingEnemy = (Enemy) actingEntry.getCharacter();

		PlayableCharacter target = selectEnemyTarget();

		int damage = attackingEnemy.attack(target);

		System.out.println("\n" + actingEntry.getDisplayName() + " atacou " + target.getName() + "!");

		displayDamage(target, damage);

		if (!target.isAlive()) {
			lastDefeatedCharacter = target;
		}
	}

	private void executeCharacterTurn(PlayableCharacter actingCharacter) {
		boolean validAction = false;

		while (!validAction) {
			System.out.println("\n[1] Atacar | [2] Defender " + "| [3] Especial | [4] Analisar "
					+ "| [5] Itens | [6] Rodada " + "| [7] Fugir");
			System.out.print("> ");

			String choice = scanner.nextLine();

			switch (choice) {
			case "1" -> validAction = executeBasicAttack(actingCharacter);
			case "2" -> {
				actingCharacter.defend();

				System.out.println(actingCharacter.getName() + " assumiu uma postura defensiva!");

				validAction = true;
			}
			case "3" -> validAction = executeSkillMenu(actingCharacter);

			case "4" -> executeEnemyAnalysis();

			case "5" -> validAction = battleItemMenu.open(actingCharacter);

			case "6" -> turnOrderScreen.show(currentTurnOrder, currentRoundNumber);

			case "7" -> {
				executeEscapeAttempt(actingCharacter);
				validAction = true;
			}

			default -> System.out.println("Opção inválida.");
			}
		}
	}

	private boolean executeSkillMenu(PlayableCharacter actingCharacter) {
		List<Skill> skills = actingCharacter.getSkills();

		if (skills.isEmpty()) {
			System.out.println("Nenhuma habilidade disponível.");
			return false;
		}

		while (true) {
			System.out.println("\nEscolha uma habilidade:");
			System.out.println("\n[" + actingCharacter.getName().toUpperCase() + "] MP: "
					+ actingCharacter.getCurrentMp() + "/" + actingCharacter.getMaxMp() + " | SP: "
					+ actingCharacter.getCurrentSp() + "/" + actingCharacter.getMaxSp() + "\n");
			for (int index = 0; index < skills.size(); index++) {
				Skill skill = skills.get(index);

				String typeDisplayName = getSkillTypeDisplayName(skill);

				String powerDescription = getSkillPowerDescription(skill);

				System.out.println("[" + (index + 1) + "] " + skill.getName() + " [" + typeDisplayName + "]" + " | "
						+ powerDescription + " | MP: " + skill.getMpCost() + " | SP: " + skill.getSpCost());
			}

			System.out.println("[0] Voltar");
			System.out.print("> ");

			String choice = scanner.nextLine();

			if (choice.equals("0")) {
				return false;
			}

			try {
				int selectedIndex = Integer.parseInt(choice) - 1;

				if (selectedIndex < 0 || selectedIndex >= skills.size()) {
					System.out.println("Habilidade inválida.");
					continue;
				}

				Skill selectedSkill = skills.get(selectedIndex);

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

		System.out.println("\n" + actingCharacter.getName() + " atacou " + targetSlot.getDisplayName() + "!");

		int damage = actingCharacter.attack(target);

		displayDamage(target, targetSlot.getDisplayName(), damage);

		if (enemyGroup.isDefeated()) {
			finishingCharacter = actingCharacter;
		}

		return true;
	}

	private void executeTurn(BattleTurnEntry actingEntry) {
		switch (actingEntry.getSide()) {
		case PARTY -> {
			PlayableCharacter actingCharacter = (PlayableCharacter) actingEntry.getCharacter();

			executeCharacterTurn(actingCharacter);
		}

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

		case FAILURE -> System.out.println(
				"\n" + actingCharacter.getName() + " tentou fugir, mas os inimigos " + "bloquearam o caminho!");
		}
	}

	private void executeEnemyAnalysis() {
		Optional<EnemyBattleSlot> selectedSlot = enemyTargetMenu.selectTarget(enemyGroup);

		if (selectedSlot.isEmpty()) {
			return;
		}

		displayEnemyAnalysis(selectedSlot.get());
	}

	private void executeCurrentRound() {
		while (!currentTurnOrder.isComplete() && !party.isDefeated() && !enemyGroup.isDefeated() && !partyFled) {
			Optional<BattleTurnEntry> currentEntry = currentTurnOrder.getCurrentEntry();

			if (currentEntry.isEmpty()) {
				return;
			}

			BattleTurnEntry actingEntry = currentEntry.get();

			displayCurrentTurn(actingEntry);
			executeTurn(actingEntry);

			currentTurnOrder.advance();
		}
	}

	private boolean executeSelectedSkill(PlayableCharacter actingCharacter, Skill skill) {
		if (skill.getRecoveryType() != RecoveryType.NONE) {
			return executeRecoverySkill(actingCharacter, skill);
		}

		return executeDamageSkill(actingCharacter, skill);
	}

	private boolean executeDamageSkill(PlayableCharacter actingCharacter, Skill skill) {
		Optional<EnemyBattleSlot> selectedSlot = enemyTargetMenu.selectTarget(enemyGroup);

		if (selectedSlot.isEmpty()) {
			return false;
		}

		EnemyBattleSlot targetSlot = selectedSlot.get();

		Enemy target = targetSlot.getEnemy();

		spendSkillResources(actingCharacter, skill);

		System.out.println("\n" + actingCharacter.getName() + " usou " + skill.getName() + "!");

		int damage = actingCharacter.usePhysicalSkill(skill, target);

		displayDamage(target, targetSlot.getDisplayName(), damage);

		if (enemyGroup.isDefeated()) {
			finishingCharacter = actingCharacter;
		}

		return true;
	}

	private boolean executeRecoverySkill(PlayableCharacter actingCharacter, Skill skill) {
		Optional<List<PlayableCharacter>> selectedTargets = selectRecoveryTargets(actingCharacter, skill);

		if (selectedTargets.isEmpty()) {
			return false;
		}

		spendSkillResources(actingCharacter, skill);

		System.out.println("\n" + actingCharacter.getName() + " usou " + skill.getName() + "!");

		for (PlayableCharacter target : selectedTargets.get()) {

			int recoveredHp = actingCharacter.useRecoverySkill(skill, target);

			displayRecovery(target, recoveredHp, skill.getRecoveryType());
		}

		return true;
	}

	private boolean confirmSkillUse(Skill skill) {
		System.out.println("\n[" + skill.getName().toUpperCase() + "]");

		System.out.println(skill.getDescription());

		while (true) {
			System.out.println("\n[1] Usar | [0] Voltar");
			System.out.print("> ");

			String confirmation = scanner.nextLine();

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

	private Optional<List<PlayableCharacter>> selectRecoveryTargets(PlayableCharacter actingCharacter, Skill skill) {

		return switch (skill.getRecoveryTargetType()) {
		case SELF -> selectSelfRecoveryTarget(actingCharacter, skill);

		case SINGLE_ALLY -> selectSingleRecoveryTarget(skill);

		case ALL_ALLIES -> selectMultipleRecoveryTargets(actingCharacter, skill, true);

		case ALL_BUT_SELF -> selectMultipleRecoveryTargets(actingCharacter, skill, false);

		case NONE, SINGLE_ENEMY, ALL_ENEMIES, RANDOM_ALLY, RANDOM_ENEMY ->
			throw new IllegalArgumentException("Invalid recovery target type.");
		};
	}

	private Optional<List<PlayableCharacter>> selectSelfRecoveryTarget(PlayableCharacter actingCharacter, Skill skill) {

		if (!canReceiveRecovery(actingCharacter, skill.getRecoveryType())) {
			displayNoRecoveryTargetMessage(skill, false);

			return Optional.empty();
		}

		return Optional.of(List.of(actingCharacter));
	}

	private Optional<List<PlayableCharacter>> selectSingleRecoveryTarget(Skill skill) {

		Optional<PlayableCharacter> selectedTarget;

		if (skill.getRecoveryType() == RecoveryType.HEAL) {
			selectedTarget = allyTargetMenu.selectInjuredTarget(party);
		} else {
			selectedTarget = allyTargetMenu.selectDefeatedTarget(party);
		}

		return selectedTarget.map(target -> List.of(target));
	}

	private Optional<List<PlayableCharacter>> selectMultipleRecoveryTargets(PlayableCharacter actingCharacter,
			Skill skill, boolean includeSelf) {

		List<PlayableCharacter> targets = party.getMembers().stream()
				.filter(member -> includeSelf || member != actingCharacter)
				.filter(member -> canReceiveRecovery(member, skill.getRecoveryType())).toList();

		if (targets.isEmpty()) {
			displayNoRecoveryTargetMessage(skill, !includeSelf);

			return Optional.empty();
		}

		return Optional.of(targets);
	}

	private boolean canReceiveRecovery(PlayableCharacter target, RecoveryType recoveryType) {
		return switch (recoveryType) {
		case HEAL -> target.isAlive() && target.getCurrentHp() < target.getMaxHp();

		case REVIVE -> !target.isAlive();

		case NONE -> false;
		};
	}

	private void spendSkillResources(PlayableCharacter actingCharacter, Skill skill) {
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

	private String getSkillTypeDisplayName(Skill skill) {
		if (skill.getRecoveryType() != RecoveryType.NONE) {
			return skill.getRecoveryType().getDisplayName();
		}

		return skill.getDamageType().toString();
	}

	private String getSkillPowerDescription(Skill skill) {
		return switch (skill.getRecoveryType()) {
		case HEAL -> "Cura: " + skill.getBaseHealing();

		case REVIVE -> "Poder: " + skill.getBaseHealing();

		case NONE -> "Poder: " + skill.getBaseDamage();
		};
	}

	private String getBattleResultSubject(PlayableCharacter character) {
		if (party.size() == 1) {
			return character.getName();
		}

		return character.getName() + " e seus companheiros";
	}

}