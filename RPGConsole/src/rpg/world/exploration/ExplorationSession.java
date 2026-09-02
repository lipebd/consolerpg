package rpg.world.exploration;

import java.util.Objects;
import java.util.Random;

// Tracks the mutable state of an exploration session.
public final class ExplorationSession {

	private boolean complete;
	private boolean epicEncounterOffered;
	private boolean merchantEncountered;
	private final ExplorationSettings settings;
	private int currentPosition;

	public ExplorationSession(ExplorationSettings settings) {
		this(settings, 1);
	}

	public ExplorationSession(ExplorationSettings settings, int startingPosition) {
		this.settings = Objects.requireNonNull(settings, "Exploration settings cannot be null.");

		if (startingPosition < 1 || startingPosition > settings.getLength()) {
			throw new IllegalArgumentException("Exploration starting position must be between 1 and "
					+ settings.getLength() + ": " + startingPosition);
		}

		this.currentPosition = startingPosition;
		this.complete = false;
		this.merchantEncountered = false;
		this.epicEncounterOffered = false;
	}

	public int advance() {
		if (isComplete()) {
			throw new IllegalStateException("Exploration is already complete.");
		}

		if (isAtFinalPosition()) {
			throw new IllegalStateException("Cannot advance beyond the final position.");
		}

		currentPosition++;

		return currentPosition;
	}

	public boolean determineSpecialArea(Random random) {
		if (currentPosition == 1 || currentPosition == settings.getLength()) {
			return false;
		}

		int roll = random.nextInt(100) + 1;

		return roll <= settings.getSpecialAreaChance();
	}

	public boolean isAtFinalPosition() {
		return currentPosition == settings.getLength();
	}

	public boolean isComplete() {
		return complete;
	}

	public void markComplete() {
		if (complete) {
			throw new IllegalStateException("Exploration is already complete.");
		}

		if (!isAtFinalPosition()) {
			throw new IllegalStateException("Exploration can only be completed " + "at the final position.");
		}

		complete = true;
	}

	public boolean canEncounterMerchant() {
		return !merchantEncountered;
	}

	public void markMerchantEncountered() {
		if (merchantEncountered) {
			throw new IllegalStateException(
					"The merchant was already encountered " + "during this exploration session.");
		}

		merchantEncountered = true;
	}

	public boolean canOfferEpicEncounter() {
		return !epicEncounterOffered;
	}

	public void markEpicEncounterOffered() {
		if (epicEncounterOffered) {
			throw new IllegalStateException(
					"The epic encounter was already offered " + "during this exploration session.");
		}

		epicEncounterOffered = true;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}

	public ExplorationSettings getSettings() {
		return settings;
	}

}