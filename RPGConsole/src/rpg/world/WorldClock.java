package rpg.world;

// Stores and advances the fictional world time.
public final class WorldClock {

	public static final int HOURS_PER_DAY = 24;
	public static final long INITIAL_ELAPSED_HOURS = 8;

	private long elapsedHours;

	public WorldClock() {
		this(INITIAL_ELAPSED_HOURS);
	}

	public WorldClock(long elapsedHours) {
		if (elapsedHours < INITIAL_ELAPSED_HOURS) {
			throw new IllegalArgumentException("Elapsed world hours cannot be lower than the initial campaign time.");
		}

		this.elapsedHours = elapsedHours;
	}

	public long getElapsedHours() {
		return elapsedHours;
	}

	public long getDay() {
		return elapsedHours / HOURS_PER_DAY + 1;
	}

	public int getHourOfDay() {
		return (int) (elapsedHours % HOURS_PER_DAY);
	}

	public boolean crossesMidnightAfter(int hours) {
		long resultingElapsedHours = calculateElapsedHoursAfter(hours);

		long currentCycle = elapsedHours / HOURS_PER_DAY;
		long resultingCycle = resultingElapsedHours / HOURS_PER_DAY;

		return currentCycle != resultingCycle;
	}

	public void advanceHours(int hours) {
		elapsedHours = calculateElapsedHoursAfter(hours);
	}

	private long calculateElapsedHoursAfter(int hours) {
		if (hours <= 0) {
			throw new IllegalArgumentException("Advanced world hours must be positive.");
		}

		try {
			return Math.addExact(elapsedHours, hours);
		} catch (ArithmeticException exception) {
			throw new IllegalStateException("World time exceeds the supported maximum.", exception);
		}
	}
}