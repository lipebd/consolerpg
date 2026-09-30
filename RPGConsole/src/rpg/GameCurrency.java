package rpg;

// Defines how the game's currency is displayed.
public final class GameCurrency {

        private static final String NAME = "GOLD";
        private static final String SYMBOL = "G";

        private GameCurrency() {
        }

        public static String getName() {
                return NAME;
        }

        public static String format(int amount) {
                return amount + SYMBOL;
        }
}
