package rpg.characters;

import java.util.Objects;

import rpg.effects.AttributeStageRules;
import rpg.effects.CombatEffectState;

// Represents the basic attributes and behaviors shared by all game characters.
public abstract class GameCharacter {

	private String name;
	private int currentHp;
	private int currentMp;
	private int currentSp;
	private int maxHp;
	private int maxMp;
	private int maxSp;
	private int attack;
	private int defense;
	private int magic;
	private int resistance;
	private int speed;
	private int luck;

	private final CombatEffectState combatEffectState;
	private boolean defending;

	public GameCharacter(String name, CharacterStats stats) {
		this.name = name;

		this.combatEffectState = new CombatEffectState();

		replaceNaturalStats(stats);

		this.currentHp = maxHp;
		this.currentMp = maxMp;
		this.currentSp = maxSp;

		this.defending = false;
	}

	protected final void replaceNaturalStats(CharacterStats stats) {
		Objects.requireNonNull(stats, "Character stats cannot be null.");

		this.maxHp = stats.getMaxHp();
		this.maxMp = stats.getMaxMp();
		this.maxSp = stats.getMaxSp();

		this.attack = stats.getAttack();
		this.defense = stats.getDefense();
		this.magic = stats.getMagic();
		this.resistance = stats.getResistance();
		this.speed = stats.getSpeed();
		this.luck = stats.getLuck();
	}

	public boolean restoreAllResources() {
		if (!isAlive()) {
			return false;
		}

		currentHp = getMaxHp();
		currentMp = getMaxMp();
		currentSp = getMaxSp();

		return true;
	}

	public int takeDamage(double damage) {
		if (!isAlive()) {
			return 0;
		}

		if (!Double.isFinite(damage)) {
			throw new IllegalArgumentException("Damage must be a finite number.");
		}

		double adjustedDamage = damage;

		if (defending) {
			adjustedDamage /= 2.0;
			defending = false;
		}

		long roundedDamage = Math.round(adjustedDamage);

		int actualDamage = (int) Math.min(Math.max(1L, roundedDamage), currentHp);

		currentHp -= actualDamage;

		if (!isAlive()) {
			clearTemporaryBattleEffects();
		}

		return actualDamage;
	}

	public int takeNonLethalDamage(int damage) {
		if (damage < 0) {
			throw new IllegalArgumentException("Non-lethal damage cannot be negative.");
		}

		if (!isAlive() || damage == 0 || currentHp == 1) {
			return 0;
		}

		int actualDamage = Math.min(damage, currentHp - 1);

		currentHp -= actualDamage;

		return actualDamage;
	}

	public int recoverHp(int recoveryAmount) {
		if (!isAlive()) {
			return 0;
		}
		int actualRecoveryAmount = recoveryAmount;
		if (actualRecoveryAmount < 1) {
			actualRecoveryAmount = 0;
		}
		if (actualRecoveryAmount > getMaxHp() - currentHp) { // The max recovery amount needs to be the missing HP value
			actualRecoveryAmount = getMaxHp() - currentHp;
		}
		currentHp = currentHp + actualRecoveryAmount;

		return actualRecoveryAmount;
	}

	public int recoverMp(int recoveryAmount) {
		if (!isAlive() || recoveryAmount <= 0) {
			return 0;
		}

		int actualRecoveryAmount = Math.min(recoveryAmount, getMaxMp() - currentMp);

		currentMp += actualRecoveryAmount;

		return actualRecoveryAmount;
	}

	public int recoverSp(int recoveryAmount) {
		if (!isAlive() || recoveryAmount <= 0) {
			return 0;
		}

		int actualRecoveryAmount = Math.min(recoveryAmount, getMaxSp() - currentSp);

		currentSp += actualRecoveryAmount;

		return actualRecoveryAmount;
	}

	public int revive(int recoveryAmount) {
		if (isAlive() || recoveryAmount <= 0) {
			return 0;
		}

		int actualRecoveryAmount = Math.min(recoveryAmount, getMaxHp());

		currentHp = actualRecoveryAmount;
		defending = false;

		return actualRecoveryAmount;
	}

	public boolean spendMp(int amount) {
		if (amount <= 0 || amount > currentMp) {
			return false;
		}
		currentMp = currentMp - amount;
		return true;
	}

	public boolean spendSp(int amount) {
		if (amount <= 0 || amount > currentSp) {
			return false;
		}
		currentSp = currentSp - amount;
		return true;
	}

	public CharacterStats getNaturalStats() {
		return new CharacterStats(maxHp, maxMp, maxSp, attack, defense, magic, resistance, speed, luck);
	}

	public boolean isAlive() {
		return currentHp > 0;
	}

	public void defend() {
		defending = true;
	}

	public String getName() {
		return name;
	}

	public int getCurrentHp() {
		return currentHp;
	}

	public int getCurrentMp() {
		return currentMp;
	}

	public int getCurrentSp() {
		return currentSp;
	}

	public int getMaxHp() {
		return Math.addExact(maxHp, getStructuralMaxHpBonus());
	}

	public int getMaxMp() {
		return Math.addExact(maxMp, getStructuralMaxMpBonus());
	}

	public int getMaxSp() {
		return Math.addExact(maxSp, getStructuralMaxSpBonus());
	}

	public int getAttack() {
		int structuralValue = combatEffectState.hasDistortedAttributes()
				? getStructuralAttributeValue(CharacterAttribute.MAG)
				: getStructuralAttributeValue(CharacterAttribute.ATK);

		return calculateEffectiveAttribute(structuralValue, CharacterAttribute.ATK);
	}

	public int getDefense() {
		int structuralValue = combatEffectState.hasDistortedAttributes()
				? getStructuralAttributeValue(CharacterAttribute.RES)
				: getStructuralAttributeValue(CharacterAttribute.DEF);

		return calculateEffectiveAttribute(structuralValue, CharacterAttribute.DEF);
	}

	public int getMagic() {
		int structuralValue = combatEffectState.hasDistortedAttributes()
				? getStructuralAttributeValue(CharacterAttribute.ATK)
				: getStructuralAttributeValue(CharacterAttribute.MAG);

		return calculateEffectiveAttribute(structuralValue, CharacterAttribute.MAG);
	}

	public int getResistance() {
		int structuralValue = combatEffectState.hasDistortedAttributes()
				? getStructuralAttributeValue(CharacterAttribute.DEF)
				: getStructuralAttributeValue(CharacterAttribute.RES);

		return calculateEffectiveAttribute(structuralValue, CharacterAttribute.RES);
	}

	public int getSpeed() {
		return calculateEffectiveAttribute(getStructuralAttributeValue(CharacterAttribute.VEL), CharacterAttribute.VEL);
	}

	public int getLuck() {
		return calculateEffectiveAttribute(getStructuralAttributeValue(CharacterAttribute.LCK), CharacterAttribute.LCK);
	}

	protected int getStructuralMaxHpBonus() {
		return 0;
	}

	protected int getStructuralMaxMpBonus() {
		return 0;
	}

	protected int getStructuralMaxSpBonus() {
		return 0;
	}

	protected int getStructuralAttributeBonus(CharacterAttribute attribute) {
		return 0;
	}

	protected final void clampCurrentResourcesToMaximums() {
		currentHp = Math.min(currentHp, getMaxHp());
		currentMp = Math.min(currentMp, getMaxMp());
		currentSp = Math.min(currentSp, getMaxSp());
	}

	private int getStructuralAttributeValue(CharacterAttribute attribute) {
		int naturalValue = switch (attribute) {
		case ATK -> attack;
		case DEF -> defense;
		case MAG -> magic;
		case RES -> resistance;
		case VEL -> speed;
		case LCK -> luck;
		};

		return Math.addExact(naturalValue, getStructuralAttributeBonus(attribute));
	}

	public boolean applyTimedAttributeStages(CharacterAttribute attribute, int stageChange, int duration) {
		return combatEffectState.applyTimedStages(attribute, stageChange, duration);
	}

	public boolean applyPendingSpeedStages(int stageChange) {
		return combatEffectState.applyPendingSpeedStages(stageChange);
	}

	public void finishConsumedTurn() {
		combatEffectState.finishConsumedTurn();
	}

	public boolean hasPositiveAttributeStates() {
		return combatEffectState.hasPositiveStates();
	}

	public boolean hasNegativeAttributeStates() {
		return combatEffectState.hasNegativeStates();
	}

	public boolean purifyNegativeAttributeStates() {
		return combatEffectState.purifyAllNegativeStates();
	}

	public boolean dissipatePositiveAttributeStates() {
		return combatEffectState.dissipateAllPositiveStates();
	}

	public boolean consumePendingSpeedStages() {
		return combatEffectState.consumePendingSpeedStages();
	}

	public void clearTemporaryBattleEffects() {
		combatEffectState.clearAll();
		defending = false;
	}

	public int getEffectiveAttributeStage(CharacterAttribute attribute) {
		return combatEffectState.getEffectiveStage(attribute);
	}

	public int getPositiveAttributeStages(CharacterAttribute attribute) {
		return combatEffectState.getPositiveStages(attribute);
	}

	public int getPositiveAttributeRemainingTurns(CharacterAttribute attribute) {
		return combatEffectState.getPositiveRemainingTurns(attribute);
	}

	public int getNegativeAttributeStages(CharacterAttribute attribute) {
		return combatEffectState.getNegativeStages(attribute);
	}

	public int getNegativeAttributeRemainingTurns(CharacterAttribute attribute) {
		return combatEffectState.getNegativeRemainingTurns(attribute);
	}

	private int calculateEffectiveAttribute(int structuralValue, CharacterAttribute attribute) {
		int effectiveStage = combatEffectState.getEffectiveStage(attribute);

		return AttributeStageRules.applyMultiplier(structuralValue, effectiveStage);
	}

	public void toggleDistortedAttributes() {
		combatEffectState.toggleDistortedAttributes();
	}

	public boolean hasDistortedAttributes() {
		return combatEffectState.hasDistortedAttributes();
	}

}
