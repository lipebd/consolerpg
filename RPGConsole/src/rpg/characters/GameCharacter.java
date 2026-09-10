package rpg.characters;

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

	/**
	 * Creates a character with full HP, MP and SP.
	 *
	 * @param name  character's name
	 * @param stats character's initial attributes
	 */
	public GameCharacter(String name, CharacterStats stats) {
		this.name = name;

		this.combatEffectState = new CombatEffectState();

		this.maxHp = stats.getMaxHp();
		this.currentHp = maxHp;

		this.maxMp = stats.getMaxMp();
		this.currentMp = maxMp;

		this.maxSp = stats.getMaxSp();
		this.currentSp = maxSp;

		this.attack = stats.getAttack();
		this.defense = stats.getDefense();
		this.magic = stats.getMagic();
		this.resistance = stats.getResistance();
		this.speed = stats.getSpeed();
		this.luck = stats.getLuck();

		this.defending = false;
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

	public int recoverHp(int recoveryAmount) {
		if (!isAlive()) {
			return 0;
		}
		int actualRecoveryAmount = recoveryAmount;
		if (actualRecoveryAmount < 1) {
			actualRecoveryAmount = 0;
		}
		if (actualRecoveryAmount > maxHp - currentHp) { // The max recovery amount needs to be the missing HP value
			actualRecoveryAmount = maxHp - currentHp;
		}
		currentHp = currentHp + actualRecoveryAmount;

		return actualRecoveryAmount;
	}

	public int recoverMp(int recoveryAmount) {
		if (!isAlive() || recoveryAmount <= 0) {
			return 0;
		}

		int actualRecoveryAmount = Math.min(recoveryAmount, maxMp - currentMp);

		currentMp += actualRecoveryAmount;

		return actualRecoveryAmount;
	}

	public int recoverSp(int recoveryAmount) {
		if (!isAlive() || recoveryAmount <= 0) {
			return 0;
		}

		int actualRecoveryAmount = Math.min(recoveryAmount, maxSp - currentSp);

		currentSp += actualRecoveryAmount;

		return actualRecoveryAmount;
	}

	public int revive(int recoveryAmount) {
		if (isAlive() || recoveryAmount <= 0) {
			return 0;
		}

		int actualRecoveryAmount = Math.min(recoveryAmount, maxHp);

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

	public int getMaxHp() {
		return maxHp;
	}

	public int getCurrentMp() {
		return currentMp;
	}

	public int getMaxMp() {
		return maxMp;
	}

	public int getCurrentSp() {
		return currentSp;
	}

	public int getMaxSp() {
		return maxSp;
	}

	public int getAttack() {
		int structuralValue = combatEffectState.hasDistortedAttributes() ? magic : attack;

		return calculateEffectiveAttribute(structuralValue, CharacterAttribute.ATK);
	}

	public int getDefense() {
		int structuralValue = combatEffectState.hasDistortedAttributes() ? resistance : defense;

		return calculateEffectiveAttribute(structuralValue, CharacterAttribute.DEF);
	}

	public int getMagic() {
		int structuralValue = combatEffectState.hasDistortedAttributes() ? attack : magic;

		return calculateEffectiveAttribute(structuralValue, CharacterAttribute.MAG);
	}

	public int getResistance() {
		int structuralValue = combatEffectState.hasDistortedAttributes() ? defense : resistance;

		return calculateEffectiveAttribute(structuralValue, CharacterAttribute.RES);
	}

	public int getSpeed() {
		return calculateEffectiveAttribute(speed, CharacterAttribute.VEL);
	}

	public int getLuck() {
		return calculateEffectiveAttribute(luck, CharacterAttribute.LCK);
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
