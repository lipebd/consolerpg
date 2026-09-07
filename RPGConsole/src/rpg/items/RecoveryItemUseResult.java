package rpg.items;

// Stores the effects produced by one recovery item use.
public final class RecoveryItemUseResult {

	private final int revivedHp;
	private final int recoveredHp;
	private final int recoveredMp;
	private final int recoveredSp;
	private final boolean purifiedStatusEffects;

	public RecoveryItemUseResult(int revivedHp, int recoveredHp, int recoveredMp, int recoveredSp,
			boolean purifiedStatusEffects) {
		if (revivedHp < 0 || recoveredHp < 0 || recoveredMp < 0 || recoveredSp < 0) {
			throw new IllegalArgumentException("Recovery result values cannot be negative.");
		}

		this.revivedHp = revivedHp;
		this.recoveredHp = recoveredHp;
		this.recoveredMp = recoveredMp;
		this.recoveredSp = recoveredSp;
		this.purifiedStatusEffects = purifiedStatusEffects;
	}

	public boolean hasAnyEffect() {
		return revivedHp > 0 || recoveredHp > 0 || recoveredMp > 0 || recoveredSp > 0 || purifiedStatusEffects;
	}

	public int getRevivedHp() {
		return revivedHp;
	}

	public int getRecoveredHp() {
		return recoveredHp;
	}

	public int getRecoveredMp() {
		return recoveredMp;
	}

	public int getRecoveredSp() {
		return recoveredSp;
	}

	public boolean purifiedStatusEffects() {
		return purifiedStatusEffects;
	}
}