package rpg.battle;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import rpg.characters.PlayableCharacter;

// Calculates whether a character successfully flees from battle.
public final class BattleEscapeService {

	private static final double BASE_ESCAPE_CHANCE = 50.0;
	private static final double SPEED_WEIGHT = 3.0;
	private static final double LUCK_WEIGHT = 2.0;

	private static final double MINIMUM_ESCAPE_CHANCE = 10.0;
	private static final double MAXIMUM_ESCAPE_CHANCE = 90.0;

	private final Random random;

	public BattleEscapeService(Random random) {
		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");
	}

	public EscapeAttemptResult attemptEscape(PlayableCharacter actingCharacter, EnemyBattleGroup enemyGroup) {
		Objects.requireNonNull(actingCharacter, "Acting character cannot be null.");

		Objects.requireNonNull(enemyGroup, "Enemy battle group cannot be null.");

		if (enemyGroup.isDefeated()) {
			throw new IllegalStateException("Cannot flee from a defeated enemy group.");
		}

		double directLuckChance =
		        clampPercentage(
		                actingCharacter.getLuck()
		        );

		if (rollChance(directLuckChance)) {
			return EscapeAttemptResult.LUCK_SUCCESS;
		}

		double attributeChance = calculateAttributeEscapeChance(actingCharacter, enemyGroup);

		if (rollChance(attributeChance)) {
			return EscapeAttemptResult.ATTRIBUTE_SUCCESS;
		}

		return EscapeAttemptResult.FAILURE;
	}

	private double calculateAttributeEscapeChance(PlayableCharacter actingCharacter, EnemyBattleGroup enemyGroup) {
		List<EnemyBattleSlot> livingEnemies = enemyGroup.getLivingSlots();

		double enemyAverageSpeed = livingEnemies.stream().mapToInt(slot -> slot.getEnemy().getSpeed()).average()
				.orElseThrow();

		double enemyAverageLuck = livingEnemies.stream().mapToInt(slot -> slot.getEnemy().getLuck()).average()
				.orElseThrow();

		double calculatedChance = BASE_ESCAPE_CHANCE + SPEED_WEIGHT * (actingCharacter.getSpeed() - enemyAverageSpeed)
				+ LUCK_WEIGHT * (actingCharacter.getLuck() - enemyAverageLuck);

		return Math.max(MINIMUM_ESCAPE_CHANCE, Math.min(MAXIMUM_ESCAPE_CHANCE, calculatedChance));
	}

	private double clampPercentage(double percentage) {
		return Math.max(0.0, Math.min(100.0, percentage));
	}

	private boolean rollChance(double chance) {
		double roll = random.nextDouble() * 100.0;

		return roll < chance;
	}
}