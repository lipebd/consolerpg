package rpg.items;

import java.util.Random;

// Defines an item drop chance and its possible quantity.
public class DropEntry {

    private final Item item;
    private final int dropChance;
    private final int minimumQuantity;
    private final int maximumQuantity;

    public DropEntry(
            Item item,
            int dropChance,
            int minimumQuantity,
            int maximumQuantity
    ) {
        if (dropChance < 0 || dropChance > 100) {
            throw new IllegalArgumentException(
                    "Drop chance must be between 0 and 100."
            );
        }

        if (
                minimumQuantity <= 0
                || maximumQuantity < minimumQuantity
        ) {
            throw new IllegalArgumentException(
                    "Invalid drop quantity range."
            );
        }

        this.item = item;
        this.dropChance = dropChance;
        this.minimumQuantity = minimumQuantity;
        this.maximumQuantity = maximumQuantity;
    }

    public int rollQuantity(Random random) {
        int chanceRoll = random.nextInt(100) + 1;

        if (chanceRoll > dropChance) {
            return 0;
        }

        int quantityRange =
                maximumQuantity - minimumQuantity + 1;

        return random.nextInt(quantityRange)
                + minimumQuantity;
    }

    public Item getItem() {
        return item;
    }

    public int getDropChance() {
        return dropChance;
    }
}