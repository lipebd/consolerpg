package rpg.characters;

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
    private int healingPower;
    private int luck;
    private boolean defending;
    
    /**
     * Creates a character with full HP, MP and SP.
     *
     * @param name         character's name
     * @param maxHp        maximum health points
     * @param maxMp        maximum mana points
     * @param maxSp        maximum stamina points
     * @param attack       base physical attack
     * @param defense      base physical defense
     * @param healingPower base healing power
     * @param luck         base luck
     */
    protected GameCharacter(String name, int maxHp, int maxMp, int maxSp, int attack, int defense, int healingPower, int luck) {
        this.name = name;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.maxMp = maxMp;
        this.currentMp = maxMp;
        this.maxSp = maxSp;
        this.currentSp = maxSp;
        this.attack = attack;
        this.defense = defense;
        this.healingPower = healingPower;
        this.luck = luck;
        this.defending = false;
    }
    
    public int takeDamage(int damage) {
        int actualDamage = damage;

        if (defending) {
        	actualDamage = actualDamage / 2;
        	defending = false;
        }
        
        if (actualDamage < 1) {
        	actualDamage = 1;
        }
        if (actualDamage > currentHp) { // The max damage that can be taken is the current HP
        	actualDamage = currentHp;
        }
        
        currentHp = currentHp - actualDamage;

        return actualDamage;
    }
    
    public int recoverHp(int recoveryAmount) {
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
    
    public int attack(GameCharacter target) {
        int damage = attack - target.getDefense();

        if (damage < 1) {
            damage = 1;
        }

        return target.takeDamage(damage);
    }
    
    public int heal(GameCharacter target) {
    	int recoveryAmount = healingPower * attack /100;
    	
    	if (recoveryAmount < 1) {
    		recoveryAmount = 0;
    	}
    	
    	return target.recoverHp(recoveryAmount);
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
        return attack;
    }

    public int getDefense() {
        return defense;
    }
    
    public int getLuck() {
    	return luck;
    }
}