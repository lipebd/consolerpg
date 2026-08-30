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
    private int magic;
    private int resistance;
    private int speed;
    private int luck;
    
    private boolean defending;
    
    /**
     * Creates a character with full HP, MP and SP.
     *
     * @param name  character's name
     * @param stats character's initial attributes
     */
    public GameCharacter(String name, CharacterStats stats) {
        this.name = name;

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
    
    public int getMagic() {
		return magic;
	}

	public int getResistance() {
		return resistance;
	}

	public int getSpeed() {
		return speed;
	}
    
    public int getLuck() {
    	return luck;
    }
}