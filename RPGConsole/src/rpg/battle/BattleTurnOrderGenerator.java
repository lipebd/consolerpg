package rpg.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import rpg.characters.Party;
import rpg.characters.PlayableCharacter;

// Generates the action order of one complete battle round.
public final class BattleTurnOrderGenerator {

	public static final int MAXIMUM_ACTIONS_PER_PARTICIPANT = 3;

	private static final double MINIMUM_REFERENCE_SPEED = 1.0;

	private final Random random;

	public BattleTurnOrderGenerator(Random random) {
		this.random = Objects.requireNonNull(random, "Random generator cannot be null.");
	}

	public List<BattleTurnEntry> generate(Party party, EnemyBattleGroup enemyGroup) {
		Objects.requireNonNull(party, "Party cannot be null.");

		Objects.requireNonNull(enemyGroup, "Enemy battle group cannot be null.");

		List<BattleTurnEntry> partyEntries = createPartyEntries(party);

		List<BattleTurnEntry> enemyEntries = createEnemyEntries(enemyGroup);

		if (partyEntries.isEmpty() || enemyEntries.isEmpty()) {
			throw new IllegalStateException("Both battle sides must have living participants.");
		}

		double partyAverageSpeed = calculateAverageSpeed(partyEntries);

		double enemyAverageSpeed = calculateAverageSpeed(enemyEntries);

		List<BattleTurnEntry> participants = new ArrayList<>();

		participants.addAll(partyEntries);
		participants.addAll(enemyEntries);

		List<BattleTurnEntry> actionEntries = createActionEntries(participants, partyAverageSpeed, enemyAverageSpeed);

		randomizeExactTies(actionEntries);
		sortByInitiative(actionEntries);

		return List.copyOf(actionEntries);
	}

	private List<BattleTurnEntry> createPartyEntries(Party party) {
		List<BattleTurnEntry> entries = new ArrayList<>();

		for (PlayableCharacter member : party.getLivingMembers()) {

			entries.add(BattleTurnEntry.fromPartyMember(member));
		}

		return entries;
	}

	private List<BattleTurnEntry> createEnemyEntries(EnemyBattleGroup enemyGroup) {
		List<BattleTurnEntry> entries = new ArrayList<>();

		for (EnemyBattleSlot slot : enemyGroup.getLivingSlots()) {

			entries.add(BattleTurnEntry.fromEnemySlot(slot));
		}

		return entries;
	}

	private double calculateAverageSpeed(List<BattleTurnEntry> entries) {
		double averageSpeed = entries.stream().mapToInt(BattleTurnEntry::getSpeed).average().orElseThrow();

		return Math.max(MINIMUM_REFERENCE_SPEED, averageSpeed);
	}

	private void randomizeExactTies(List<BattleTurnEntry> participants) {
		Collections.shuffle(participants, random);
	}

	private void sortByInitiative(List<BattleTurnEntry> actionEntries) {
		Comparator<BattleTurnEntry> comparator = Comparator.comparingDouble(BattleTurnEntry::getInitiativeScore)
				.reversed().thenComparing(Comparator.comparingInt(BattleTurnEntry::getLuck).reversed());

		actionEntries.sort(comparator);
	}

	private List<BattleTurnEntry> createActionEntries(List<BattleTurnEntry> participants, double partyAverageSpeed,
			double enemyAverageSpeed) {
		List<BattleTurnEntry> actionEntries = new ArrayList<>();

		for (BattleTurnEntry participant : participants) {
			double opponentAverageSpeed = participant.getSide() == BattleSide.PARTY ? enemyAverageSpeed
					: partyAverageSpeed;

			int actionCount = calculateActionCount(participant.getSpeed(), opponentAverageSpeed);

			for (int actionNumber = 1; actionNumber <= actionCount; actionNumber++) {

				actionEntries.add(participant.createActionEntry(actionNumber));
			}
		}

		return actionEntries;
	}

	private int calculateActionCount(int participantSpeed, double opponentAverageSpeed) {
		double speedRatio = participantSpeed / opponentAverageSpeed;

		int actionCount = (int) Math.round(speedRatio);

		actionCount = Math.max(1, actionCount);

		return Math.min(MAXIMUM_ACTIONS_PER_PARTICIPANT, actionCount);
	}
}