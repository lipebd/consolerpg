package rpg.items;

import java.util.LinkedHashMap;
import java.util.Map;

// Stores the items owned by the party and their quantities.
public class Inventory {

    private final Map<Item, Integer> items;

    public Inventory() {
        this.items = new LinkedHashMap<>();
    }

    public boolean addItem(Item item, int quantity) {
        if (item == null || quantity <= 0) {
            return false;
        }

        int currentQuantity = items.getOrDefault(item, 0);

        items.put(item, currentQuantity + quantity);

        return true;
    }

    public boolean removeItem(Item item, int quantity) {
        if (item == null || quantity <= 0) {
            return false;
        }

        int currentQuantity = getQuantity(item);

        if (currentQuantity < quantity) {
            return false;
        }

        int remainingQuantity = currentQuantity - quantity;

        if (remainingQuantity == 0) {
            items.remove(item);
        } else {
            items.put(item, remainingQuantity);
        }

        return true;
    }

    public int getQuantity(Item item) {
        return items.getOrDefault(item, 0);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Map<Item, Integer> getContents() {
        return new LinkedHashMap<>(items);
    }
}