package rpg.ui;

import java.util.Objects;

import rpg.world.WorldClock;

// Formats fictional world time for console interfaces.
public final class WorldTimeFormatter {

	private static final int DAY_START_HOUR = 6;
	private static final int NIGHT_START_HOUR = 18;

	private static final String SUN_SYMBOL = "\u263C";
	private static final String MOON_SYMBOL = "\u263E";

	private WorldTimeFormatter() {
	}

	public static String formatCurrentTime(WorldClock worldClock) {
		Objects.requireNonNull(worldClock, "World clock cannot be null.");

		return format(worldClock.getDay(), worldClock.getHourOfDay());
	}

	public static String formatNextRestockingTime(WorldClock worldClock) {
		Objects.requireNonNull(worldClock, "World clock cannot be null.");

		long nextRestockingDay = Math.addExact(worldClock.getDay(), 1);

		return format(nextRestockingDay, 0);
	}

	private static String format(long day, int hourOfDay) {
		String periodSymbol = hourOfDay >= DAY_START_HOUR && hourOfDay < NIGHT_START_HOUR ? SUN_SYMBOL : MOON_SYMBOL;

		return String.format("Dia %d — %02d:00 %s" , day, hourOfDay, periodSymbol);
	}
}