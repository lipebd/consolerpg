package rpg.battle;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import rpg.characters.CharacterAttribute;
import rpg.characters.Enemy;
import rpg.characters.PlayableCharacter;
import rpg.enemies.EnemyTargetProfile;

// Selects individual party targets and keeps per-enemy target memory during one battle.
public final class EnemyTargetSelector {

	private static final int BASE_SCORE = 100;
	private static final int DEFENDING_PENALTY = 50;
	private static final int RETARGET_MARGIN = 40;

	private final Random random;
	private final Map<Enemy, PlayableCharacter> currentTargets;

	public EnemyTargetSelector(Random random) {
		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");
		this.currentTargets = new IdentityHashMap<>();
	}

	public PlayableCharacter selectTarget(
			Enemy actingEnemy,
			List<PlayableCharacter> partyMembers,
			boolean damagingAction,
			DamageType relevantDamageType
	) {
		Objects.requireNonNull(actingEnemy, "Acting enemy cannot be null.");
		Objects.requireNonNull(partyMembers, "Party member list cannot be null.");

		if (!damagingAction && relevantDamageType != null) {
			throw new IllegalArgumentException("A non-damaging action cannot define a damage type.");
		}

		for (PlayableCharacter partyMember : partyMembers) {
			Objects.requireNonNull(partyMember, "Party member cannot be null.");
		}

		List<PlayableCharacter> livingTargets = partyMembers.stream().filter(PlayableCharacter::isAlive).toList();

		if (livingTargets.isEmpty()) {
			throw new IllegalStateException("No living party member is available.");
		}

		EnemyTargetProfile profile = actingEnemy.getTargetProfile();

		return switch (profile) {
		case ERRATIC -> selectUniformly(livingTargets);
		case INSTINCTIVE -> selectPersistentTarget(actingEnemy, livingTargets);
		case PREDATOR, TACTICAL -> selectScoredTarget(
				actingEnemy,
				livingTargets,
				damagingAction,
				relevantDamageType
		);
		};
	}

	private PlayableCharacter selectPersistentTarget(
			Enemy actingEnemy,
			List<PlayableCharacter> livingTargets
	) {
		PlayableCharacter currentTarget = getValidCurrentTarget(actingEnemy, livingTargets);

		if (currentTarget != null) {
			return currentTarget;
		}

		PlayableCharacter selectedTarget = selectUniformly(livingTargets);
		currentTargets.put(actingEnemy, selectedTarget);

		return selectedTarget;
	}

	private PlayableCharacter selectScoredTarget(
			Enemy actingEnemy,
			List<PlayableCharacter> livingTargets,
			boolean damagingAction,
			DamageType relevantDamageType
	) {
		EnemyTargetProfile profile = actingEnemy.getTargetProfile();
		PlayableCharacter currentTarget = getValidCurrentTarget(actingEnemy, livingTargets);

		if (currentTarget == null) {
			PlayableCharacter selectedTarget = selectWeightedTarget(
					livingTargets,
					profile,
					damagingAction,
					relevantDamageType
			);

			currentTargets.put(actingEnemy, selectedTarget);

			return selectedTarget;
		}

		int currentScore = calculateScore(
				currentTarget,
				livingTargets,
				profile,
				damagingAction,
				relevantDamageType
		);

		int bestAlternativeScore = Integer.MIN_VALUE;
		List<PlayableCharacter> bestAlternatives = new ArrayList<>();

		for (PlayableCharacter candidate : livingTargets) {
			if (candidate == currentTarget) {
				continue;
			}

			int candidateScore = calculateScore(
					candidate,
					livingTargets,
					profile,
					damagingAction,
					relevantDamageType
			);

			if (candidateScore > bestAlternativeScore) {
				bestAlternativeScore = candidateScore;
				bestAlternatives.clear();
				bestAlternatives.add(candidate);
			} else if (candidateScore == bestAlternativeScore) {
				bestAlternatives.add(candidate);
			}
		}

		if (bestAlternativeScore <= currentScore + RETARGET_MARGIN) {
			return currentTarget;
		}

		PlayableCharacter selectedTarget = selectUniformly(bestAlternatives);
		currentTargets.put(actingEnemy, selectedTarget);

		return selectedTarget;
	}

	private PlayableCharacter getValidCurrentTarget(
			Enemy actingEnemy,
			List<PlayableCharacter> livingTargets
	) {
		PlayableCharacter currentTarget = currentTargets.get(actingEnemy);

		return livingTargets.contains(currentTarget) ? currentTarget : null;
	}

	private PlayableCharacter selectWeightedTarget(
			List<PlayableCharacter> livingTargets,
			EnemyTargetProfile profile,
			boolean damagingAction,
			DamageType relevantDamageType
	) {
		List<Integer> scores = new ArrayList<>(livingTargets.size());
		int totalScore = 0;

		for (PlayableCharacter candidate : livingTargets) {
			int score = calculateScore(
					candidate,
					livingTargets,
					profile,
					damagingAction,
					relevantDamageType
			);

			scores.add(score);
			totalScore = Math.addExact(totalScore, score);
		}

		int draw = random.nextInt(totalScore);

		for (int index = 0; index < livingTargets.size(); index++) {
			draw -= scores.get(index);

			if (draw < 0) {
				return livingTargets.get(index);
			}
		}

		throw new IllegalStateException("Weighted target selection did not produce a target.");
	}

	private int calculateScore(
			PlayableCharacter candidate,
			List<PlayableCharacter> livingTargets,
			EnemyTargetProfile profile,
			boolean damagingAction,
			DamageType relevantDamageType
	) {
		int missingHpPercent = calculateMissingHpPercent(candidate);
		int defendingPenalty = damagingAction && candidate.isDefending() ? DEFENDING_PENALTY : 0;

		int score = switch (profile) {
		case PREDATOR -> BASE_SCORE + 2 * missingHpPercent - defendingPenalty;
		case TACTICAL -> BASE_SCORE + missingHpPercent
				+ calculateDefensiveVulnerability(candidate, livingTargets, relevantDamageType)
				- defendingPenalty;
		case INSTINCTIVE, ERRATIC -> throw new IllegalArgumentException(
				"Target profile does not use target scores: " + profile
		);
		};

		return Math.max(1, score);
	}

	private int calculateMissingHpPercent(PlayableCharacter candidate) {
		long missingHp = candidate.getMaxHp() - candidate.getCurrentHp();

		return (int) (missingHp * 100L / candidate.getMaxHp());
	}

	private int calculateDefensiveVulnerability(
			PlayableCharacter candidate,
			List<PlayableCharacter> livingTargets,
			DamageType relevantDamageType
	) {
		if (relevantDamageType == null) {
			return 0;
		}

		CharacterAttribute relevantAttribute = switch (relevantDamageType) {
		case PHYSICAL -> CharacterAttribute.DEF;
		case MAGICAL -> CharacterAttribute.RES;
		};

		int highestDefense = livingTargets.stream()
				.mapToInt(target -> target.getEffectiveAttribute(relevantAttribute))
				.max()
				.orElseThrow();

		int candidateDefense = candidate.getEffectiveAttribute(relevantAttribute);

		return (int) ((long) (highestDefense - candidateDefense) * 100L / highestDefense);
	}

	private PlayableCharacter selectUniformly(List<PlayableCharacter> livingTargets) {
		if (livingTargets.size() == 1) {
			return livingTargets.get(0);
		}

		return livingTargets.get(random.nextInt(livingTargets.size()));
	}
}
