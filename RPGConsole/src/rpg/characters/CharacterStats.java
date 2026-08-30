package rpg.characters;

// Stores the initial attributes of a game character.
public final class CharacterStats {

    private final int maxHp;
    private final int maxMp;
    private final int maxSp;
    private final int attack;
    private final int defense;
    private final int magic;
    private final int resistance;
    private final int speed;
    private final int luck;

    public CharacterStats(
            int maxHp,
            int maxMp,
            int maxSp,
            int attack,
            int defense,
            int magic,
            int resistance,
            int speed,
            int luck
    ) {
        this.maxHp = maxHp;
        this.maxMp = maxMp;
        this.maxSp = maxSp;
        this.attack = attack;
        this.defense = defense;
        this.magic = magic;
        this.resistance = resistance;
        this.speed = speed;
        this.luck = luck;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getMaxMp() {
        return maxMp;
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