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
		this.maxHp = requirePositive(maxHp, "Maximum HP");
		this.maxMp = requireNonNegative(maxMp, "Maximum MP");
		this.maxSp = requireNonNegative(maxSp, "Maximum SP");
		this.attack = requirePositive(attack, "ATK");
		this.defense = requirePositive(defense, "DEF");
		this.magic = requirePositive(magic, "MAG");
		this.resistance = requirePositive(resistance, "RES");
		this.speed = requirePositive(speed, "VEL");
		this.luck = requirePositive(luck, "LCK");
	}

	private static int requirePositive(int value, String fieldName) {
		if (value <= 0) {
			throw new IllegalArgumentException(fieldName + " must be positive.");
		}

		return value;
	}

	private static int requireNonNegative(int value, String fieldName) {
		if (value < 0) {
			throw new IllegalArgumentException(fieldName + " cannot be negative.");
		}

		return value;
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